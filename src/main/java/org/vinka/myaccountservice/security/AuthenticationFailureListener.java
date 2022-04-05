package org.vinka.myaccountservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;
import org.vinka.myaccountservice.business.service.UserService;
import org.vinka.myaccountservice.presentation.UserAccessChange;
import org.vinka.myaccountservice.security.log.LogEvent;
import org.vinka.myaccountservice.security.log.LoggerService;

import javax.servlet.http.HttpServletRequest;

@Component
public class AuthenticationFailureListener
        implements ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final LoginAttemptService loginAttemptService;
    private final UserService userService;
    private final LoggerService logger;
    private final HttpServletRequest request;

    @Autowired
    public AuthenticationFailureListener(LoginAttemptService loginAttemptService, UserService userService, LoggerService logger, HttpServletRequest request) {
        this.loginAttemptService = loginAttemptService;
        this.userService = userService;
        this.logger = logger;
        this.request = request;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        var email = event.getAuthentication().getName();
        var userOpt = userService.findByEmailIgnoreCase(email);

        logger.log(
                LogEvent.Action.LOGIN_FAILED,
                email,
                request.getRequestURI(),
                request.getRequestURI()
        );

        //If login failed for non-existent user then nothing
        if (userOpt.isEmpty()) return;


        final int MAX_ATTEMPTS = 5;

        if (loginAttemptService.loginFailed(email) > MAX_ATTEMPTS) {
            logger.log(
                    LogEvent.Action.BRUTE_FORCE,
                    email,
                    request.getRequestURI(),
                    request.getRequestURI()
            );
            userService.lockUser(userOpt.get());
            loginAttemptService.clearAttempts(email);
            logger.log(
                    LogEvent.Action.LOCK_USER,
                    email,
                    String.format(UserAccessChange.OP.LOCK.getLogFormat(), email),
                    request.getRequestURI()
            );
        }
    }
}
