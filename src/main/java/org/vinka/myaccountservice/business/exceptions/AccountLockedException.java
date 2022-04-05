package org.vinka.myaccountservice.business.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.FORBIDDEN, reason = "User account is locked")
public class AccountLockedException extends RuntimeException{
}
