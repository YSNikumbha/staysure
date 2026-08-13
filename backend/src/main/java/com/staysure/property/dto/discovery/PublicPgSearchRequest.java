package com.staysure.property.dto.discovery;

import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyType;
import com.staysure.property.enums.SharingType;

import java.math.BigDecimal;
import java.util.List;

public record PublicPgSearchRequest(
        String search,
        String city,
        String area,
        BigDecimal minRent,
        BigDecimal maxRent,
        GenderType genderType,
        PropertyType propertyType,
        SharingType sharingType,
        Boolean foodAvailable,
        List<Long> amenityIds,
        boolean availableOnly,
        String sort
) {
}
