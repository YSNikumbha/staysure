package com.staysure.operations.dto;

import com.staysure.operations.enums.ComplaintStatus;

import java.time.LocalDateTime;

public record ComplaintHistoryResponse(
        Long id,
        ComplaintStatus previousStatus,
        ComplaintStatus newStatus,
        String remarks,
        Long changedBy,
        LocalDateTime createdAt
) {
}
