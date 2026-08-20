package com.staysure.booking.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CreateBookingRequest(
        @NotNull Long propertyId,
        @NotNull Long roomId,
        @NotNull Long bedId,
        @NotNull LocalDate moveInDate,
        @NotNull LocalDate expectedMoveOutDate,
        String remarks
) {
}