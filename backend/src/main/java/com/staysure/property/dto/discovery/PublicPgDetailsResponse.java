package com.staysure.property.dto.discovery;

import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.dto.PgImageResponse;
import com.staysure.property.dto.PropertyRuleResponse;
import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyType;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

public record PublicPgDetailsResponse(
        Long id,
        String slug,
        String name,
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
        PropertyRuleResponse rules,
        List<AmenityResponse> amenities,
        List<PgImageResponse> gallery,
        List<PublicRoomAvailabilityResponse> availableRooms,
        long availableBedCount,
        long totalBedCount,
        PublicOwnerSummaryResponse owner
) {
}
