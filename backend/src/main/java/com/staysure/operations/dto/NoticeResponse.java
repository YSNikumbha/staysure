package com.staysure.operations.dto;

import com.staysure.operations.enums.NoticeStatus;
import com.staysure.operations.enums.NoticeType;
import com.staysure.operations.enums.OperationalPriority;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoticeResponse(
        Long id,
        Long propertyId,
        String propertyName,
        String title,
        String content,
        NoticeType noticeType,
        OperationalPriority priority,
        NoticeStatus status,
        LocalDateTime publishedAt,
        LocalDate expiresAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
