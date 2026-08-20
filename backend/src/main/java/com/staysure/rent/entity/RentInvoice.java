package com.staysure.rent.entity;

import com.staysure.booking.entity.TenantProfile;
import com.staysure.common.entity.BaseEntity;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.rent.enums.RentInvoiceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(
        name = "rent_invoices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_rent_invoices_invoice_number", columnNames = "invoice_number"),
                @UniqueConstraint(name = "uk_rent_invoice_tenant_month", columnNames = {"tenant_profile_id", "billing_month", "billing_year"})
        }
)
public class RentInvoice extends BaseEntity {

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_profile_id", nullable = false)
    private TenantProfile tenantProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PgProperty property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @Column(name = "billing_month", nullable = false)
    private Integer billingMonth;

    @Column(name = "billing_year", nullable = false)
    private Integer billingYear;

    @Column(name = "base_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal baseRent = BigDecimal.ZERO;

    @Column(name = "maintenance_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal maintenanceCharge = BigDecimal.ZERO;

    @Column(name = "electricity_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal electricityCharge = BigDecimal.ZERO;

    @Column(name = "other_charge", nullable = false, precision = 12, scale = 2)
    private BigDecimal otherCharge = BigDecimal.ZERO;

    @Column(name = "late_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Column(name = "balance_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAmount = BigDecimal.ZERO;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RentInvoiceStatus status = RentInvoiceStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}
