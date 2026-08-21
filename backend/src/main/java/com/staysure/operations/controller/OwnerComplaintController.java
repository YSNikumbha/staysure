package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.ComplaintCommentRequest;
import com.staysure.operations.dto.ComplaintResponse;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.service.ComplaintService;
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
@RequestMapping("/api/v1/owner/complaints")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerComplaintController {

    private final ComplaintService complaintService;

    public OwnerComplaintController(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Complaints loaded",
                complaintService.listForOwner(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ComplaintResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Complaint loaded",
                complaintService.getForOwner(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponse<ComplaintResponse>> acknowledge(@PathVariable Long id,
                                                                      @RequestBody(required = false) OperationActionRequest request,
                                                                      HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint acknowledged",
                complaintService.acknowledge(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/start")
    public ResponseEntity<ApiResponse<ComplaintResponse>> start(@PathVariable Long id,
                                                                @RequestBody(required = false) OperationActionRequest request,
                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint started",
                complaintService.start(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ApiResponse<ComplaintResponse>> resolve(@PathVariable Long id,
                                                                  @RequestBody(required = false) OperationActionRequest request,
                                                                  HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint resolved",
                complaintService.resolve(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/close")
    public ResponseEntity<ApiResponse<ComplaintResponse>> close(@PathVariable Long id,
                                                                @RequestBody(required = false) OperationActionRequest request,
                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Complaint closed",
                complaintService.closeByOwner(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<ComplaintResponse>> comment(@PathVariable Long id,
                                                                  @Valid @RequestBody ComplaintCommentRequest request,
                                                                  HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Comment added",
                complaintService.addOwnerComment(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }
}
