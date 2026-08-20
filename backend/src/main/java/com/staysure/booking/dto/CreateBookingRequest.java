package com.staysure.booking.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateBookingRequest(
        @NotNull Long propertyId,
        @NotNull Long roomId,
        @NotNull Long bedId,
        @NotNull @FutureOrPresent LocalDate moveInDate,
        LocalDate expectedMoveOutDate,
        @Size(max = 1000) String remarks
) {
}
