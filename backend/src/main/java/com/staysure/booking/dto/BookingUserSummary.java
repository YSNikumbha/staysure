package com.staysure.booking.dto;

public record BookingUserSummary(
        Long id,
        String firstName,
        String lastName,
        String phone,
        String email
) {
}
