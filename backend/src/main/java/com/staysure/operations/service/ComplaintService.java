package com.staysure.operations.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.operations.dto.ComplaintCommentRequest;
import com.staysure.operations.dto.ComplaintRequest;
import com.staysure.operations.dto.ComplaintResponse;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.entity.Complaint;
import com.staysure.operations.entity.ComplaintComment;
import com.staysure.operations.entity.ComplaintStatusHistory;
import com.staysure.operations.enums.ComplaintStatus;
import com.staysure.operations.enums.NotificationType;
import com.staysure.operations.enums.OperationalPriority;
import com.staysure.operations.mapper.OperationMapper;
import com.staysure.operations.repository.ComplaintCommentRepository;
import com.staysure.operations.repository.ComplaintRepository;
import com.staysure.operations.repository.ComplaintStatusHistoryRepository;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;
    private final ComplaintCommentRepository commentRepository;
    private final ComplaintStatusHistoryRepository historyRepository;
    private final OperationAccessService accessService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final OperationMapper mapper;

    public ComplaintService(ComplaintRepository complaintRepository,
                            ComplaintCommentRepository commentRepository,
                            ComplaintStatusHistoryRepository historyRepository,
                            OperationAccessService accessService,
                            NotificationService notificationService,
                            AuditService auditService,
                            OperationMapper mapper) {
        this.complaintRepository = complaintRepository;
        this.commentRepository = commentRepository;
        this.historyRepository = historyRepository;
        this.accessService = accessService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.mapper = mapper;
    }

    @Transactional
    public ComplaintResponse create(Long userId, ComplaintRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        TenantProfile tenant = accessService.activeTenant(userId);
        Complaint complaint = new Complaint();
        complaint.setComplaintNumber(nextComplaintNumber());
        complaint.setTenantProfile(tenant);
        complaint.setProperty(tenant.getProperty());
        complaint.setRoom(tenant.getRoom());
        complaint.setCategory(request.category());
        complaint.setTitle(request.title().trim());
        complaint.setDescription(request.description().trim());
        complaint.setPriority(request.priority() == null ? OperationalPriority.MEDIUM : request.priority());
        complaint.setStatus(ComplaintStatus.OPEN);
        Complaint saved = complaintRepository.save(complaint);
        addHistory(saved, null, ComplaintStatus.OPEN, "Complaint created", actor);
        auditService.log(actor, "COMPLAINT_CREATED", "OPERATIONS", "Complaint", saved.getId(),
                "Complaint created", null, saved.getComplaintNumber(), ipAddress);
        notificationService.create(saved.getProperty().getOwner().getUser(), NotificationType.COMPLAINT_CREATED,
                "New complaint raised", saved.getTitle(), "COMPLAINT", saved.getId());
        return detail(saved);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> listForUser(Long userId) {
        return complaintRepository.findAllByTenantUser(accessService.user(userId)).stream().map(this::detail).toList();
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getForUser(Long userId, Long complaintId) {
        return detail(userComplaint(userId, complaintId));
    }

    @Transactional
    public ComplaintResponse cancel(Long userId, Long complaintId, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        Complaint complaint = userComplaint(userId, complaintId);
        if (complaint.getStatus() != ComplaintStatus.OPEN) {
            throw new BusinessRuleException("Complaint cannot be cancelled now", "COMPLAINT_CANNOT_BE_CANCELLED");
        }
        transition(complaint, ComplaintStatus.CANCELLED, remarks(request), actor, "COMPLAINT_CANCELLED", ipAddress);
        notificationService.create(complaint.getProperty().getOwner().getUser(), NotificationType.COMPLAINT_CANCELLED,
                "Complaint cancelled", complaint.getTitle(), "COMPLAINT", complaint.getId());
        return detail(complaint);
    }

    @Transactional
    public ComplaintResponse reopen(Long userId, Long complaintId, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        Complaint complaint = userComplaint(userId, complaintId);
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new BusinessRuleException("Only resolved complaints can be reopened", "COMPLAINT_CANNOT_BE_REOPENED");
        }
        transition(complaint, ComplaintStatus.REOPENED, remarks(request), actor, "COMPLAINT_REOPENED", ipAddress);
        notificationService.create(complaint.getProperty().getOwner().getUser(), NotificationType.COMPLAINT_REOPENED,
                "Complaint reopened", complaint.getTitle(), "COMPLAINT", complaint.getId());
        return detail(complaint);
    }

    @Transactional
    public ComplaintResponse closeByTenant(Long userId, Long complaintId, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        Complaint complaint = userComplaint(userId, complaintId);
        if (complaint.getStatus() != ComplaintStatus.RESOLVED) {
            throw new BusinessRuleException("Only resolved complaints can be closed", "INVALID_COMPLAINT_TRANSITION");
        }
        transition(complaint, ComplaintStatus.CLOSED, remarks(request), actor, "COMPLAINT_CLOSED", ipAddress);
        return detail(complaint);
    }

    @Transactional
    public ComplaintResponse addTenantComment(Long userId, Long complaintId, ComplaintCommentRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        Complaint complaint = userComplaint(userId, complaintId);
        ComplaintComment comment = addComment(complaint, actor, request.comment());
        auditService.log(actor, "COMPLAINT_COMMENT_ADDED", "OPERATIONS", "ComplaintComment", comment.getId(),
                "Complaint comment added", null, null, ipAddress);
        notificationService.create(complaint.getProperty().getOwner().getUser(), NotificationType.COMPLAINT_COMMENT_ADDED,
                "New complaint comment", complaint.getTitle(), "COMPLAINT", complaint.getId());
        return detail(complaint);
    }

    @Transactional(readOnly = true)
    public List<ComplaintResponse> listForOwner(Long ownerUserId) {
        OwnerProfile owner = accessService.owner(ownerUserId);
        return complaintRepository.findAllByOwner(owner).stream().map(this::detail).toList();
    }

    @Transactional(readOnly = true)
    public ComplaintResponse getForOwner(Long ownerUserId, Long complaintId) {
        return detail(ownerComplaint(ownerUserId, complaintId));
    }

    @Transactional
    public ComplaintResponse acknowledge(Long ownerUserId, Long complaintId, OperationActionRequest request, String ipAddress) {
        return ownerTransition(ownerUserId, complaintId, ComplaintStatus.OPEN, ComplaintStatus.ACKNOWLEDGED,
                "COMPLAINT_ACKNOWLEDGED", NotificationType.COMPLAINT_ACKNOWLEDGED, request, ipAddress);
    }

    @Transactional
    public ComplaintResponse start(Long ownerUserId, Long complaintId, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        Complaint complaint = ownerComplaint(ownerUserId, complaintId);
        if (complaint.getStatus() != ComplaintStatus.ACKNOWLEDGED && complaint.getStatus() != ComplaintStatus.REOPENED) {
            throw new BusinessRuleException("Complaint cannot be started from this status", "INVALID_COMPLAINT_TRANSITION");
        }
        transition(complaint, ComplaintStatus.IN_PROGRESS, remarks(request), actor, "COMPLAINT_STARTED", ipAddress);
        notificationService.create(complaint.getTenantProfile().getUser(), NotificationType.COMPLAINT_IN_PROGRESS,
                "Complaint work started", complaint.getTitle(), "COMPLAINT", complaint.getId());
        return detail(complaint);
    }

    @Transactional
    public ComplaintResponse resolve(Long ownerUserId, Long complaintId, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        Complaint complaint = ownerComplaint(ownerUserId, complaintId);
        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS && complaint.getStatus() != ComplaintStatus.ACKNOWLEDGED) {
            throw new BusinessRuleException("Complaint cannot be resolved from this status", "INVALID_COMPLAINT_TRANSITION");
        }
        complaint.setResolvedAt(LocalDateTime.now());
        transition(complaint, ComplaintStatus.RESOLVED, remarks(request), actor, "COMPLAINT_RESOLVED", ipAddress);
        notificationService.create(complaint.getTenantProfile().getUser(), NotificationType.COMPLAINT_RESOLVED,
                "Complaint resolved", complaint.getTitle(), "COMPLAINT", complaint.getId());
        return detail(complaint);
    }

    @Transactional
    public ComplaintResponse closeByOwner(Long ownerUserId, Long complaintId, OperationActionRequest request, String ipAddress) {
        return ownerTransition(ownerUserId, complaintId, ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED,
                "COMPLAINT_CLOSED", null, request, ipAddress);
    }

    @Transactional
    public ComplaintResponse addOwnerComment(Long ownerUserId, Long complaintId, ComplaintCommentRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        Complaint complaint = ownerComplaint(ownerUserId, complaintId);
        ComplaintComment comment = addComment(complaint, actor, request.comment());
        auditService.log(actor, "COMPLAINT_COMMENT_ADDED", "OPERATIONS", "ComplaintComment", comment.getId(),
                "Complaint comment added", null, null, ipAddress);
        notificationService.create(complaint.getTenantProfile().getUser(), NotificationType.COMPLAINT_COMMENT_ADDED,
                "New complaint update", complaint.getTitle(), "COMPLAINT", complaint.getId());
        return detail(complaint);
    }

    private ComplaintResponse ownerTransition(Long ownerUserId,
                                              Long complaintId,
                                              ComplaintStatus expected,
                                              ComplaintStatus next,
                                              String auditAction,
                                              NotificationType notificationType,
                                              OperationActionRequest request,
                                              String ipAddress) {
        User actor = accessService.user(ownerUserId);
        Complaint complaint = ownerComplaint(ownerUserId, complaintId);
        if (complaint.getStatus() != expected) {
            throw new BusinessRuleException("Invalid complaint transition", "INVALID_COMPLAINT_TRANSITION");
        }
        transition(complaint, next, remarks(request), actor, auditAction, ipAddress);
        if (notificationType != null) {
            notificationService.create(complaint.getTenantProfile().getUser(), notificationType,
                    "Complaint updated", complaint.getTitle(), "COMPLAINT", complaint.getId());
        }
        return detail(complaint);
    }

    private Complaint userComplaint(Long userId, Long complaintId) {
        return complaintRepository.findByIdAndTenantUser(complaintId, accessService.user(userId))
                .orElseThrow(() -> complaintAccessError(complaintId));
    }

    private Complaint ownerComplaint(Long ownerUserId, Long complaintId) {
        return complaintRepository.findByIdAndOwner(complaintId, accessService.owner(ownerUserId))
                .orElseThrow(() -> complaintAccessError(complaintId));
    }

    private ApiException complaintAccessError(Long complaintId) {
        if (complaintId != null && complaintRepository.existsById(complaintId)) {
            return new ApiException(HttpStatus.FORBIDDEN, "Complaint access denied", "COMPLAINT_ACCESS_DENIED");
        }
        return new ApiException(HttpStatus.NOT_FOUND, "Complaint not found", "COMPLAINT_NOT_FOUND");
    }

    private void transition(Complaint complaint, ComplaintStatus next, String remarks, User actor, String auditAction, String ipAddress) {
        ComplaintStatus previous = complaint.getStatus();
        complaint.setStatus(next);
        if (next == ComplaintStatus.CLOSED) {
            complaint.setClosedAt(LocalDateTime.now());
        }
        Complaint saved = complaintRepository.save(complaint);
        addHistory(saved, previous, next, remarks, actor);
        auditService.log(actor, auditAction, "OPERATIONS", "Complaint", saved.getId(),
                "Complaint status updated", previous.name(), next.name(), ipAddress);
    }

    private ComplaintComment addComment(Complaint complaint, User actor, String text) {
        ComplaintComment comment = new ComplaintComment();
        comment.setComplaint(complaint);
        comment.setAuthorUser(actor);
        comment.setComment(text.trim());
        return commentRepository.save(comment);
    }

    private void addHistory(Complaint complaint, ComplaintStatus previous, ComplaintStatus next, String remarks, User actor) {
        ComplaintStatusHistory history = new ComplaintStatusHistory();
        history.setComplaint(complaint);
        history.setPreviousStatus(previous);
        history.setNewStatus(next);
        history.setRemarks(remarks);
        history.setChangedBy(actor);
        historyRepository.save(history);
    }

    private ComplaintResponse detail(Complaint complaint) {
        return mapper.toComplaint(
                complaint,
                commentRepository.findAllByComplaintOrderByCreatedAtAsc(complaint),
                historyRepository.findAllByComplaintOrderByCreatedAtAsc(complaint)
        );
    }

    private String nextComplaintNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "CMP-" + year + "-";
        long sequence = complaintRepository.countByComplaintNumberStartingWith(prefix) + 1;
        String number;
        do {
            number = prefix + String.format("%06d", sequence++);
        } while (complaintRepository.existsByComplaintNumber(number));
        return number;
    }

    private String remarks(OperationActionRequest request) {
        return request == null || request.remarks() == null || request.remarks().isBlank() ? null : request.remarks().trim();
    }
}
