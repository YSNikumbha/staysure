package com.staysure.owner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OwnerSuspendRequest(
        @NotBlank @Size(max = 1000) String reason
) {
}
