package com.staysure.property.dto;

public record PropertyInventoryCountsResponse(
        long totalFloors,
        long totalRooms,
        long totalBeds,
        long availableBeds,
        long maintenanceBeds,
        long inactiveBeds
) {
}
