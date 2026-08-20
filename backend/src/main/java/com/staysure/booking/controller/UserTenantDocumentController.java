package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.booking.entity.TenantDocument;
import com.staysure.booking.enums.DocumentType;
import com.staysure.booking.service.TenantDocumentService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/bookings/{bookingId}/documents")
@PreAuthorize("hasRole('USER')")
public class UserTenantDocumentController {

    private final TenantDocumentService tenantDocumentService;

    public UserTenantDocumentController(TenantDocumentService tenantDocumentService) {
        this.tenantDocumentService = tenantDocumentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TenantDocument>> uploadDocument(@PathVariable Long bookingId,
                                                                      @RequestParam DocumentType documentType,
                                                                      @RequestParam(required = false) String documentNumber,
                                                                      @RequestParam String documentUrl,
                                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Document uploaded", tenantDocumentService.uploadDocument(
                SecurityUtils.currentUserId(), bookingId, documentType, documentNumber, documentUrl, RequestUtils.getClientIp(servletRequest)
        )));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantDocument>>> listDocuments(@PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success("Documents loaded", tenantDocumentService.getBookingDocuments(
                SecurityUtils.currentUserId(), bookingId
        )));
    }

    @DeleteMapping("/{documentId}")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long bookingId,
                                                             @PathVariable Long documentId,
                                                             HttpServletRequest servletRequest) {
        tenantDocumentService.deleteDocument(SecurityUtils.currentUserId(), bookingId, documentId, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Document deleted"));
    }
}