package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.ComplaintCommentRequest;
import com.staysure.operations.dto.ComplaintRequest;
import com.staysure.operations.dto.ComplaintResponse;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.service.ComplaintService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/complaints")
public class UserComplaintController {

    private final ComplaintService complaintService;

    public UserComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ComplaintResponse>> create(@Valid @RequestBody ComplaintRequest request,
                                                                 HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint created",
                complaintService.create(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Complaints loaded",
                complaintService.listForUser(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Complaint loaded",
                complaintService.getForUser(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<ComplaintResponse>> cancel(@PathVariable Long id,
                                                                 @RequestBody(required = false) OperationActionRequest request,
                                                                 HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint cancelled",
                complaintService.cancel(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/reopen")
    public ResponseEntity<ApiResponse<ComplaintResponse>> reopen(@PathVariable Long id,
                                                                 @RequestBody(required = false) OperationActionRequest request,
                                                                 HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint reopened",
                complaintService.reopen(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ComplaintResponse>> close(@PathVariable Long id,
                                                                @RequestBody(required = false) OperationActionRequest request,
                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint closed",
                complaintService.closeByTenant(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<ComplaintResponse>> comment(@PathVariable Long id,
                                                                  @Valid @RequestBody ComplaintCommentRequest request,
                                                                  HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Comment added",
                complaintService.addTenantComment(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }
}
