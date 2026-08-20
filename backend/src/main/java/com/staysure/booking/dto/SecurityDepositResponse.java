package com.staysure.booking.dto;

import com.staysure.booking.enums.DepositStatus;
import com.staysure.booking.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SecurityDepositResponse(
        Long id,
        Long bookingId,
        BigDecimal requiredAmount,
        BigDecimal paidAmount,
        BigDecimal remainingAmount,
        DepositStatus status,
        PaymentMethod lastPaymentMethod,
        String lastPaymentReference,
        String remarks,
        LocalDateTime paidAt
) {
}
