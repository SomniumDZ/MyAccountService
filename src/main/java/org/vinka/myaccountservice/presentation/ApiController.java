package org.vinka.myaccountservice.presentation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.vinka.myaccountservice.business.Payroll;
import org.vinka.myaccountservice.business.PayrollInfoJson;
import org.vinka.myaccountservice.business.RoleEntity;
import org.vinka.myaccountservice.business.User;
import org.vinka.myaccountservice.business.exceptions.*;
import org.vinka.myaccountservice.business.service.PayrollService;
import org.vinka.myaccountservice.business.service.UserService;
import org.vinka.myaccountservice.security.log.LogEvent;
import org.vinka.myaccountservice.security.log.LoggerService;

import javax.validation.Valid;
import java.time.YearMonth;
import java.util.*;

import static org.vinka.myaccountservice.security.log.LogEvent.Action;

@RestController
public class ApiController {

    private final UserService userService;
    private final PayrollService payrollService;
    private final LoggerService loggerService;

    private final PasswordEncoder encoder;
    private final Set<String> breachedPasswords;

    @Autowired
    public ApiController(
            UserService userService,
            PayrollService payrollService,
            PasswordEncoder encoder,
            @Qualifier("breachedPasswords") Set<String> breachedPasswords,
            LoggerService loggerService
    ) {
        this.userService = userService;
        this.payrollService = payrollService;
        this.encoder = encoder;
        this.breachedPasswords = breachedPasswords;
        this.loggerService = loggerService;
    }

    @PostMapping("api/auth/signup")
    public User signup(@RequestBody @Valid User user) {
        if (userService.findByEmailIgnoreCase(user.getEmail()).isPresent()) {
            throw new UserExistException();
        }

        checkPassword(user.getPassword());
        user.setEmail(user.getEmail().toLowerCase(Locale.ROOT));
        user.setPassword(encoder.encode(user.getPassword()));
        user.setNonLocked(true);

        var admin = userService.findAdmin();
        if (admin.isPresent()) {
            var userRole = userService.findRole("ROLE_USER");
            user.setRoles(List.of(userRole));
        } else {
            var adminRole = userService.findRole("ROLE_ADMINISTRATOR");
            user.setRoles(List.of(adminRole));
        }

        loggerService.log(Action.CREATE_USER, "Anonymous", user.getEmail(), "/api/auth/signup");
        return userService.save(user);
    }

    @PostMapping("api/auth/changepass")
    public ChangePassJson changePassword(@RequestBody @Valid ChangePassJson json, Authentication auth) {

        checkPassword(json.getNewPassword());


        User user = userService.findByEmailIgnoreCase(auth.getName()).orElseThrow(UserNotFoundException::new);

        if (encoder.matches(json.getNewPassword(), user.getPassword())) {
            throw new SamePasswordException();
        }

        user.setPassword(encoder.encode(json.getNewPassword()));
        userService.save(user);

        json.setEmail(user.getEmail());
        json.setStatus("The password has been updated successfully");

        loggerService.log(Action.CHANGE_PASSWORD, auth.getName(), user.getEmail(), "/api/auth/changepass");
        return json;
    }

    private void checkPassword(String password) {
        if (password.length() < 12) {
            throw new PasswordLengthException();
        }
        if (breachedPasswords.contains(password) ){
            throw new PasswordBreachedException();
        }
    }

    @GetMapping("api/admin/user")
    public List<User> getUsers() {
        return userService.findUsers();
    }

    @DeleteMapping("api/admin/user")
    public void deleteNobody() {
        throw new UserNotFoundException();
    }

    @DeleteMapping("api/admin/user/{user_email}")
    public Map<String, String> deleteUser(@PathVariable(name = "user_email") String email, Authentication auth) {
        var user = userService.findUser(email);
        var isAdmin = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMINISTRATOR"::equals);

        if (isAdmin) {
            throw new RemoveAdminException();
        }

        userService.delete(user);
        loggerService.log(
                Action.DELETE_USER,
                auth.getName(),
                user.getEmail(),
                "/api/admin/user"
        );
        return Map.of("user", email, "status", "Deleted successfully!");
    }

    @PutMapping("api/admin/user/role")
    public User changeRole(@RequestBody UserRoleChange change, Authentication auth) {
        var user = (User) userService.findUser(change.getUser());

        try {
            user = userService.changeRole(change, user);
        } catch (ChangePassException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }

        UserRoleChange.OP op = change.getOperation();

        loggerService.log(
                op.toLogAction(),
                auth.getName(),
                String.format(op.getFormatMessage(), change.getRole(), user.getEmail()),
                "/api/admin/user/role"
        );

        return user;
    }

    @PutMapping("api/admin/user/access")
    private Map<String, String> putUserAccess(@RequestBody UserAccessChange change, Authentication auth) {
        User user = userService.findUser(change.getUserEmail());
        var op = change.getOperation();

        switch (op) {
            case LOCK -> {
                if (user.getRoles().stream().map(RoleEntity::getName).anyMatch("ROLE_ADMINISTRATOR"::equals)) {
                    throw new ResponseStatusException(
                            HttpStatus.BAD_REQUEST,
                            "Can't lock the ADMINISTRATOR!"
                    );
                }
                user = userService.lockUser(user);
            }
            case UNLOCK -> user = userService.unlockUser(user);
        }

        loggerService.log(
                op.toLogAction(),
                auth.getName(),
                String.format(op.getLogFormat(), user.getEmail()),
                "/api/admin/user/access"
        );

        return Map.of("status", String.format(op.getStatusFormat(), user.getEmail()));
    }


    @GetMapping("api/security/events")
    public List<LogEvent> getEvents() {
        return loggerService.findLog();
    }

    @PostMapping("api/acct/payments")
    public Map<String, String> postPayments(
            @RequestBody List<@Valid Payroll> payrolls
            ) {
        try {
            payrollService.save(payrolls);
        } catch (org.springframework.transaction.TransactionSystemException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
        return Map.of("status", "Added successfully!");
    }

    @PutMapping("api/acct/payments")
    public Map<String, String> putPayment(
            @RequestBody @Valid Payroll payroll
    ) {
        payrollService.update(payroll);
        return Map.of("status", "Updated successfully!");
    }

    @GetMapping("api/empl/payment")
    @Validated
    public ResponseEntity<?> getPayment(
            @RequestParam(required = false) @DateTimeFormat(pattern = "MM-yyyy") YearMonth period,
            Authentication auth
    ) {
        var user = userService.findUser(auth.getName());
        var payrolls = payrollService.findByEmployee(auth.getName());

        if (period != null) {
            var response = payrolls.stream()
                    .filter(p -> p.getPeriod().compareTo(period) == 0)
                    .map(p -> new PayrollInfoJson(p, user)).toList();
            return ResponseEntity.ok(
                    response.isEmpty() ? new EmptyJson() : response.get(0)
            );
        } else {
            List<PayrollInfoJson> ps = new ArrayList<>();
            payrolls.forEach(p -> ps.add(new PayrollInfoJson(p, user)));
            Collections.reverse(ps);
            return ResponseEntity.ok(ps);
        }
    }
}
