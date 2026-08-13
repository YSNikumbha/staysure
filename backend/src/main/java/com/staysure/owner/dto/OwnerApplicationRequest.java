package com.staysure.owner.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OwnerApplicationRequest(
        @NotBlank @Size(max = 180) String businessName,
        @Pattern(regexp = "^[0-9+()\\-\\s]{7,30}$", message = "must be a valid phone number") String alternatePhone,
        @Email @Size(max = 180) String businessEmail,
        @Min(0) @Max(60) Integer experienceYears,
        @Size(max = 3000) String description
) {
}
