package com.staysure.property.entity;

import com.staysure.common.entity.BaseEntity;
import com.staysure.property.enums.FurnishingType;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.enums.SharingType;
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

@Getter
@Setter
@Entity
@Table(name = "rooms")
public class Room extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "floor_id", nullable = false)
    private Floor floor;

    @Column(name = "room_number", nullable = false, length = 50)
    private String roomNumber;

    @Column(name = "room_name", length = 120)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sharing_type", nullable = false, length = 40)
    private SharingType sharingType;

    @Column(nullable = false)
    private Integer capacity;

    @Column(name = "monthly_rent", nullable = false, precision = 12, scale = 2)
    private BigDecimal monthlyRent = BigDecimal.ZERO;

    @Column(name = "security_deposit", nullable = false, precision = 12, scale = 2)
    private BigDecimal securityDeposit = BigDecimal.ZERO;

    @Column(name = "ac_available", nullable = false)
    private boolean acAvailable;

    @Column(name = "attached_bathroom", nullable = false)
    private boolean attachedBathroom;

    @Enumerated(EnumType.STRING)
    @Column(name = "furnishing_type", nullable = false, length = 40)
    private FurnishingType furnishingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RoomStatus status = RoomStatus.ACTIVE;

    @Column(columnDefinition = "TEXT")
    private String description;
}
