package com.staysure.operations.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.dto.VisitorRequest;
import com.staysure.operations.dto.VisitorResponse;
import com.staysure.operations.entity.VisitorEntry;
import com.staysure.operations.enums.NotificationType;
import com.staysure.operations.enums.VisitorStatus;
import com.staysure.operations.mapper.OperationMapper;
import com.staysure.operations.repository.VisitorEntryRepository;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.user.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VisitorService {

    private final VisitorEntryRepository visitorRepository;
    private final OperationAccessService accessService;
    private final NotificationService notificationService;
    private final AuditService auditService;
    private final OperationMapper mapper;

    public VisitorService(VisitorEntryRepository visitorRepository,
                          OperationAccessService accessService,
                          NotificationService notificationService,
                          AuditService auditService,
                          OperationMapper mapper) {
        this.visitorRepository = visitorRepository;
        this.accessService = accessService;
        this.notificationService = notificationService;
        this.auditService = auditService;
        this.mapper = mapper;
    }

    @Transactional
    public VisitorResponse request(Long userId, VisitorRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        TenantProfile tenant = accessService.activeTenant(userId);
        validateVisit(request);
        VisitorEntry visitor = new VisitorEntry();
        visitor.setVisitorNumber(nextVisitorNumber());
        visitor.setTenantProfile(tenant);
        visitor.setProperty(tenant.getProperty());
        apply(visitor, request);
        visitor.setStatus(VisitorStatus.REQUESTED);
        VisitorEntry saved = visitorRepository.save(visitor);
        auditService.log(actor, "VISITOR_REQUESTED", "OPERATIONS", "VisitorEntry", saved.getId(),
                "Visitor requested", null, saved.getVisitorNumber(), ipAddress);
        notificationService.create(saved.getProperty().getOwner().getUser(), NotificationType.VISITOR_REQUESTED,
                "New visitor request", saved.getVisitorName(), "VISITOR", saved.getId());
        return mapper.toVisitor(saved);
    }

    @Transactional(readOnly = true)
    public List<VisitorResponse> listForUser(Long userId) {
        return visitorRepository.findAllByTenantUser(accessService.user(userId)).stream().map(mapper::toVisitor).toList();
    }

    @Transactional(readOnly = true)
    public VisitorResponse getForUser(Long userId, Long id) {
        return mapper.toVisitor(userVisitor(userId, id));
    }

    @Transactional
    public VisitorResponse cancel(Long userId, Long id, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(userId);
        VisitorEntry visitor = userVisitor(userId, id);
        if (visitor.getStatus() != VisitorStatus.REQUESTED && visitor.getStatus() != VisitorStatus.APPROVED) {
            throw new BusinessRuleException("Visitor request cannot be cancelled", "INVALID_VISITOR_TRANSITION");
        }
        return transition(visitor, VisitorStatus.CANCELLED, actor, "VISITOR_CANCELLED", ipAddress);
    }

    @Transactional(readOnly = true)
    public List<VisitorResponse> listForOwner(Long ownerUserId) {
        return visitorRepository.findAllByOwner(accessService.owner(ownerUserId)).stream().map(mapper::toVisitor).toList();
    }

    @Transactional(readOnly = true)
    public VisitorResponse getForOwner(Long ownerUserId, Long id) {
        return mapper.toVisitor(ownerVisitor(ownerUserId, id));
    }

    @Transactional
    public VisitorResponse approve(Long ownerUserId, Long id, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        VisitorEntry visitor = ownerVisitor(ownerUserId, id);
        if (visitor.getStatus() != VisitorStatus.REQUESTED) {
            throw new BusinessRuleException("Only requested visitors can be approved", "INVALID_VISITOR_TRANSITION");
        }
        visitor.setApprovedBy(actor);
        visitor.setApprovedAt(LocalDateTime.now());
        VisitorResponse response = transition(visitor, VisitorStatus.APPROVED, actor, "VISITOR_APPROVED", ipAddress);
        notificationService.create(visitor.getTenantProfile().getUser(), NotificationType.VISITOR_APPROVED,
                "Visitor approved", visitor.getVisitorName(), "VISITOR", visitor.getId());
        return response;
    }

    @Transactional
    public VisitorResponse reject(Long ownerUserId, Long id, OperationActionRequest request, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        VisitorEntry visitor = ownerVisitor(ownerUserId, id);
        if (visitor.getStatus() != VisitorStatus.REQUESTED) {
            throw new BusinessRuleException("Only requested visitors can be rejected", "INVALID_VISITOR_TRANSITION");
        }
        visitor.setRejectionReason(request == null || request.remarks() == null ? null : request.remarks().trim());
        VisitorResponse response = transition(visitor, VisitorStatus.REJECTED, actor, "VISITOR_REJECTED", ipAddress);
        notificationService.create(visitor.getTenantProfile().getUser(), NotificationType.VISITOR_REJECTED,
                "Visitor rejected", visitor.getVisitorName(), "VISITOR", visitor.getId());
        return response;
    }

    @Transactional
    public VisitorResponse checkIn(Long ownerUserId, Long id, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        VisitorEntry visitor = ownerVisitor(ownerUserId, id);
        if (visitor.getStatus() != VisitorStatus.APPROVED) {
            throw new BusinessRuleException("Visitor check-in requires approval", "INVALID_VISITOR_TRANSITION");
        }
        visitor.setActualArrivalTime(LocalDateTime.now());
        return transition(visitor, VisitorStatus.CHECKED_IN, actor, "VISITOR_CHECKED_IN", ipAddress);
    }

    @Transactional
    public VisitorResponse checkOut(Long ownerUserId, Long id, String ipAddress) {
        User actor = accessService.user(ownerUserId);
        VisitorEntry visitor = ownerVisitor(ownerUserId, id);
        if (visitor.getStatus() != VisitorStatus.CHECKED_IN) {
            throw new BusinessRuleException("Visitor checkout requires check-in", "INVALID_VISITOR_TRANSITION");
        }
        visitor.setActualDepartureTime(LocalDateTime.now());
        return transition(visitor, VisitorStatus.CHECKED_OUT, actor, "VISITOR_CHECKED_OUT", ipAddress);
    }

    private VisitorResponse transition(VisitorEntry visitor, VisitorStatus next, User actor, String action, String ipAddress) {
        VisitorStatus previous = visitor.getStatus();
        visitor.setStatus(next);
        VisitorEntry saved = visitorRepository.save(visitor);
        auditService.log(actor, action, "OPERATIONS", "VisitorEntry", saved.getId(),
                "Visitor status updated", previous.name(), next.name(), ipAddress);
        return mapper.toVisitor(saved);
    }

    private VisitorEntry userVisitor(Long userId, Long id) {
        return visitorRepository.findByIdAndTenantUser(id, accessService.user(userId))
                .orElseThrow(() -> visitorAccessError(id));
    }

    private VisitorEntry ownerVisitor(Long ownerUserId, Long id) {
        OwnerProfile owner = accessService.owner(ownerUserId);
        return visitorRepository.findByIdAndOwner(id, owner)
                .orElseThrow(() -> visitorAccessError(id));
    }

    private ApiException visitorAccessError(Long id) {
        if (id != null && visitorRepository.existsById(id)) {
            return new ApiException(HttpStatus.FORBIDDEN, "Visitor access denied", "VISITOR_ACCESS_DENIED");
        }
        return new ApiException(HttpStatus.NOT_FOUND, "Visitor not found", "VISITOR_NOT_FOUND");
    }

    private void validateVisit(VisitorRequest request) {
        if (request.visitDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Visit date cannot be in the past", "INVALID_VISIT_DATE");
        }
        if (!request.expectedDepartureTime().isAfter(request.expectedArrivalTime())) {
            throw new BusinessRuleException("Departure time must be after arrival time", "INVALID_VISIT_DATE");
        }
    }

    private void apply(VisitorEntry visitor, VisitorRequest request) {
        visitor.setVisitorName(request.visitorName().trim());
        visitor.setVisitorPhone(request.visitorPhone().trim());
        visitor.setRelationship(request.relationship().trim());
        visitor.setVisitDate(request.visitDate());
        visitor.setExpectedArrivalTime(request.expectedArrivalTime());
        visitor.setExpectedDepartureTime(request.expectedDepartureTime());
        visitor.setPurpose(request.purpose().trim());
    }

    private String nextVisitorNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "VIS-" + year + "-";
        long sequence = visitorRepository.countByVisitorNumberStartingWith(prefix) + 1;
        String number;
        do {
            number = prefix + String.format("%06d", sequence++);
        } while (visitorRepository.existsByVisitorNumber(number));
        return number;
    }
}
