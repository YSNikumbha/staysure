package com.staysure.operations.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ComplaintCommentRequest(
        @NotBlank @Size(max = 2000) String comment
) {
}
