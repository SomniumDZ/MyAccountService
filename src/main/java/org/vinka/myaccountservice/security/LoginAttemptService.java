package org.vinka.myaccountservice.security;


import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private final ConcurrentHashMap<String, Integer> attempts;

    public LoginAttemptService() {
        attempts = new ConcurrentHashMap<>();
    }


    public void loginSucceeded(String email) {
        clearAttempts(email);
    }

    public void clearAttempts(String email) {
        attempts.remove(email);
    }

    public int loginFailed(String email) {
        attempts.putIfAbsent(email, 1);
        attempts.computeIfPresent(email, (k, v) -> v += 1);
        return attempts.get(email);
    }
}
