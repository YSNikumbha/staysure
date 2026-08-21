package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.operations.dto.NoticeResponse;
import com.staysure.operations.service.NoticeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/notices")
public class UserNoticeController {

    private final NoticeService noticeService;

    public UserNoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NoticeResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Notices loaded", noticeService.listForUser(SecurityUtils.currentUserId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NoticeResponse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Notice loaded", noticeService.getForUser(SecurityUtils.currentUserId(), id)));
    }
}
