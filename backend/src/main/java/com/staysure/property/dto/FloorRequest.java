package com.staysure.property.dto;

import com.staysure.property.enums.FloorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record FloorRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull Integer floorNumber,
        @Size(max = 2000) String description,
        FloorStatus status
) {
}
