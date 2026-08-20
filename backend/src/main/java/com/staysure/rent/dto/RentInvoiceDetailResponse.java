package com.staysure.rent.dto;

import com.staysure.booking.dto.SecurityDepositResponse;
import com.staysure.rent.enums.RentInvoiceStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RentInvoiceDetailResponse(
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
        BigDecimal baseRent,
        BigDecimal maintenanceCharge,
        BigDecimal electricityCharge,
        BigDecimal otherCharge,
        BigDecimal lateFee,
        BigDecimal totalAmount,
        BigDecimal paidAmount,
        BigDecimal balanceAmount,
        LocalDate dueDate,
        RentInvoiceStatus status,
        String notes,
        LocalDateTime generatedAt,
        SecurityDepositResponse securityDeposit,
        List<RentPaymentResponse> payments
) {
}
