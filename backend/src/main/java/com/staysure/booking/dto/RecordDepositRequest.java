package com.staysure.booking.dto;

import com.staysure.booking.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record RecordDepositRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 160) String paymentReference,
        @Size(max = 1000) String remarks
) {
}
