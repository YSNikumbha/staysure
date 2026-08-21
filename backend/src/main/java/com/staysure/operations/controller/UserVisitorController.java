package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.dto.VisitorRequest;
import com.staysure.operations.dto.VisitorResponse;
import com.staysure.operations.service.VisitorService;
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
@RequestMapping("/api/v1/users/visitors")
public class UserVisitorController {

    private final VisitorService visitorService;

    public UserVisitorController(VisitorService visitorService) {
        this.visitorService = visitorService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<VisitorResponse>> request(@Valid @RequestBody VisitorRequest request,
                                                                HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Visitor requested",
                visitorService.request(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<VisitorResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Visitors loaded",
                visitorService.listForUser(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VisitorResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Visitor loaded",
                visitorService.getForUser(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<VisitorResponse>> cancel(@PathVariable Long id,
                                                               @RequestBody(required = false) OperationActionRequest request,
                                                               HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Visitor cancelled",
                visitorService.cancel(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }
}
