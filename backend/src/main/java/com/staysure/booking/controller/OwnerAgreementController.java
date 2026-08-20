package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.service.BookingService;
import com.staysure.booking.service.RentalAgreementService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/owner/bookings/{bookingId}/agreement")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerAgreementController {

    private final RentalAgreementService rentalAgreementService;
    private final BookingService bookingService;

    public OwnerAgreementController(RentalAgreementService rentalAgreementService, BookingService bookingService) {
        this.rentalAgreementService = rentalAgreementService;
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createAgreement(@PathVariable Long bookingId,
                                                                        HttpServletRequest servletRequest) {
        rentalAgreementService.createAgreement(
                SecurityUtils.currentUserId(), bookingId, RequestUtils.getClientIp(servletRequest)
        );
        Booking booking = bookingService.getOwnerBooking(SecurityUtils.currentUserId(), bookingId);
        return ResponseEntity.ok(ApiResponse.success("Agreement created", toResponse(booking)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RentalAgreement>> getAgreement(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Agreement loaded", rentalAgreementService.getAgreement(
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