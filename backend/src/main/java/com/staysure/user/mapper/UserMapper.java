package com.staysure.user.mapper;

import com.staysure.role.entity.Permission;
import com.staysure.role.entity.Role;
import com.staysure.user.dto.UserResponse;
import com.staysure.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .map(Enum::name)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getProfileImageUrl(),
                user.getStatus(),
                user.isEmailVerified(),
                user.isPhoneVerified(),
                user.getLastLoginAt(),
                roles,
                permissions
        );
    }
}
