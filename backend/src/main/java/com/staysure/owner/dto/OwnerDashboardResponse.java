package com.staysure.owner.dto;

import com.staysure.common.enums.OwnerVerificationStatus;

public record OwnerDashboardResponse(
        Long ownerId,
        String businessName,
        OwnerVerificationStatus verificationStatus,
        long totalPgs,
        long activePgs,
        long totalRooms,
        long totalBeds,
        long availableBeds
) {
}
