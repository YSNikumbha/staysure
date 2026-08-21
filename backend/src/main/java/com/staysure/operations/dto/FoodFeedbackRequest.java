package com.staysure.operations.dto;

import com.staysure.operations.enums.MealType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record FoodFeedbackRequest(
        @NotNull LocalDate menuDate,
        @NotNull MealType mealType,
        @NotNull @Min(1) @Max(5) Integer rating,
        @Size(max = 1000) String comment
) {
}
