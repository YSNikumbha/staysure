package com.staysure.operations.entity;

import com.staysure.booking.entity.TenantProfile;
import com.staysure.common.entity.BaseEntity;
import com.staysure.operations.enums.VisitorStatus;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "visitor_entries")
public class VisitorEntry extends BaseEntity {

    @Column(name = "visitor_number", nullable = false, unique = true, length = 50)
    private String visitorNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tenant_profile_id", nullable = false)
    private TenantProfile tenantProfile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PgProperty property;

    @Column(name = "visitor_name", nullable = false, length = 140)
    private String visitorName;

    @Column(name = "visitor_phone", nullable = false, length = 30)
    private String visitorPhone;

    @Column(nullable = false, length = 80)
    private String relationship;

    @Column(name = "visit_date", nullable = false)
    private LocalDate visitDate;

    @Column(name = "expected_arrival_time", nullable = false)
    private LocalTime expectedArrivalTime;

    @Column(name = "expected_departure_time", nullable = false)
    private LocalTime expectedDepartureTime;

    @Column(name = "actual_arrival_time")
    private LocalDateTime actualArrivalTime;

    @Column(name = "actual_departure_time")
    private LocalDateTime actualDepartureTime;

    @Column(nullable = false, length = 300)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VisitorStatus status = VisitorStatus.REQUESTED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;
}
