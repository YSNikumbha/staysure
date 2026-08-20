package com.staysure.booking.entity;

import com.staysure.booking.enums.DepositStatus;
import com.staysure.booking.enums.PaymentMethod;
import com.staysure.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "security_deposits")
public class SecurityDeposit extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "required_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal requiredAmount = BigDecimal.ZERO;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DepositStatus status = DepositStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "last_payment_method", length = 40)
    private PaymentMethod lastPaymentMethod;

    @Column(name = "last_payment_reference", length = 160)
    private String lastPaymentReference;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;
}
