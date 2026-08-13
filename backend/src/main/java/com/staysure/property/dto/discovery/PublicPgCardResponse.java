package com.staysure.property.dto.discovery;

import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyType;
import com.staysure.property.enums.PropertyVerificationStatus;

import java.math.BigDecimal;
import java.util.List;

public record PublicPgCardResponse(
        Long id,
        String slug,
        String name,
        String coverImage,
        String area,
        String city,
        GenderType genderType,
        PropertyType propertyType,
        BigDecimal startingRent,
        BigDecimal securityDeposit,
        boolean foodAvailable,
        Double averageRating,
        long totalBeds,
        long availableBeds,
        PropertyVerificationStatus verificationStatus,
        List<AmenityResponse> amenities
) {
}
