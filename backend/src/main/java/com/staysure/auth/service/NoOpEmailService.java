package com.staysure.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class NoOpEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(NoOpEmailService.class);

    @Override
    public void sendPasswordResetEmail(String email, String rawToken) {
        log.info("Password reset email requested for {}. Configure an EmailService implementation to send reset links.", email);
    }
}
