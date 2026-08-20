package com.staysure.rent.dto;

import com.staysure.booking.dto.SecurityDepositResponse;

import java.math.BigDecimal;

public record RentSummaryResponse(
        BigDecimal totalRent,
        BigDecimal collected,
        BigDecimal outstanding,
        BigDecimal overdueAmount,
        long pendingInvoices,
        long overdueInvoices,
        SecurityDepositResponse securityDeposit
) {
}
