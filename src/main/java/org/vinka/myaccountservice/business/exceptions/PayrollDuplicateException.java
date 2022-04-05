package org.vinka.myaccountservice.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "Some of payrolls duplicates existent payrolls")
public class PayrollDuplicateException extends RuntimeException {
}
