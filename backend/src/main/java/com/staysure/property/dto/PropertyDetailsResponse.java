package com.staysure.property.dto;

import java.util.List;

public record PropertyDetailsResponse(
        PgPropertyResponse property,
        PropertyRuleResponse rules,
        List<AmenityResponse> amenities,
        List<PgImageResponse> images,
        List<FloorResponse> floors,
        long roomCount,
        long bedCount,
        long availableBedCount,
        PropertyInventoryCountsResponse counts
) {
}
