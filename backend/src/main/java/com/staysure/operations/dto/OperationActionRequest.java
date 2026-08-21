package com.staysure.operations.dto;

import jakarta.validation.constraints.Size;

public record OperationActionRequest(
        @Size(max = 1000) String remarks
) {
}
