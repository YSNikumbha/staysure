package com.staysure.property.dto;

public record OwnerDashboardStats(
        long totalPgs,
        long activePgs,
        long totalRooms,
        long totalBeds,
        long availableBeds
) {
}
