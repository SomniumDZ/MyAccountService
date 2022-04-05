package org.vinka.myaccountservice.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.vinka.myaccountservice.business.RoleEntity;
import org.vinka.myaccountservice.business.User;
import org.vinka.myaccountservice.business.exceptions.ChangePassException;
import org.vinka.myaccountservice.business.exceptions.RemoveAdminException;
import org.vinka.myaccountservice.business.exceptions.RoleNotFoundException;
import org.vinka.myaccountservice.business.exceptions.UserNotFoundException;
import org.vinka.myaccountservice.persistance.RoleRepository;
import org.vinka.myaccountservice.persistance.UserRepository;
import org.vinka.myaccountservice.presentation.UserRoleChange;
import org.vinka.myaccountservice.security.log.LogEvent;
import org.vinka.myaccountservice.security.log.LoggerService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;

    private final LoggerService loggerService;
    private final HttpServletRequest request;

    @Autowired
    public UserService(UserRepository userRepo, RoleRepository roleRepo, LoggerService loggerService, HttpServletRequest request) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.loggerService = loggerService;
        this.request = request;
        createImplicitRoles();
    }

    public Optional<User> findByEmailIgnoreCase(String email) {
        return userRepo.findByEmailIgnoreCase(email);
    }

    public User save(User user) {
        return userRepo.save(user);
    }

    public User findUser(String email) {
        return findByEmailIgnoreCase(email).orElseThrow(UserNotFoundException::new);
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        var opt = findByEmailIgnoreCase(username);
        if (opt.isEmpty()) {
            loggerService.log(
                    LogEvent.Action.LOGIN_FAILED,
                    username,
                    request.getRequestURI(),
                    request.getRequestURI()
            );
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return opt.get();
    }

    private void createImplicitRoles() {
        roleRepo.save(new RoleEntity("ROLE_USER", "BUSINESS"));
        roleRepo.save(new RoleEntity("ROLE_ADMINISTRATOR", "ADMIN"));
        roleRepo.save(new RoleEntity("ROLE_ACCOUNTANT", "BUSINESS"));
        roleRepo.save(new RoleEntity("ROLE_AUDITOR", "BUSINESS"));
    }

    public Optional<User> findAdmin() {
        return userRepo.findByRolesContaining(findRole("ROLE_ADMINISTRATOR"));
    }

    public RoleEntity findRole(String role) {
        return roleRepo.findById(role).orElseThrow(RoleNotFoundException::new);
    }

    public List<User> findUsers() {
        return userRepo.findAllByOrderByIdAsc();
    }

    public void delete(User user) {
        userRepo.delete(user);
    }

    public User changeRole(UserRoleChange change, User user) {
        var role = findRole("ROLE_" + change.getRole());

        switch (change.getOperation()) {
            case GRANT -> grantRole(user, role);
            case REMOVE -> removeRole(user, role);
        }

        save(user);
        return user;
    }

    private void grantRole(User user, RoleEntity role) {
        //Checks if granted role group corresponds with user roles group
        var sameRoleGroup = user.getRoles().stream()
                .map(RoleEntity::getRoleGroup)
                .allMatch(role.getRoleGroup()::equals);

        if (!sameRoleGroup) {
            throw new ChangePassException("The user cannot combine administrative and business roles!");
        }
        user.getRoles().add(role);
    }

    private void removeRole(User user, RoleEntity role) {
        if (!user.getRoles().contains(role)) {
            throw new ChangePassException("The user does not have a role!");
        }
        if (role.getName().equals("ROLE_ADMINISTRATOR")) {
            throw new RemoveAdminException();
        }
        if (user.getRoles().size() == 1) {
            throw new ChangePassException("The user must have at least one role!");
        }
        user.getRoles().remove(role);
    }

    public User lockUser(User user) {
        if (user.getRoles().stream().map(RoleEntity::getName).anyMatch("ROLE_ADMINISTRATOR"::equals)) {
            return user;
        }
        user.setNonLocked(false);
        return save(user);
    }

    public User unlockUser(User user) {
        user.setNonLocked(true);
        return save(user);
    }
}
