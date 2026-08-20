package com.staysure.booking.entity;

import com.staysure.booking.enums.AgreementStatus;
import com.staysure.common.entity.BaseEntity;
import com.staysure.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "rental_agreements")
public class RentalAgreement extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @Column(name = "agreement_number", nullable = false, unique = true, length = 40)
    private String agreementNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AgreementStatus status = AgreementStatus.ISSUED;

    @Column(name = "document_url", length = 600)
    private String documentUrl;

    @Column(name = "original_file_name", length = 255)
    private String originalFileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(columnDefinition = "TEXT")
    private String terms;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent = BigDecimal.ZERO;

    @Column(name = "security_deposit", nullable = false, precision = 12, scale = 2)
    private BigDecimal securityDeposit = BigDecimal.ZERO;

    @Column(name = "notice_period_days", nullable = false)
    private Integer noticePeriodDays = 0;

    @Column(name = "lock_in_months", nullable = false)
    private Integer lockInMonths = 0;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;
}
