package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.booking.dto.BookingActionRequest;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.dto.CreateBookingRequest;
import com.staysure.booking.dto.RentalAgreementResponse;
import com.staysure.booking.dto.SecurityDepositResponse;
import com.staysure.booking.dto.TenantDocumentResponse;
import com.staysure.booking.dto.TenantProfileResponse;
import com.staysure.booking.service.BookingService;
import com.staysure.booking.service.RentalAgreementService;
import com.staysure.booking.service.SecurityDepositService;
import com.staysure.booking.service.TenantDocumentService;
import com.staysure.booking.service.TenantProfileService;
import com.staysure.common.enums.DocumentType;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserBookingController {

    private final BookingService bookingService;
    private final TenantDocumentService tenantDocumentService;
    private final SecurityDepositService securityDepositService;
    private final RentalAgreementService rentalAgreementService;
    private final TenantProfileService tenantProfileService;

    public UserBookingController(BookingService bookingService,
                                 TenantDocumentService tenantDocumentService,
                                 SecurityDepositService securityDepositService,
                                 RentalAgreementService rentalAgreementService,
                                 TenantProfileService tenantProfileService) {
        this.bookingService = bookingService;
        this.tenantDocumentService = tenantDocumentService;
        this.securityDepositService = securityDepositService;
        this.rentalAgreementService = rentalAgreementService;
        this.tenantProfileService = tenantProfileService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<ApiResponse<BookingResponse>> requestBooking(@Valid @RequestBody CreateBookingRequest request,
                                                                       HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Booking requested",
                bookingService.request(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/bookings")
    public ResponseEntity<ApiResponse<List<BookingResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Bookings loaded",
                bookingService.listForUser(SecurityUtils.currentUserId())));
    }

    @GetMapping("/bookings/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Booking loaded",
                bookingService.getForUser(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/bookings/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancel(@PathVariable Long id,
                                                               @RequestBody(required = false) BookingActionRequest request,
                                                               HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled",
                bookingService.cancel(SecurityUtils.currentUserId(), id, request == null ? null : request.remarks(),
                        RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/bookings/{id}/documents")
    public ResponseEntity<ApiResponse<List<TenantDocumentResponse>>> listDocuments(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("KYC documents loaded",
                tenantDocumentService.listForUser(SecurityUtils.currentUserId(), id)));
    }

    @PostMapping(value = "/bookings/{id}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<TenantDocumentResponse>> uploadDocument(@PathVariable Long id,
                                                                              @RequestParam DocumentType documentType,
                                                                              @RequestParam(required = false) String documentNumber,
                                                                              @RequestPart("file") MultipartFile file,
                                                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("KYC document uploaded",
                tenantDocumentService.upload(SecurityUtils.currentUserId(), id, documentType, documentNumber, file,
                        RequestUtils.getClientIp(servletRequest))));
    }

    @DeleteMapping("/bookings/{id}/documents/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id, @PathVariable Long documentId) {
        tenantDocumentService.delete(SecurityUtils.currentUserId(), id, documentId);
        return ResponseEntity.ok(ApiResponse.success("KYC document deleted"));
    }

    @GetMapping("/bookings/{id}/deposit")
    public ResponseEntity<ApiResponse<SecurityDepositResponse>> deposit(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Security deposit loaded",
                securityDepositService.getForUser(SecurityUtils.currentUserId(), id)));
    }

    @GetMapping("/bookings/{id}/agreement")
    public ResponseEntity<ApiResponse<RentalAgreementResponse>> agreement(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Agreement loaded",
                rentalAgreementService.getForUser(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/bookings/{id}/agreement/accept")
    public ResponseEntity<ApiResponse<RentalAgreementResponse>> acceptAgreement(@PathVariable Long id,
                                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Agreement accepted",
                rentalAgreementService.accept(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/my-pg")
    public ResponseEntity<ApiResponse<BookingResponse>> myPg() {
        return ResponseEntity.ok(ApiResponse.success("Current PG loaded", bookingService.myPg(SecurityUtils.currentUserId())));
    }

    @GetMapping("/tenants")
    public ResponseEntity<ApiResponse<List<TenantProfileResponse>>> myTenantProfiles() {
        return ResponseEntity.ok(ApiResponse.success("Tenant profiles loaded",
                tenantProfileService.listForUser(SecurityUtils.currentUserId())));
    }
}
