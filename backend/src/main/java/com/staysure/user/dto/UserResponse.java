package com.staysure.user.dto;

import com.staysure.common.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        String phone,
        String profileImageUrl,
        UserStatus status,
        boolean emailVerified,
        boolean phoneVerified,
        LocalDateTime lastLoginAt,
        Set<String> roles,
        Set<String> permissions
) {
}
