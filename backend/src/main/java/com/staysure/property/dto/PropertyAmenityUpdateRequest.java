package com.staysure.property.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record PropertyAmenityUpdateRequest(
        @NotNull Set<Long> amenityIds
) {
}
