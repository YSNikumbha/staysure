package com.staysure.owner.dto;

import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.user.dto.UserResponse;

import java.time.LocalDateTime;

public record OwnerProfileResponse(
        Long id,
        UserResponse user,
        String businessName,
        String alternatePhone,
        String businessEmail,
        Integer experienceYears,
        String description,
        OwnerVerificationStatus verificationStatus,
        String verificationRemarks,
        LocalDateTime verifiedAt,
        Long verifiedBy,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
