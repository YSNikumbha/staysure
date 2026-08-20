package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.dto.CreateBookingRequest;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.service.BookingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/bookings")
@PreAuthorize("hasRole('USER')")
public class UserBookingController {

    private final BookingService bookingService;

    public UserBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(@Valid @RequestBody CreateBookingRequest request,
                                                                      HttpServletRequest servletRequest) {
        Booking booking = bookingService.createBooking(
                SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Booking created", toResponse(booking)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> listBookings() {
        List<Booking> bookings = bookingService.getUserBookings(SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Bookings loaded", bookings.stream().map(this::toResponse).toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBooking(@PathVariable Long id) {
        Booking booking = bookingService.getUserBooking(SecurityUtils.currentUserId(), id);
        return ResponseEntity.ok(ApiResponse.success("Booking loaded", toResponse(booking)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelBooking(@PathVariable Long id, HttpServletRequest servletRequest) {
        bookingService.cancelBooking(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled"));
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
