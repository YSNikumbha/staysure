package com.staysure.property.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record PropertyRuleRequest(
        boolean visitorAllowed,
        boolean smokingAllowed,
        boolean alcoholAllowed,
        boolean cookingAllowed,
        LocalTime gateClosingTime,
        boolean lateEntryAllowed,
        @Min(0) Integer noticePeriodDays,
        @Size(max = 3000) String additionalRules
) {
}
