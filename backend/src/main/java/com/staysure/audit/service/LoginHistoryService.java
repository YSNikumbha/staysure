package com.staysure.audit.service;

import com.staysure.audit.entity.LoginHistory;
import com.staysure.audit.repository.LoginHistoryRepository;
import com.staysure.user.entity.User;
import org.springframework.stereotype.Service;

@Service
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;

    public LoginHistoryService(LoginHistoryRepository loginHistoryRepository) {
        this.loginHistoryRepository = loginHistoryRepository;
    }

    public void record(User user, String attemptedEmail, String ipAddress, String userAgent,
                       boolean successful, String failureReason) {
        LoginHistory history = new LoginHistory();
        history.setUser(user);
        history.setAttemptedEmail(attemptedEmail);
        history.setIpAddress(ipAddress);
        history.setUserAgent(userAgent);
        history.setSuccessful(successful);
        history.setFailureReason(failureReason);
        loginHistoryRepository.save(history);
    }
}
