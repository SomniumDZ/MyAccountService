package org.vinka.myaccountservice.business.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ResponseStatusException;
import org.vinka.myaccountservice.business.Payroll;
import org.vinka.myaccountservice.business.PayrollId;
import org.vinka.myaccountservice.business.exceptions.EmployeeNotExistException;
import org.vinka.myaccountservice.business.exceptions.PayrollDuplicateException;
import org.vinka.myaccountservice.persistance.PayrollRepository;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Service
public class PayrollService {
    private final PayrollRepository repository;
    private final UserService userService;

    @Autowired
    public PayrollService(PayrollRepository repository, UserService userService) {
        this.repository = repository;
        this.userService = userService;
    }

    @Transactional
    @Validated
    public void save(List<@Valid Payroll> payrolls) {
        payrolls.forEach(payroll -> {
            var duplicate = repository.findById(new PayrollId(
                    payroll.getEmployee(),
                    payroll.getPeriod())
            );
            if (duplicate.isPresent()) {
                throw new PayrollDuplicateException();
            }

            var userOptional = userService.findByEmailIgnoreCase(payroll.getEmployee());
            if (userOptional.isEmpty()) {
                throw new EmployeeNotExistException(payroll.getEmployee());
            }

            var user = userOptional.get();

            repository.save(payroll);
        });
    }

    public void update(Payroll payroll) {
        var toUpdateOpt = repository.findById(new PayrollId(
                payroll.getEmployee(), payroll.getPeriod()
        ));

        var toUpdate = toUpdateOpt.orElseThrow(
                () -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payroll not found")
        );

        toUpdate.setSalary(payroll.getSalary());

        repository.save(toUpdate);
    }

    public Optional<Payroll> findById(PayrollId id) {
        return repository.findById(id);
    }

    public List<Payroll> findByEmployee(String employee) {
        return repository.findAllByEmployee(employee);
    }
}
