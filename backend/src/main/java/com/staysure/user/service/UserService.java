package com.staysure.user.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.user.dto.ChangePasswordRequest;
import com.staysure.user.dto.UpdateProfileRequest;
import com.staysure.user.dto.UserResponse;
import com.staysure.user.entity.User;
import com.staysure.user.mapper.UserMapper;
import com.staysure.user.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public User getUser(Long userId) {
        Long id = Objects.requireNonNull(userId, "userId must not be null");
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        return userMapper.toResponse(getUser(userId));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userRepository.findAll().stream().map(userMapper::toResponse).toList();
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request, String ipAddress) {
        User user = getUser(userId);
        String normalizedPhone = normalizePhone(request.phone());
        if (!user.getPhone().equals(normalizedPhone) && userRepository.existsByPhone(normalizedPhone)) {
            throw new DuplicateResourceException("Phone already registered", "PHONE_ALREADY_EXISTS");
        }
        user.setFirstName(request.firstName().trim());
        user.setLastName(request.lastName().trim());
        user.setPhone(normalizedPhone);
        user.setProfileImageUrl(blankToNull(request.profileImageUrl()));
        User saved = userRepository.save(user);
        auditService.log(saved, "USER_PROFILE_UPDATED", "USER", "User", saved.getId(),
                "User profile updated", null, null, ipAddress);
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request, String ipAddress) {
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new BusinessRuleException("Password and confirmation do not match", "PASSWORD_MISMATCH");
        }
        User user = getUser(userId);
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Current password is incorrect", "INVALID_CURRENT_PASSWORD");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        auditService.log(user, "PASSWORD_CHANGED", "USER", "User", user.getId(),
                "Password changed", null, null, ipAddress);
    }

    private String normalizePhone(String phone) {
        return phone == null ? null : phone.trim().replaceAll("[\\s()\\-]", "");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
