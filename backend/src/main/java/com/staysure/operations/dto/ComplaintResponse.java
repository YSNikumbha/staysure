package com.staysure.operations.dto;

import com.staysure.operations.enums.ComplaintCategory;
import com.staysure.operations.enums.ComplaintStatus;
import com.staysure.operations.enums.OperationalPriority;

import java.time.LocalDateTime;
import java.util.List;

public record ComplaintResponse(
        Long id,
        String complaintNumber,
        Long tenantProfileId,
        String tenantName,
        Long propertyId,
        String propertyName,
        Long roomId,
        String roomNumber,
        ComplaintCategory category,
        String title,
        String description,
        OperationalPriority priority,
        ComplaintStatus status,
        LocalDateTime resolvedAt,
        LocalDateTime closedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<ComplaintCommentResponse> comments,
        List<ComplaintHistoryResponse> history
) {
}
