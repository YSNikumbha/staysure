package com.staysure.booking.dto;

import com.staysure.booking.enums.AgreementStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record RentalAgreementResponse(
        Long id,
        Long bookingId,
        String agreementNumber,
        AgreementStatus status,
        String documentUrl,
        String originalFileName,
        String terms,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyRent,
        BigDecimal securityDeposit,
        Integer noticePeriodDays,
        Integer lockInMonths,
        LocalDateTime issuedAt,
        LocalDateTime acceptedAt
) {
}
