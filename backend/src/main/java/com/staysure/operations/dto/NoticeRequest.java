package com.staysure.operations.dto;

import com.staysure.operations.enums.NoticeType;
import com.staysure.operations.enums.OperationalPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record NoticeRequest(
        @NotNull Long propertyId,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 5000) String content,
        NoticeType noticeType,
        OperationalPriority priority,
        LocalDate expiresAt
) {
}
