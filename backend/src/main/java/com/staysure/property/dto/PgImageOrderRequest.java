package com.staysure.property.dto;

import jakarta.validation.constraints.NotNull;

public record PgImageOrderRequest(
        @NotNull Long imageId,
        @NotNull Integer sortOrder
) {
}
