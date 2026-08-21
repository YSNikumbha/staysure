package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.operations.dto.NoticeRequest;
import com.staysure.operations.dto.NoticeResponse;
import com.staysure.operations.service.NoticeService;
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
@RequestMapping("/api/v1/owner/notices")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerNoticeController {

    private final NoticeService noticeService;

    public OwnerNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Notices loaded", noticeService.listForOwner(SecurityUtils.currentUserId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NoticeResponse>> create(@Valid @RequestBody NoticeRequest request,
                                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Notice created",
                noticeService.create(SecurityUtils.currentUserId(), request, RequestUtils.getClientIp(servletRequest))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Notice loaded", noticeService.getForOwner(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> update(@PathVariable Long id,
                                                              @Valid @RequestBody NoticeRequest request,
                                                              HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Notice updated",
                noticeService.update(SecurityUtils.currentUserId(), id, request, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<ApiResponse<NoticeResponse>> publish(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Notice published",
                noticeService.publish(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest))));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<NoticeResponse>> archive(@PathVariable Long id, HttpServletRequest servletRequest) {
        return ResponseEntity.ok(ApiResponse.success("Notice archived",
                noticeService.archive(SecurityUtils.currentUserId(), id, RequestUtils.getClientIp(servletRequest))));
    }
}
