package com.staysure.operations.service;

import com.staysure.common.exception.ApiException;
import com.staysure.operations.dto.NotificationResponse;
import com.staysure.operations.dto.UnreadCountResponse;
import com.staysure.operations.entity.Notification;
import com.staysure.operations.enums.NotificationType;
import com.staysure.operations.mapper.OperationMapper;
import com.staysure.operations.repository.NotificationRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final OperationMapper mapper;

    public NotificationService(NotificationRepository notificationRepository, UserService userService, OperationMapper mapper) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.mapper = mapper;
    }

    @Transactional
    public Notification create(User user, NotificationType type, String title, String message, String referenceType, Long referenceId) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setReferenceType(referenceType);
        notification.setReferenceId(referenceId);
        return notificationRepository.save(notification);
    }

    @Transactional
    public void createOnce(User user, NotificationType type, String title, String message, String referenceType, Long referenceId) {
        if (notificationRepository.existsByUserAndTypeAndReferenceTypeAndReferenceId(user, type, referenceType, referenceId)) {
            return;
        }
        create(user, type, title, message, referenceType, referenceId);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Long userId) {
        User user = userService.getUser(userId);
        return notificationRepository.findAllByUserOrderByCreatedAtDesc(user).stream().map(mapper::toNotification).toList();
    }

    @Transactional(readOnly = true)
    public UnreadCountResponse unreadCount(Long userId) {
        return new UnreadCountResponse(notificationRepository.countByUserAndReadAtIsNull(userService.getUser(userId)));
    }

    @Transactional
    public NotificationResponse markRead(Long userId, Long notificationId) {
        User user = userService.getUser(userId);
        Notification notification = notificationRepository.findByIdAndUser(notificationId, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Notification not found", "NOTIFICATION_NOT_FOUND"));
        if (notification.getReadAt() == null) {
            notification.setReadAt(LocalDateTime.now());
        }
        return mapper.toNotification(notificationRepository.save(notification));
    }

    @Transactional
    public void markAllRead(Long userId) {
        User user = userService.getUser(userId);
        LocalDateTime now = LocalDateTime.now();
        notificationRepository.findAllByUserOrderByCreatedAtDesc(user).forEach(notification -> {
            if (notification.getReadAt() == null) {
                notification.setReadAt(now);
                notificationRepository.save(notification);
            }
        });
    }
}
