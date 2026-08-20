package com.staysure.booking.entity;

import com.staysure.common.entity.BaseEntity;
import com.staysure.booking.enums.AgreementStatus;
import com.staysure.property.entity.PgProperty;
import com.staysure.user.entity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "rental_agreements")
public class RentalAgreement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PgProperty property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "agreement_number", nullable = false, unique = true, length = 30)
    private String agreementNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "security_deposit", nullable = false, precision = 12, scale = 2)
    private BigDecimal securityDeposit;

    @Column(name = "notice_period_days", nullable = false)
    private Integer noticePeriodDays;

    @Column(name = "lock_in_months", nullable = false)
    private Integer lockInMonths;

    @Column(name = "terms_and_conditions", columnDefinition = "TEXT")
    private String termsAndConditions;

    @Column(name = "agreement_file_url", length = 500)
    private String agreementFileUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AgreementStatus status = AgreementStatus.DRAFT;
}