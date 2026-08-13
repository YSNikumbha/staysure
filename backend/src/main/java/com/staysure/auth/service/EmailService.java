package com.staysure.auth.service;

public interface EmailService {
    void sendPasswordResetEmail(String email, String rawToken);
}
