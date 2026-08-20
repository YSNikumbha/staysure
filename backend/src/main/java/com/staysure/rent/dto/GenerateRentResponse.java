package com.staysure.rent.dto;

import java.util.List;

public record GenerateRentResponse(
        Long propertyId,
        Integer billingMonth,
        Integer billingYear,
        int generatedCount,
        int alreadyGeneratedCount,
        int skippedCount,
        List<RentInvoiceSummaryResponse> invoices
) {
}
