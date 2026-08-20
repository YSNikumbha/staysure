package com.staysure.rent.dto;

import com.staysure.booking.enums.PaymentMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RecordRentPaymentRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotNull PaymentMethod paymentMethod,
        @Size(max = 160) String paymentReference,
        @NotNull LocalDate paymentDate,
        @Size(max = 1000) String remarks
) {
}
