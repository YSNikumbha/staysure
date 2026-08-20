package com.staysure.booking.dto;

import jakarta.validation.constraints.Size;

public record DocumentReviewRequest(
        @Size(max = 1000) String remarks
) {
}
