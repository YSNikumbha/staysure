package com.staysure.operations.dto;

import java.time.LocalDateTime;

public record ComplaintCommentResponse(
        Long id,
        Long authorUserId,
        String authorName,
        String comment,
        LocalDateTime createdAt
) {
}
