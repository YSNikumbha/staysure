package com.staysure.rent.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record GenerateRentRequest(
        @NotNull Long propertyId,
        @NotNull @Min(1) @Max(12) Integer billingMonth,
        @NotNull @Min(2000) @Max(2100) Integer billingYear
) {
}
