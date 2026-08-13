package com.staysure.auth.controller;

import com.staysure.auth.dto.AuthResponse;
import com.staysure.auth.dto.ForgotPasswordRequest;
import com.staysure.auth.dto.LoginRequest;
import com.staysure.auth.dto.LogoutRequest;
import com.staysure.auth.dto.RefreshTokenRequest;
import com.staysure.auth.dto.RegisterRequest;
import com.staysure.auth.dto.ResetPasswordRequest;
import com.staysure.auth.service.AuthService;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request,
                                                              HttpServletRequest servletRequest) {
        AuthResponse response = authService.register(
                request,
                RequestUtils.getClientIp(servletRequest),
                RequestUtils.getUserAgent(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request,
                                                           HttpServletRequest servletRequest) {
        AuthResponse response = authService.login(
                request,
                RequestUtils.getClientIp(servletRequest),
                RequestUtils.getUserAgent(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request,
                                                             HttpServletRequest servletRequest) {
        AuthResponse response = authService.refresh(
                request.refreshToken(),
                RequestUtils.getClientIp(servletRequest),
                RequestUtils.getUserAgent(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok(ApiResponse.success("Logout successful"));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(ApiResponse.success("If the email exists, a reset link will be sent"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request,
                                                           HttpServletRequest servletRequest) {
        authService.resetPassword(request, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Password reset successful"));
    }
}
