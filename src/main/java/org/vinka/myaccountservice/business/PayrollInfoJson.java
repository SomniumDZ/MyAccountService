package org.vinka.myaccountservice.business;

import lombok.Data;

import java.time.format.DateTimeFormatter;

@Data
public class PayrollInfoJson {
    private String name;
    private String lastname;
    private String period;
    private String salary;

    public PayrollInfoJson(Payroll payroll, User user) {
        name = user.getName();
        lastname = user.getLastname();
        period = payroll.getPeriod().format(DateTimeFormatter.ofPattern("MMMM-yyyy"));
        salary = String.format(
                "%d dollar(s) %d cent(s)",
                payroll.getSalary() / 100, payroll.getSalary() % 100
        );
    }
}
