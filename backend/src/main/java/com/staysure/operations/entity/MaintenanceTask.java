package com.staysure.operations.entity;

import com.staysure.common.entity.BaseEntity;
import com.staysure.operations.enums.MaintenanceStatus;
import com.staysure.operations.enums.OperationalPriority;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
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

@Getter
@Setter
@Entity
@Table(name = "maintenance_tasks")
public class MaintenanceTask extends BaseEntity {

    @Column(name = "task_number", nullable = false, unique = true, length = 50)
    private String taskNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "complaint_id")
    private Complaint complaint;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PgProperty property;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    @Column(nullable = false, length = 180)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OperationalPriority priority = OperationalPriority.MEDIUM;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private MaintenanceStatus status = MaintenanceStatus.PENDING;

    @Column(name = "assigned_to_text", length = 160)
    private String assignedToText;

    @Column(name = "scheduled_date")
    private LocalDate scheduledDate;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(columnDefinition = "TEXT")
    private String remarks;
}
