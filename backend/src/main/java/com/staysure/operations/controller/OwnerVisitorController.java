package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.dto.VisitorResponse;
import com.staysure.operations.service.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping("/api/v1/owner/visitors")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerVisitorController {

    private final VisitorService visitorService;

    public OwnerVisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitorResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Visitors loaded",
                visitorService.listForOwner(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitorResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Visitor loaded",
                visitorService.getForOwner(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<VisitorResponse>> approve(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Visitor approved",
                visitorService.approve(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<VisitorResponse>> reject(@PathVariable Long id,
                                                               @RequestBody(required = false) OperationActionRequest request,
                                                               HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Visitor rejected",
                visitorService.reject(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<VisitorResponse>> checkIn(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Visitor checked in",
                visitorService.checkIn(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/check-out")
    public ResponseEntity<ApiResponse<VisitorResponse>> checkOut(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Visitor checked out",
                visitorService.checkOut(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest))));
    }
}
