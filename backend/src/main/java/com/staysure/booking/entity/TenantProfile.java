package com.staysure.booking.entity;

import com.staysure.common.entity.BaseEntity;
import com.staysure.booking.enums.TenantStatus;
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

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tenant_profiles")
public class TenantProfile extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PgProperty property;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bed_id", nullable = false)
    private Bed bed;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TenantStatus status = TenantStatus.UPCOMING;

    @Column(name = "joining_date")
    private LocalDateTime joiningDate;

    @Column(name = "expected_checkout_date")
    private LocalDateTime expectedCheckoutDate;

    @Column(name = "emergency_contact_name", length = 120)
    private String emergencyContactName;

    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;

    @Column(name = "college_or_company", length = 180)
    private String collegeOrCompany;

    @Column(name = "occupation_type", length = 60)
    private String occupationType;
}