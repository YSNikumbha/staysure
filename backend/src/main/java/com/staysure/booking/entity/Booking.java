package com.staysure.booking.entity;

import com.staysure.common.entity.BaseEntity;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.property.entity.Bed;
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
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @Column(name = "booking_number", nullable = false, unique = true, length = 30)
    private String bookingNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PgProperty property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @Column(name = "move_in_date", nullable = false)
    private LocalDate moveInDate;

    @Column(name = "expected_move_out_date", nullable = false)
    private LocalDate expectedMoveOutDate;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent;

    @Column(name = "security_deposit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal securityDepositAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private BookingStatus status = BookingStatus.REQUESTED;

    @Column(name = "user_remarks", columnDefinition = "TEXT")
    private String userRemarks;

    @Column(name = "owner_remarks", columnDefinition = "TEXT")
    private String ownerRemarks;

    @Column(name = "approved_at")
    private java.time.LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "rejected_at")
    private java.time.LocalDateTime rejectedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rejected_by")
    private User rejectedBy;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "confirmed_at")
    private java.time.LocalDateTime confirmedAt;

    @Column(name = "checked_in_at")
    private java.time.LocalDateTime checkedInAt;

    @Column(name = "cancelled_at")
    private java.time.LocalDateTime cancelledAt;
}