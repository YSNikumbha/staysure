package com.staysure.auth.service;

import com.staysure.audit.service.AuditService;
import com.staysure.audit.service.LoginHistoryService;
import com.staysure.auth.dto.AuthResponse;
import com.staysure.auth.dto.ForgotPasswordRequest;
import com.staysure.auth.dto.LoginRequest;
import com.staysure.auth.dto.RegisterRequest;
import com.staysure.auth.dto.ResetPasswordRequest;
import com.staysure.auth.security.JwtService;
import com.staysure.auth.security.PasswordResetToken;
import com.staysure.auth.security.PasswordResetTokenRepository;
import com.staysure.auth.security.RefreshToken;
import com.staysure.auth.security.UserPrincipal;
import com.staysure.common.enums.RoleName;
import com.staysure.common.enums.UserStatus;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.common.util.TokenHashUtil;
import com.staysure.role.service.RoleService;
import com.staysure.user.entity.User;
import com.staysure.user.mapper.UserMapper;
import com.staysure.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final LoginHistoryService loginHistoryService;

    public AuthService(UserRepository userRepository,
                       RoleService roleService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       TokenGenerator tokenGenerator,
                       EmailService emailService,
                       UserMapper userMapper,
                       AuditService auditService,
                       LoginHistoryService loginHistoryService) {
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.emailService = emailService;
        this.userMapper = userMapper;
        this.auditService = auditService;
        this.loginHistoryService = loginHistoryService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request, String ipAddress, String userAgent) {
        validatePasswordConfirmation(request.password(), request.confirmPassword());
        String email = normalizeEmail(request.email());
        String phone = normalizePhone(request.phone());

        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered", "EMAIL_ALREADY_EXISTS");
        }
        if (userRepository.existsByPhone(phone)) {
            throw new DuplicateResourceException("Phone already registered", "PHONE_ALREADY_EXISTS");
        }

        User user = new User();
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setStatus(UserStatus.ACTIVE);
        roleService.assignRoleIfMissing(user, RoleName.USER);
        User saved = userRepository.save(user);

        auditService.log(saved, "USER_REGISTERED", "AUTH", "User", saved.getId(),
                "User registered", null, null, ipAddress);

        UserPrincipal principal = UserPrincipal.from(saved);
        String accessToken = jwtService.generateAccessToken(principal);
        String refreshToken = refreshTokenService.create(saved, ipAddress, userAgent);
        return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(saved));
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        String email = normalizeEmail(request.email());
        User user = userRepository.findByEmail(email).orElse(null);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, request.password()));
            if (user == null) {
                throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password", "INVALID_CREDENTIALS");
            }
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);
            loginHistoryService.record(user, email, ipAddress, userAgent, true, null);
            auditService.log(user, "USER_LOGIN", "AUTH", "User", user.getId(),
                    "User logged in", null, null, ipAddress);
            UserPrincipal principal = UserPrincipal.from(user);
            String accessToken = jwtService.generateAccessToken(principal);
            String refreshToken = refreshTokenService.create(user, ipAddress, userAgent);
            return new AuthResponse(accessToken, refreshToken, userMapper.toResponse(user));
        } catch (AuthenticationException | ApiException ex) {
            String reason = user != null && user.getStatus() == UserStatus.SUSPENDED ? "User account suspended" : "Invalid credentials";
            loginHistoryService.record(user, email, ipAddress, userAgent, false, reason);
            if (user != null && user.getStatus() == UserStatus.SUSPENDED) {
                throw new ApiException(HttpStatus.FORBIDDEN, "User account suspended", "USER_SUSPENDED");
            }
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password", "INVALID_CREDENTIALS");
        }
    }

    @Transactional
    public AuthResponse refresh(String rawRefreshToken, String ipAddress, String userAgent) {
        RefreshToken consumed = refreshTokenService.consume(rawRefreshToken);
        User user = consumed.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "User account is not active", "USER_NOT_ACTIVE");
        }
        UserPrincipal principal = UserPrincipal.from(user);
        String accessToken = jwtService.generateAccessToken(principal);
        String newRefreshToken = refreshTokenService.create(user, ipAddress, userAgent);
        return new AuthResponse(accessToken, newRefreshToken, userMapper.toResponse(user));
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenService.revoke(rawRefreshToken);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        String email = normalizeEmail(request.email());
        userRepository.findByEmail(email).ifPresent(user -> {
            String rawToken = tokenGenerator.generate();
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setUser(user);
            resetToken.setTokenHash(TokenHashUtil.sha256(rawToken));
            resetToken.setExpiresAt(LocalDateTime.now().plusMinutes(30));
            resetToken.setUsed(false);
            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(email, rawToken);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request, String ipAddress) {
        validatePasswordConfirmation(request.newPassword(), request.confirmPassword());
        String email = normalizeEmail(request.email());
        PasswordResetToken resetToken = passwordResetTokenRepository.findByTokenHash(TokenHashUtil.sha256(request.token()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token", "INVALID_RESET_TOKEN"));
        if (!resetToken.getUser().getEmail().equals(email) || resetToken.isUsed() || resetToken.isExpired()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset token", "INVALID_RESET_TOKEN");
        }
        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        resetToken.setUsed(true);
        resetToken.setUsedAt(LocalDateTime.now());
        passwordResetTokenRepository.save(resetToken);
        auditService.log(user, "PASSWORD_CHANGED", "AUTH", "User", user.getId(),
                "Password reset completed", null, null, ipAddress);
    }

    private void validatePasswordConfirmation(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new BusinessRuleException("Password and confirmation do not match", "PASSWORD_MISMATCH");
        }
    }

    public String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    public String normalizePhone(String phone) {
        return phone == null ? null : phone.trim().replaceAll("[\\s()\\-]", "");
    }
}
