package com.staysure.property.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.property.dto.admin.AdminPropertyDetailsResponse;
import com.staysure.property.dto.admin.AdminPropertySummaryResponse;
import com.staysure.property.dto.admin.VerificationDecisionRequest;
import com.staysure.property.service.AdminPgVerificationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/pgs")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminPgController {

    private final AdminPgVerificationService adminPgVerificationService;

    public AdminPgController(AdminPgVerificationService adminPgVerificationService) {
        this.adminPgVerificationService = adminPgVerificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AdminPropertySummaryResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("PGs loaded", adminPgVerificationService.list()));
    }

    @GetMapping("/pending-verification")
    public ResponseEntity<ApiResponse<List<AdminPropertySummaryResponse>>> pendingVerification() {
        return ResponseEntity.ok(ApiResponse.success("Pending PG verification requests loaded",
                adminPgVerificationService.pendingVerification()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AdminPropertyDetailsResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("PG verification request loaded", adminPgVerificationService.get(id)));
    }

    @PatchMapping("/{id}/review")
    public ResponseEntity<ApiResponse<AdminPropertyDetailsResponse>> review(@PathVariable Long id,
                                                                           @RequestBody(required = false) VerificationDecisionRequest request,
                                                                           HttpServletRequest servletRequest) {
        AdminPropertyDetailsResponse response = adminPgVerificationService.startReview(
                id,
                SecurityUtils.currentUserId(),
                request == null ? null : request.remarks(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG review started", response));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<AdminPropertyDetailsResponse>> verify(@PathVariable Long id,
                                                                           @RequestBody(required = false) VerificationDecisionRequest request,
                                                                           HttpServletRequest servletRequest) {
        AdminPropertyDetailsResponse response = adminPgVerificationService.verify(
                id,
                SecurityUtils.currentUserId(),
                request == null ? null : request.remarks(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG verified", response));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<AdminPropertyDetailsResponse>> reject(@PathVariable Long id,
                                                                           @Valid @RequestBody VerificationDecisionRequest request,
                                                                           HttpServletRequest servletRequest) {
        AdminPropertyDetailsResponse response = adminPgVerificationService.reject(
                id,
                SecurityUtils.currentUserId(),
                request.remarks(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG rejected", response));
    }

    @PatchMapping("/{id}/request-changes")
    public ResponseEntity<ApiResponse<AdminPropertyDetailsResponse>> requestChanges(@PathVariable Long id,
                                                                                   @Valid @RequestBody VerificationDecisionRequest request,
                                                                                   HttpServletRequest servletRequest) {
        AdminPropertyDetailsResponse response = adminPgVerificationService.requestChanges(
                id,
                SecurityUtils.currentUserId(),
                request.remarks(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG changes requested", response));
    }
}
