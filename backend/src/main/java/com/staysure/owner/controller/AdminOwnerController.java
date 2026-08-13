package com.staysure.owner.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.owner.dto.OwnerDetailResponse;
import com.staysure.owner.dto.OwnerProfileResponse;
import com.staysure.owner.dto.OwnerRejectRequest;
import com.staysure.owner.dto.OwnerSuspendRequest;
import com.staysure.owner.dto.OwnerVerifyRequest;
import com.staysure.owner.service.AdminOwnerService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/owners")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminOwnerController {

    private final AdminOwnerService adminOwnerService;

    public AdminOwnerController(AdminOwnerService adminOwnerService) {
        this.adminOwnerService = adminOwnerService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<OwnerProfileResponse>>> list(@RequestParam(required = false) OwnerVerificationStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Owner applications loaded", adminOwnerService.list(status)));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<OwnerProfileResponse>>> pending() {
        return ResponseEntity.ok(ApiResponse.success("Pending owner applications loaded", adminOwnerService.pending()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OwnerDetailResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Owner application loaded", adminOwnerService.get(id)));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> verify(@PathVariable Long id,
                                                                    @Valid @RequestBody OwnerVerifyRequest request,
                                                                    HttpServletRequest servletRequest) {
        OwnerProfileResponse response = adminOwnerService.verify(
                id,
                SecurityUtils.currentUserId(),
                request.remarks(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Owner verified", response));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> reject(@PathVariable Long id,
                                                                    @Valid @RequestBody OwnerRejectRequest request,
                                                                    HttpServletRequest servletRequest) {
        OwnerProfileResponse response = adminOwnerService.reject(
                id,
                SecurityUtils.currentUserId(),
                request.reason(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Owner rejected", response));
    }

    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<OwnerProfileResponse>> suspend(@PathVariable Long id,
                                                                     @Valid @RequestBody OwnerSuspendRequest request,
                                                                     HttpServletRequest servletRequest) {
        OwnerProfileResponse response = adminOwnerService.suspend(
                id,
                SecurityUtils.currentUserId(),
                request.reason(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("Owner suspended", response));
    }
}
