package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.service.RentalAgreementService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/bookings/{bookingId}/agreement")
@PreAuthorize("hasRole('USER')")
public class UserAgreementController {

    private final RentalAgreementService rentalAgreementService;

    public UserAgreementController(RentalAgreementService rentalAgreementService) {
        this.rentalAgreementService = rentalAgreementService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<RentalAgreement>> getAgreement(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Agreement loaded", rentalAgreementService.getAgreement(
                SecurityUtils.currentUserId(), bookingId
        )));
    }

    @PatchMapping("/accept")
    public ResponseEntity<ApiResponse<RentalAgreement>> acceptAgreement(@PathVariable Long bookingId,
                                                                        jakarta.servlet.http.HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Agreement accepted", rentalAgreementService.acceptAgreement(
                SecurityUtils.currentUserId(), bookingId, RequestUtils.getClientIp(servletRequest)
        )));
    }
}
