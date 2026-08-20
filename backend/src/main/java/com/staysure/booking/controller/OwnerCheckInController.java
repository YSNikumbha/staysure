package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.service.BookingService;
import com.staysure.booking.service.CheckInService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner/bookings/{bookingId}/check-in")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerCheckInController {

    private final CheckInService checkInService;
    private final BookingService bookingService;

    public OwnerCheckInController(CheckInService checkInService, BookingService bookingService) {
        this.checkInService = checkInService;
        this.bookingService = bookingService;
    }

    @PatchMapping
    public ResponseEntity<ApiResponse<BookingResponse>> checkIn(@PathVariable Long bookingId,
                                                                 HttpServletRequest servletRequest) {
        checkInService.checkIn(
                SecurityUtils.currentUserId(), bookingId, RequestUtils.getClientIp(servletRequest)
        );
        Booking booking = bookingService.getOwnerBooking(SecurityUtils.currentUserId(), bookingId);
        return ResponseEntity.ok(ApiResponse.success("Tenant checked in", toResponse(booking)));
    }

    private BookingResponse toResponse(Booking booking) {
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