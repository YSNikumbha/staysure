package com.staysure.rent.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateRentChargesRequest(
        @DecimalMin(value = "0.0") BigDecimal maintenanceCharge,
        @DecimalMin(value = "0.0") BigDecimal electricityCharge,
        @DecimalMin(value = "0.0") BigDecimal otherCharge,
        @DecimalMin(value = "0.0") BigDecimal lateFee,
        @Size(max = 1000) String notes
) {
}
