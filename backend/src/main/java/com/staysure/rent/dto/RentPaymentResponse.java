package com.staysure.rent.dto;

import com.staysure.booking.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RentPaymentResponse(
        Long id,
        String paymentNumber,
        Long invoiceId,
        String invoiceNumber,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        String paymentReference,
        LocalDate paymentDate,
        String remarks,
        LocalDateTime createdAt
) {
}
