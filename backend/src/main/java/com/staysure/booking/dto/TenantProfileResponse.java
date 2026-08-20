package com.staysure.booking.dto;

import com.staysure.booking.enums.TenantStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TenantProfileResponse(
        Long id,
        Long bookingId,
        BookingUserSummary user,
        BookingPropertySummary property,
        BookingRoomSummary room,
        BookingBedSummary bed,
        TenantStatus status,
        LocalDateTime joiningDate,
        LocalDate expectedCheckoutDate,
        LocalDateTime createdAt
) {
}
