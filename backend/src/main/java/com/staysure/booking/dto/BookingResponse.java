package com.staysure.booking.dto;

import com.staysure.booking.enums.BookingStatus;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(
        Long id,
        String bookingNumber,
        Long userId,
        Long propertyId,
        String propertyName,
        String propertyCity,
        PropertyStatus propertyStatus,
        PropertyVerificationStatus propertyVerificationStatus,
        Long roomId,
        String roomNumber,
        Long bedId,
        String bedNumber,
        LocalDate moveInDate,
        LocalDate expectedMoveOutDate,
        BigDecimal monthlyRent,
        BigDecimal securityDepositAmount,
        BookingStatus status,
        String userRemarks,
        String ownerRemarks,
        LocalDateTime approvedAt,
        LocalDateTime rejectedAt,
        String rejectionReason,
        LocalDateTime confirmedAt,
        LocalDateTime checkedInAt,
        LocalDateTime cancelledAt,
        LocalDateTime createdAt
) {
}