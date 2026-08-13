package com.staysure.property.dto;

import com.staysure.property.enums.BedStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BedRequest(
        @NotBlank @Size(max = 50) String bedNumber,
        @Size(max = 120) String bedLabel,
        BedStatus status
) {
}
