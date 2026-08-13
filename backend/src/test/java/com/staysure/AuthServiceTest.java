package com.staysure;

import com.staysure.audit.service.AuditService;
import com.staysure.audit.service.LoginHistoryService;
import com.staysure.auth.dto.LoginRequest;
import com.staysure.auth.dto.RegisterRequest;
import com.staysure.auth.security.JwtService;
import com.staysure.auth.security.PasswordResetTokenRepository;
import com.staysure.auth.service.AuthService;
import com.staysure.auth.service.EmailService;
import com.staysure.auth.service.RefreshTokenService;
import com.staysure.auth.service.TokenGenerator;
import com.staysure.common.enums.UserStatus;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.role.service.RoleService;
import com.staysure.user.entity.User;
import com.staysure.user.mapper.UserMapper;
import com.staysure.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleService roleService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock private TokenGenerator tokenGenerator;
    @Mock private EmailService emailService;
    @Mock private UserMapper userMapper;
    @Mock private AuditService auditService;
    @Mock private LoginHistoryService loginHistoryService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                roleService,
                passwordEncoder,
                authenticationManager,
                jwtService,
                refreshTokenService,
                passwordResetTokenRepository,
                tokenGenerator,
                emailService,
                userMapper,
                auditService,
                loginHistoryService
        );
    }

    @Test
    void duplicateEmailIsRejected() {
        RegisterRequest request = registerRequest("User@example.com", "9999999999");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1", "test"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Email already registered");
    }

    @Test
    void duplicatePhoneIsRejected() {
        RegisterRequest request = registerRequest("user@example.com", "9999999999");
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("9999999999")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1", "test"))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Phone already registered");
    }

    @Test
    void passwordMismatchIsRejected() {
        RegisterRequest request = new RegisterRequest(
                "Example",
                "User",
                "user@example.com",
                "9999999999",
                "password123",
                "different123"
        );

        assertThatThrownBy(() -> authService.register(request, "127.0.0.1", "test"))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Password and confirmation do not match");
    }

    @Test
    void suspendedUserLoginIsRejectedAndRecorded() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setStatus(UserStatus.SUSPENDED);
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(authenticationManager.authenticate(any())).thenThrow(new LockedException("locked"));

        assertThatThrownBy(() -> authService.login(new LoginRequest("user@example.com", "password123"), "127.0.0.1", "test"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getStatus()).isEqualTo(HttpStatus.FORBIDDEN))
                .hasMessage("User account suspended");

        verify(loginHistoryService).record(user, "user@example.com", "127.0.0.1", "test", false, "User account suspended");
    }

    private RegisterRequest registerRequest(String email, String phone) {
        return new RegisterRequest(
                "Example",
                "User",
                email,
                phone,
                "password123",
                "password123"
        );
    }
}
