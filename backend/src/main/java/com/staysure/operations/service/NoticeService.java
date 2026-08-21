package com.staysure.operations.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.operations.dto.NoticeRequest;
import com.staysure.operations.dto.NoticeResponse;
import com.staysure.operations.entity.Notice;
import com.staysure.operations.enums.NotificationType;
import com.staysure.operations.enums.NoticeStatus;
import com.staysure.operations.enums.NoticeType;
import com.staysure.operations.enums.OperationalPriority;
import com.staysure.operations.mapper.OperationMapper;
import com.staysure.operations.repository.NoticeRepository;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import com.staysure.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NoticeService {

    private final NoticeRepository noticeRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final OperationAccessService accessService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final OperationMapper mapper;

    public NoticeService(NoticeRepository noticeRepository,
                         TenantProfileRepository tenantProfileRepository,
                         OperationAccessService accessService,
                         NotificationService notificationService,
                         AuditService auditService,
                         OperationMapper mapper) {
        this.noticeRepository = noticeRepository;
        this.tenantProfileRepository = tenantProfileRepository;
        this.accessService = accessService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> listForOwner(Long ownerUserId) {
        return noticeRepository.findAllByOwner(accessService.owner(ownerUserId)).stream().map(mapper::toNotice).toList();
    }

    @Transactional(readOnly = true)
    public NoticeResponse getForOwner(Long ownerUserId, Long id) {
        return mapper.toNotice(ownerNotice(ownerUserId, id));
    }

    @Transactional
    public NoticeResponse create(Long ownerUserId, NoticeRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        OwnerProfile owner = accessService.owner(ownerUserId);
        PgProperty property = accessService.ownedProperty(request.propertyId(), owner);
        Notice notice = new Notice();
        apply(notice, property, actor, request);
        Notice saved = noticeRepository.save(notice);
        auditService.log(actor, "NOTICE_CREATED", "OPERATIONS", "Notice", saved.getId(),
                "Notice created", null, saved.getTitle(), ipAddress);
        return mapper.toNotice(saved);
    }

    @Transactional
    public NoticeResponse update(Long ownerUserId, Long id, NoticeRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        OwnerProfile owner = accessService.owner(ownerUserId);
        Notice notice = ownerNotice(ownerUserId, id);
        if (notice.getStatus() == NoticeStatus.ARCHIVED) {
            throw new BusinessRuleException("Archived notices cannot be edited", "INVALID_NOTICE_STATUS");
        }
        apply(notice, accessService.ownedProperty(request.propertyId(), owner), notice.getCreatedBy(), request);
        Notice saved = noticeRepository.save(notice);
        auditService.log(actor, "NOTICE_UPDATED", "OPERATIONS", "Notice", saved.getId(),
                "Notice updated", null, saved.getStatus().name(), ipAddress);
        return mapper.toNotice(saved);
    }

    @Transactional
    public NoticeResponse publish(Long ownerUserId, Long id, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        Notice notice = ownerNotice(ownerUserId, id);
        if (notice.getStatus() == NoticeStatus.ARCHIVED) {
            throw new BusinessRuleException("Archived notices cannot be published", "INVALID_NOTICE_STATUS");
        }
        NoticeStatus previous = notice.getStatus();
        notice.setStatus(NoticeStatus.PUBLISHED);
        notice.setPublishedAt(notice.getPublishedAt() == null ? LocalDateTime.now() : notice.getPublishedAt());
        Notice saved = noticeRepository.save(notice);
        tenantProfileRepository.findAllByPropertyAndOwnerAndStatus(saved.getProperty(), saved.getProperty().getOwner(), TenantStatus.ACTIVE)
                .stream()
                .map(TenantProfile::getUser)
                .forEach(user -> notificationService.createOnce(user, NotificationType.NOTICE_PUBLISHED,
                        "New PG notice", saved.getTitle(), "NOTICE", saved.getId()));
        auditService.log(actor, "NOTICE_PUBLISHED", "OPERATIONS", "Notice", saved.getId(),
                "Notice published", previous.name(), NoticeStatus.PUBLISHED.name(), ipAddress);
        return mapper.toNotice(saved);
    }

    @Transactional
    public NoticeResponse archive(Long ownerUserId, Long id, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        Notice notice = ownerNotice(ownerUserId, id);
        NoticeStatus previous = notice.getStatus();
        notice.setStatus(NoticeStatus.ARCHIVED);
        Notice saved = noticeRepository.save(notice);
        auditService.log(actor, "NOTICE_ARCHIVED", "OPERATIONS", "Notice", saved.getId(),
                "Notice archived", previous.name(), NoticeStatus.ARCHIVED.name(), ipAddress);
        return mapper.toNotice(saved);
    }

    @Transactional(readOnly = true)
    public List<NoticeResponse> listForUser(Long userId) {
        TenantProfile tenant = accessService.activeTenant(userId);
        return noticeRepository.findActiveForProperty(tenant.getProperty(), NoticeStatus.PUBLISHED, LocalDate.now())
                .stream().map(mapper::toNotice).toList();
    }

    @Transactional(readOnly = true)
    public NoticeResponse getForUser(Long userId, Long id) {
        TenantProfile tenant = accessService.activeTenant(userId);
        Notice notice = noticeRepository.findActiveByIdForProperty(id, tenant.getProperty(), NoticeStatus.PUBLISHED, LocalDate.now())
                .orElseThrow(() -> {
                    if (id != null && noticeRepository.existsById(id)) {
                        return new ApiException(HttpStatus.FORBIDDEN, "Notice access denied", "NOTICE_ACCESS_DENIED");
                    }
                    return new ApiException(HttpStatus.NOT_FOUND, "Notice not found", "NOTICE_NOT_FOUND");
                });
        return mapper.toNotice(notice);
    }

    private Notice ownerNotice(Long ownerUserId, Long id) {
        return noticeRepository.findByIdAndOwner(id, accessService.owner(ownerUserId))
                .orElseThrow(() -> {
                    if (id != null && noticeRepository.existsById(id)) {
                        return new ApiException(HttpStatus.FORBIDDEN, "Notice access denied", "NOTICE_ACCESS_DENIED");
                    }
                    return new ApiException(HttpStatus.NOT_FOUND, "Notice not found", "NOTICE_NOT_FOUND");
                });
    }

    private void apply(Notice notice, PgProperty property, User createdBy, NoticeRequest request) {
        notice.setProperty(property);
        notice.setCreatedBy(createdBy);
        notice.setTitle(request.title().trim());
        notice.setContent(request.content().trim());
        notice.setNoticeType(request.noticeType() == null ? NoticeType.GENERAL : request.noticeType());
        notice.setPriority(request.priority() == null ? OperationalPriority.MEDIUM : request.priority());
        notice.setExpiresAt(request.expiresAt());
    }
}
