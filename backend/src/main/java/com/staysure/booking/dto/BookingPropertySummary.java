package com.staysure.booking.dto;

public record BookingPropertySummary(
        Long id,
        String slug,
        String name,
        String area,
        String city,
        String addressLine1
) {
}
