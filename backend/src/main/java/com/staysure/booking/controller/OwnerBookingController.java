package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.booking.dto.BookingActionRequest;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.dto.DocumentReviewRequest;
import com.staysure.booking.dto.RecordDepositRequest;
import com.staysure.booking.dto.RentalAgreementResponse;
import com.staysure.booking.dto.SecurityDepositResponse;
import com.staysure.booking.dto.TenantDocumentResponse;
import com.staysure.booking.service.BookingService;
import com.staysure.booking.service.CheckInService;
import com.staysure.booking.service.RentalAgreementService;
import com.staysure.booking.service.SecurityDepositService;
import com.staysure.booking.service.TenantDocumentService;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/bookings")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerBookingController {

    private final BookingService bookingService;
    private final TenantDocumentService tenantDocumentService;
    private final SecurityDepositService securityDepositService;
    private final RentalAgreementService rentalAgreementService;
    private final CheckInService checkInService;

    public OwnerBookingController(BookingService bookingService,
                                  TenantDocumentService tenantDocumentService,
                                  SecurityDepositService securityDepositService,
                                  RentalAgreementService rentalAgreementService,
                                  CheckInService checkInService) {
        this.bookingService = bookingService;
        this.tenantDocumentService = tenantDocumentService;
        this.securityDepositService = securityDepositService;
        this.rentalAgreementService = rentalAgreementService;
        this.checkInService = checkInService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BookingResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Owner bookings loaded",
                bookingService.listForOwner(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Owner booking loaded",
                bookingService.getForOwner(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<BookingResponse>> approve(@PathVariable Long id,
                                                                @RequestBody(required = false) BookingActionRequest request,
                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Booking approved",
                bookingService.approve(SecurityUtils.currentUserId(), id, request == null ? null : request.remarks(),
                        RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<BookingResponse>> reject(@PathVariable Long id,
                                                               @Valid @RequestBody DocumentReviewRequest request,
                                                               HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Booking rejected",
                bookingService.reject(SecurityUtils.currentUserId(), id, request.remarks(),
                        RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/{id}/documents")
    public ResponseEntity<ApiResponse<List<TenantDocumentResponse>>> documents(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("KYC documents loaded",
                tenantDocumentService.listForOwner(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}/documents/{documentId}/verify")
    public ResponseEntity<ApiResponse<TenantDocumentResponse>> verifyDocument(@PathVariable Long id,
                                                                              @PathVariable Long documentId,
                                                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("KYC document verified",
                tenantDocumentService.verify(SecurityUtils.currentUserId(), id, documentId,
                        RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/documents/{documentId}/reject")
    public ResponseEntity<ApiResponse<TenantDocumentResponse>> rejectDocument(@PathVariable Long id,
                                                                              @PathVariable Long documentId,
                                                                              @Valid @RequestBody DocumentReviewRequest request,
                                                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("KYC document rejected",
                tenantDocumentService.reject(SecurityUtils.currentUserId(), id, documentId, request.remarks(),
                        RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<SecurityDepositResponse>> deposit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Security deposit loaded",
                securityDepositService.getForOwner(SecurityUtils.currentUserId(), id)));
    }

    @PostMapping("/{id}/deposit")
    public ResponseEntity<ApiResponse<SecurityDepositResponse>> recordDeposit(@PathVariable Long id,
                                                                              @Valid @RequestBody RecordDepositRequest request,
                                                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Security deposit recorded",
                securityDepositService.record(SecurityUtils.currentUserId(), id, request,
                        RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/{id}/agreement")
    public ResponseEntity<ApiResponse<RentalAgreementResponse>> agreement(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Agreement loaded",
                rentalAgreementService.getForOwner(SecurityUtils.currentUserId(), id)));
    }

    @PostMapping(value = "/{id}/agreement", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<RentalAgreementResponse>> issueAgreement(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String terms,
            @RequestPart(value = "file", required = false) MultipartFile file,
            HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Agreement issued",
                rentalAgreementService.issue(SecurityUtils.currentUserId(), id, endDate, terms, file,
                        RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<BookingResponse>> checkIn(@PathVariable Long id,
                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Tenant checked in",
                checkInService.checkIn(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest))));
    }
}
