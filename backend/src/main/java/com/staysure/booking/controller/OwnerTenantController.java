package com.staysure.booking.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.booking.dto.TenantProfileResponse;
import com.staysure.booking.service.TenantProfileService;
import com.staysure.common.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/tenants")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerTenantController {

    private final TenantProfileService tenantProfileService;

    public OwnerTenantController(TenantProfileService tenantProfileService) {
        this.tenantProfileService = tenantProfileService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TenantProfileResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Tenants loaded",
                tenantProfileService.listForOwner(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TenantProfileResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Tenant loaded",
                tenantProfileService.getForOwner(SecurityUtils.currentUserId(), id)));
    }
}
