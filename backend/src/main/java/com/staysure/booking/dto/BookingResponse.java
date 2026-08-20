package com.staysure.booking.dto;

import com.staysure.booking.enums.BookingStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record BookingResponse(
        Long id,
        String bookingNumber,
        BookingStatus status,
        BookingUserSummary user,
        BookingPropertySummary property,
        BookingRoomSummary room,
        BookingBedSummary bed,
        LocalDate moveInDate,
        LocalDate expectedMoveOutDate,
        BigDecimal monthlyRent,
        BigDecimal securityDeposit,
        LocalDateTime requestedAt,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        LocalDateTime cancelledAt,
        LocalDateTime confirmedAt,
        LocalDateTime checkedInAt,
        String rejectionReason,
        String cancellationReason,
        String remarks,
        List<TenantDocumentResponse> documents,
        SecurityDepositResponse deposit,
        RentalAgreementResponse agreement,
        TenantProfileResponse tenant,
        List<BookingStatusHistoryResponse> history
) {
}
