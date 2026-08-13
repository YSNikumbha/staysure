package com.staysure.property.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerificationDecisionRequest(
        @Size(max = 3000) String remarks
) {
    public String requiredRemarks() {
        if (remarks == null || remarks.isBlank()) {
            throw new jakarta.validation.ValidationException("remarks: must not be blank");
        }
        return remarks.trim();
    }
}
