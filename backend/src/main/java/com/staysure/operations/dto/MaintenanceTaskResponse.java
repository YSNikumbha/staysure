package com.staysure.operations.dto;

import com.staysure.operations.enums.MaintenanceStatus;
import com.staysure.operations.enums.OperationalPriority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MaintenanceTaskResponse(
        Long id,
        String taskNumber,
        Long complaintId,
        String complaintNumber,
        Long propertyId,
        String propertyName,
        Long roomId,
        String roomNumber,
        String title,
        String description,
        OperationalPriority priority,
        MaintenanceStatus status,
        String assignedToText,
        LocalDate scheduledDate,
        LocalDateTime completedAt,
        String remarks,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
