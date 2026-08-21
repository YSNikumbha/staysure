package com.staysure.operations.dto;

import com.staysure.operations.enums.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FoodMenuResponse(
        Long id,
        Long propertyId,
        String propertyName,
        LocalDate menuDate,
        MealType mealType,
        String items,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
