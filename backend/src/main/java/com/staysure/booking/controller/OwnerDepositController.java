package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.enums.PaymentMethod;
import com.staysure.booking.service.BookingService;
import com.staysure.booking.service.SecurityDepositService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/owner/bookings/{bookingId}/deposit")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerDepositController {

    private final SecurityDepositService securityDepositService;
    private final BookingService bookingService;

    public OwnerDepositController(SecurityDepositService securityDepositService, BookingService bookingService) {
        this.securityDepositService = securityDepositService;
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> recordDeposit(@PathVariable Long bookingId,
                                                                      @RequestParam BigDecimal amount,
                                                                      @RequestParam PaymentMethod paymentMethod,
                                                                      @RequestParam(required = false) String paymentReference,
                                                                      @RequestParam(required = false) String remarks,
                                                                      HttpServletRequest servletRequest) {
        securityDepositService.recordDeposit(
                SecurityUtils.currentUserId(), bookingId, amount, paymentMethod, paymentReference, remarks, RequestUtils.getClientIp(servletRequest)
        );
        Booking booking = bookingService.getOwnerBooking(SecurityUtils.currentUserId(), bookingId);
        return ResponseEntity.ok(ApiResponse.success("Deposit recorded", toResponse(booking)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<SecurityDeposit>> getDeposit(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Deposit loaded", securityDepositService.getDeposit(
                SecurityUtils.currentUserId(), bookingId
        )));
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