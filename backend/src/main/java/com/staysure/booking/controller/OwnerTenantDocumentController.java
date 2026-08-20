package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.entity.TenantDocument;
import com.staysure.booking.service.TenantDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/bookings/{bookingId}/documents")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerTenantDocumentController {

    private final TenantDocumentService tenantDocumentService;

    public OwnerTenantDocumentController(TenantDocumentService tenantDocumentService) {
        this.tenantDocumentService = tenantDocumentService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantDocument>>> listDocuments(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Documents loaded", tenantDocumentService.getBookingDocuments(
                SecurityUtils.currentUserId(), bookingId
        )));
    }

    @PatchMapping("/{documentId}/verify")
    public ResponseEntity<ApiResponse<TenantDocument>> verifyDocument(@PathVariable Long bookingId,
                                                                      @PathVariable Long documentId,
                                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Document verified", tenantDocumentService.verifyDocument(
                SecurityUtils.currentUserId(), bookingId, documentId, RequestUtils.getClientIp(servletRequest)
        )));
    }

    @PatchMapping("/{documentId}/reject")
    public ResponseEntity<ApiResponse<TenantDocument>> rejectDocument(@PathVariable Long bookingId,
                                                                      @PathVariable Long documentId,
                                                                      @RequestParam String reason,
                                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Document rejected", tenantDocumentService.rejectDocument(
                SecurityUtils.currentUserId(), bookingId, documentId, reason, RequestUtils.getClientIp(servletRequest)
        )));
    }
}