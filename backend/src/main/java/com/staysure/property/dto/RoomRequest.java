package com.staysure.property.dto;

import com.staysure.property.enums.FurnishingType;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.enums.SharingType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RoomRequest(
        @NotBlank @Size(max = 50) String roomNumber,
        @Size(max = 120) String roomName,
        @NotNull SharingType sharingType,
        @NotNull @Min(1) Integer capacity,
        @NotNull @DecimalMin("0.0") BigDecimal monthlyRent,
        @NotNull @DecimalMin("0.0") BigDecimal securityDeposit,
        boolean acAvailable,
        boolean attachedBathroom,
        @NotNull FurnishingType furnishingType,
        RoomStatus status,
        @Size(max = 2000) String description
) {
}
