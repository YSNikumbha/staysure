package com.staysure.booking.dto;

import jakarta.validation.constraints.Size;

public record BookingActionRequest(
        @Size(max = 1000) String remarks
) {
}
