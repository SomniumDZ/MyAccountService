package org.vinka.myaccountservice.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.vinka.myaccountservice.security.log.LogEvent;
import org.vinka.myaccountservice.security.log.LoggerService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SimpleAccessDeniedHandler implements AccessDeniedHandler {

    private final LoggerService loggerService;

    @Autowired
    public SimpleAccessDeniedHandler(LoggerService loggerService) {
        this.loggerService = loggerService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        loggerService.log(
                LogEvent.Action.ACCESS_DENIED,
                auth.getName(),
                request.getRequestURI(),
                request.getRequestURI()
        );
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied!");
    }
}
