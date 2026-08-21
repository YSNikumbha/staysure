package com.staysure.operations.dto;

import com.staysure.operations.enums.VisitorStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record VisitorResponse(
        Long id,
        String visitorNumber,
        Long tenantProfileId,
        String tenantName,
        Long propertyId,
        String propertyName,
        String visitorName,
        String visitorPhone,
        String relationship,
        LocalDate visitDate,
        LocalTime expectedArrivalTime,
        LocalTime expectedDepartureTime,
        LocalDateTime actualArrivalTime,
        LocalDateTime actualDepartureTime,
        String purpose,
        VisitorStatus status,
        String rejectionReason,
        LocalDateTime approvedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
