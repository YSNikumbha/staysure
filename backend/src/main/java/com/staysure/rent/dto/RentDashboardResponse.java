package com.staysure.rent.dto;

import java.util.List;

public record RentDashboardResponse(
        RentSummaryResponse summary,
        List<RentInvoiceSummaryResponse> invoices
) {
}
