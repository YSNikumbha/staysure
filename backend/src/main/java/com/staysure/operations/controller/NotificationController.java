package com.staysure.operations.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.operations.dto.NotificationResponse;
import com.staysure.operations.dto.UnreadCountResponse;
import com.staysure.operations.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success("Notifications loaded",
                notificationService.list(SecurityUtils.currentUserId())));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> unreadCount() {
        return ResponseEntity.ok(ApiResponse.success("Unread notification count loaded",
                notificationService.unreadCount(SecurityUtils.currentUserId())));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Notification marked read",
                notificationService.markRead(SecurityUtils.currentUserId(), id)));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAll() {
        notificationService.markAllRead(SecurityUtils.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Notifications marked read", null));
    }
}
