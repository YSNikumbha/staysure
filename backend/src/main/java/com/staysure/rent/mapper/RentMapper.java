package com.staysure.rent.mapper;

import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.mapper.BookingMapper;
import com.staysure.rent.dto.RentDashboardResponse;
import com.staysure.rent.dto.RentInvoiceDetailResponse;
import com.staysure.rent.dto.RentInvoiceSummaryResponse;
import com.staysure.rent.dto.RentPaymentResponse;
import com.staysure.rent.dto.RentSummaryResponse;
import com.staysure.rent.entity.RentInvoice;
import com.staysure.rent.entity.RentPayment;
import com.staysure.rent.enums.RentInvoiceStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
public class RentMapper {

    private final BookingMapper bookingMapper;

    public RentMapper(BookingMapper bookingMapper) {
        this.bookingMapper = bookingMapper;
    }

    public RentDashboardResponse toDashboard(List<RentInvoice> invoices, SecurityDeposit securityDeposit) {
        BigDecimal totalRent = BigDecimal.ZERO;
        BigDecimal collected = BigDecimal.ZERO;
        BigDecimal outstanding = BigDecimal.ZERO;
        BigDecimal overdueAmount = BigDecimal.ZERO;
        long pendingInvoices = 0;
        long overdueInvoices = 0;

        for (RentInvoice invoice : invoices) {
            RentInvoiceStatus status = effectiveStatus(invoice);
            totalRent = totalRent.add(safe(invoice.getTotalAmount()));
            collected = collected.add(safe(invoice.getPaidAmount()));
            outstanding = outstanding.add(safe(invoice.getBalanceAmount()));
            if (status == RentInvoiceStatus.OVERDUE) {
                overdueAmount = overdueAmount.add(safe(invoice.getBalanceAmount()));
                overdueInvoices++;
            }
            if (status == RentInvoiceStatus.PENDING || status == RentInvoiceStatus.PARTIALLY_PAID) {
                pendingInvoices++;
            }
        }

        return new RentDashboardResponse(
                new RentSummaryResponse(
                        totalRent,
                        collected,
                        outstanding,
                        overdueAmount,
                        pendingInvoices,
                        overdueInvoices,
                        bookingMapper.toDepositResponse(securityDeposit)
                ),
                invoices.stream().map(this::toSummary).toList()
        );
    }

    public RentInvoiceSummaryResponse toSummary(RentInvoice invoice) {
        return new RentInvoiceSummaryResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getTenantProfile().getId(),
                tenantName(invoice),
                invoice.getProperty().getId(),
                invoice.getProperty().getName(),
                invoice.getRoom().getRoomNumber(),
                bedLabel(invoice),
                invoice.getBillingMonth(),
                invoice.getBillingYear(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getBalanceAmount(),
                invoice.getDueDate(),
                effectiveStatus(invoice)
        );
    }

    public RentInvoiceDetailResponse toDetail(RentInvoice invoice,
                                              List<RentPayment> payments,
                                              SecurityDeposit securityDeposit) {
        return new RentInvoiceDetailResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getTenantProfile().getId(),
                tenantName(invoice),
                invoice.getProperty().getId(),
                invoice.getProperty().getName(),
                invoice.getRoom().getRoomNumber(),
                bedLabel(invoice),
                invoice.getBillingMonth(),
                invoice.getBillingYear(),
                invoice.getBaseRent(),
                invoice.getMaintenanceCharge(),
                invoice.getElectricityCharge(),
                invoice.getOtherCharge(),
                invoice.getLateFee(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getBalanceAmount(),
                invoice.getDueDate(),
                effectiveStatus(invoice),
                invoice.getNotes(),
                invoice.getGeneratedAt(),
                bookingMapper.toDepositResponse(securityDeposit),
                payments.stream().map(this::toPayment).toList()
        );
    }

    public RentPaymentResponse toPayment(RentPayment payment) {
        return new RentPaymentResponse(
                payment.getId(),
                payment.getPaymentNumber(),
                payment.getRentInvoice().getId(),
                payment.getRentInvoice().getInvoiceNumber(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentReference(),
                payment.getPaymentDate(),
                payment.getRemarks(),
                payment.getCreatedAt()
        );
    }

    public RentInvoiceStatus effectiveStatus(RentInvoice invoice) {
        if (invoice.getStatus() == RentInvoiceStatus.CANCELLED || invoice.getStatus() == RentInvoiceStatus.PAID) {
            return invoice.getStatus();
        }
        if (safe(invoice.getBalanceAmount()).compareTo(BigDecimal.ZERO) <= 0) {
            return RentInvoiceStatus.PAID;
        }
        if (invoice.getDueDate() != null && LocalDate.now().isAfter(invoice.getDueDate())) {
            return RentInvoiceStatus.OVERDUE;
        }
        if (safe(invoice.getPaidAmount()).compareTo(BigDecimal.ZERO) > 0) {
            return RentInvoiceStatus.PARTIALLY_PAID;
        }
        return RentInvoiceStatus.PENDING;
    }

    private String tenantName(RentInvoice invoice) {
        return invoice.getTenantProfile().getUser().getFirstName() + " " + invoice.getTenantProfile().getUser().getLastName();
    }

    private String bedLabel(RentInvoice invoice) {
        return invoice.getBed().getBedLabel() == null || invoice.getBed().getBedLabel().isBlank()
                ? invoice.getBed().getBedNumber()
                : invoice.getBed().getBedLabel();
    }

    private BigDecimal safe(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
