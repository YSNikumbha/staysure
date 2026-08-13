package com.staysure.property.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;

public record PropertyRuleResponse(
        Long id,
        Long propertyId,
        boolean visitorAllowed,
        boolean smokingAllowed,
        boolean alcoholAllowed,
        boolean cookingAllowed,
        LocalTime gateClosingTime,
        boolean lateEntryAllowed,
        Integer noticePeriodDays,
        String additionalRules,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
