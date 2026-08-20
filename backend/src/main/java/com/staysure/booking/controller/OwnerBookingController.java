package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/bookings")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerBookingController {

    private final BookingService bookingService;

    public OwnerBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> listBookings() {
        List<Booking> bookings = bookingService.getOwnerBookings(SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Bookings loaded", bookings.stream().map(this::toResponse).toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        Booking booking = bookingService.getOwnerBooking(SecurityUtils.currentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking loaded", toResponse(booking)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<BookingResponse>> approveBooking(@PathVariable Long id,
                                                                       @RequestParam(required = false) String remarks,
                                                                       HttpServletRequest servletRequest) {
        Booking booking = bookingService.approveBooking(
                SecurityUtils.currentUserId(), id, remarks, RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Booking approved", toResponse(booking)));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> rejectBooking(@PathVariable Long id,
                                                                      @RequestParam String reason,
                                                                      HttpServletRequest servletRequest) {
        Booking booking = bookingService.rejectBooking(
                SecurityUtils.currentUserId(), id, reason, RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Booking rejected", toResponse(booking)));
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
