package com.staysure.operations.dto;

import com.staysure.operations.enums.ComplaintCategory;
import com.staysure.operations.enums.OperationalPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ComplaintRequest(
        @NotNull ComplaintCategory category,
        @NotBlank @Size(max = 180) String title,
        @NotBlank @Size(max = 5000) String description,
        OperationalPriority priority
) {
}
