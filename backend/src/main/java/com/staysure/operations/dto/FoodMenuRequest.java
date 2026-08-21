package com.staysure.operations.dto;

import com.staysure.operations.enums.MealType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FoodMenuRequest(
        @NotNull Long propertyId,
        @NotNull LocalDate menuDate,
        @NotNull MealType mealType,
        @NotBlank @Size(max = 5000) String items,
        @Size(max = 1000) String notes
) {
}
