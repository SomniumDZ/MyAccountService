package org.vinka.myaccountservice.persistance;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.vinka.myaccountservice.business.Payroll;
import org.vinka.myaccountservice.business.PayrollId;

import java.util.List;

@Repository
public interface PayrollRepository extends CrudRepository<Payroll, PayrollId> {
    List<Payroll> findAllByEmployee(String employee);
}
