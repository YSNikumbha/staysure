package com.staysure.owner.dto;

import jakarta.validation.constraints.Size;

public record OwnerVerifyRequest(
        @Size(max = 1000) String remarks
) {
}
