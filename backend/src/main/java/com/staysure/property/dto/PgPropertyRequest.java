package com.staysure.property.dto;

import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalTime;

public record PgPropertyRequest(
        @NotBlank @Size(max = 180) String name,
        @Size(max = 3000) String description,
        @NotNull GenderType genderType,
        @NotNull PropertyType propertyType,
        @NotBlank @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @NotBlank @Size(max = 120) String area,
        @NotBlank @Size(max = 100) String city,
        @NotBlank @Size(max = 100) String state,
        @NotBlank @Pattern(regexp = "^[1-9][0-9]{5}$", message = "must be a valid 6 digit pincode") String pincode,
        @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") BigDecimal latitude,
        @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") BigDecimal longitude,
        @NotNull @DecimalMin(value = "0.0") BigDecimal startingRent,
        @NotNull @DecimalMin(value = "0.0") BigDecimal securityDeposit,
        @NotNull @Min(0) Integer noticePeriodDays,
        @NotNull @Min(0) Integer lockInMonths,
        LocalTime entryTime,
        boolean foodAvailable,
        PropertyStatus status,
        @Valid PropertyRuleRequest rules
) {
}
