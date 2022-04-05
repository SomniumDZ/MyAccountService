package org.vinka.myaccountservice.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class EmployeeNotExistException extends RuntimeException {
    public EmployeeNotExistException(String employee) {
        super("There's no employee with email " + employee);
    }
}
