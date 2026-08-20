package com.staysure.rent.dto;

import com.staysure.rent.enums.RentInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RentInvoiceSummaryResponse(
        Long id,
        String invoiceNumber,
        Long tenantProfileId,
        String tenantName,
        Long propertyId,
        String propertyName,
        String roomNumber,
        String bedLabel,
        Integer billingMonth,
        Integer billingYear,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        LocalDate dueDate,
        RentInvoiceStatus status
) {
}
