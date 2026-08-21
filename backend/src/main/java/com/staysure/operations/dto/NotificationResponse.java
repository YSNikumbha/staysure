package com.staysure.operations.dto;

import com.staysure.operations.enums.NotificationType;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        NotificationType type,
        String title,
        String message,
        String referenceType,
        Long referenceId,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
