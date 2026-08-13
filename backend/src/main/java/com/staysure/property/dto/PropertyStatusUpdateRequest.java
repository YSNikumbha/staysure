package com.staysure.property.dto;

import com.staysure.property.enums.PropertyStatus;
import jakarta.validation.constraints.NotNull;

public record PropertyStatusUpdateRequest(
        @NotNull PropertyStatus status
) {
}
