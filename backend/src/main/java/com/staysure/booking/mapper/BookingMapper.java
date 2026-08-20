package com.staysure.booking.mapper;

import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.entity.Booking;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getUser().getId(),
                booking.getProperty().getId(),
                booking.getProperty().getName(),
                booking.getProperty().getCity(),
                booking.getProperty().getStatus(),
                booking.getProperty().getVerificationStatus(),
                booking.getRoom().getId(),
                booking.getRoom().getRoomNumber(),
                booking.getBed().getId(),
                booking.getBed().getBedNumber(),
                booking.getMoveInDate(),
                booking.getExpectedMoveOutDate(),
                booking.getMonthlyRent(),
                booking.getSecurityDepositAmount(),
                booking.getStatus(),
                booking.getUserRemarks(),
                booking.getOwnerRemarks(),
                booking.getApprovedAt(),
                booking.getRejectedAt(),
                booking.getRejectionReason(),
                booking.getConfirmedAt(),
                booking.getCheckedInAt(),
                booking.getCancelledAt(),
                booking.getCreatedAt()
        );
    }
}