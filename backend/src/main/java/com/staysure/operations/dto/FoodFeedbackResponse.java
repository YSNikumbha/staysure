package com.staysure.operations.dto;

import com.staysure.operations.enums.MealType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record FoodFeedbackResponse(
        Long id,
        Long tenantProfileId,
        String tenantName,
        Long propertyId,
        String propertyName,
        LocalDate menuDate,
        MealType mealType,
        Integer rating,
        String comment,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
