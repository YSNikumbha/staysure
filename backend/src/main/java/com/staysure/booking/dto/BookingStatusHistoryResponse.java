package com.staysure.booking.dto;

import com.staysure.booking.enums.BookingStatus;

import java.time.LocalDateTime;

public record BookingStatusHistoryResponse(
        Long id,
        BookingStatus previousStatus,
        BookingStatus newStatus,
        String remarks,
        Long actionBy,
        LocalDateTime createdAt
) {
}
