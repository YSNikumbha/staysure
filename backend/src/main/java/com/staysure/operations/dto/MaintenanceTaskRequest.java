package com.staysure.operations.dto;

import com.staysure.operations.enums.OperationalPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record MaintenanceTaskRequest(
        @NotNull Long propertyId,
        Long complaintId,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 5000) String description,
        OperationalPriority priority,
        @Size(max = 160) String assignedToText,
        LocalDate scheduledDate,
        @Size(max = 1000) String remarks
) {
}
