package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.MaintenanceTaskRequest;
import com.staysure.operations.dto.MaintenanceTaskResponse;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.service.MaintenanceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner/maintenance")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerMaintenanceController {

    private final MaintenanceService maintenanceService;

    public OwnerMaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<MaintenanceTaskResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Maintenance tasks loaded",
                maintenanceService.list(SecurityUtils.currentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> create(@Valid @RequestBody MaintenanceTaskRequest request,
                                                                       HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance task created",
                maintenanceService.create(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance task loaded",
                maintenanceService.get(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> update(@PathVariable Long id,
                                                                       @Valid @RequestBody MaintenanceTaskRequest request,
                                                                       HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance task updated",
                maintenanceService.update(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> start(@PathVariable Long id,
                                                                      @RequestBody(required = false) OperationActionRequest request,
                                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance task started",
                maintenanceService.start(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/complete")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> complete(@PathVariable Long id,
                                                                         @RequestBody(required = false) OperationActionRequest request,
                                                                         HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance task completed",
                maintenanceService.complete(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<MaintenanceTaskResponse>> cancel(@PathVariable Long id,
                                                                       @RequestBody(required = false) OperationActionRequest request,
                                                                       HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Maintenance task cancelled",
                maintenanceService.cancel(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }
}
