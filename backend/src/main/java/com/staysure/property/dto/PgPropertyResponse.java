package com.staysure.property.dto;

import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyType;
import com.staysure.property.enums.PropertyVerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record PgPropertyResponse(
        Long id,
        Long ownerId,
        String name,
        String slug,
        String description,
        GenderType genderType,
        PropertyType propertyType,
        String addressLine1,
        String addressLine2,
        String area,
        String city,
        String state,
        String pincode,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal startingRent,
        BigDecimal securityDeposit,
        Integer noticePeriodDays,
        Integer lockInMonths,
        LocalTime entryTime,
        boolean foodAvailable,
        PropertyStatus status,
        PropertyVerificationStatus verificationStatus,
        LocalDateTime submittedForVerificationAt,
        LocalDateTime verifiedAt,
        Long verifiedBy,
        String verificationRemarks,
        String rejectionReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
