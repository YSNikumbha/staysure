package com.staysure.operations.repository;

import com.staysure.operations.entity.Notification;
import com.staysure.operations.enums.NotificationType;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findAllByUserOrderByCreatedAtDesc(User user);

    Optional<Notification> findByIdAndUser(Long id, User user);

    long countByUserAndReadAtIsNull(User user);

    boolean existsByUserAndTypeAndReferenceTypeAndReferenceId(User user, NotificationType type, String referenceType, Long referenceId);
}
