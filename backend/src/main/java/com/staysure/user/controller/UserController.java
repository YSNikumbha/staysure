package com.staysure.user.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.user.dto.ChangePasswordRequest;
import com.staysure.user.dto.UpdateProfileRequest;
import com.staysure.user.dto.UserResponse;
import com.staysure.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> me() {
        return ResponseEntity.ok(ApiResponse.success("Profile loaded", userService.getProfile(SecurityUtils.currentUserId())));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateMe(@Valid @RequestBody UpdateProfileRequest request,
                                                              HttpServletRequest servletRequest) {
        UserResponse response = userService.updateProfile(
                SecurityUtils.currentUserId(),
                request,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @PutMapping("/me/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                            HttpServletRequest servletRequest) {
        userService.changePassword(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Password changed"));
    }
}
