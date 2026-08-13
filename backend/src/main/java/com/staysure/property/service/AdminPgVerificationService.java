package com.staysure.property.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.owner.mapper.OwnerMapper;
import com.staysure.property.dto.admin.AdminPropertyDetailsResponse;
import com.staysure.property.dto.admin.AdminPropertySummaryResponse;
import com.staysure.property.dto.verification.VerificationHistoryResponse;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.PropertyVerificationHistory;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.mapper.PgPropertyMapper;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.PropertyVerificationHistoryRepository;
import com.staysure.property.repository.RoomRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AdminPgVerificationService {

    private final PgPropertyRepository pgPropertyRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final PropertyVerificationHistoryRepository verificationHistoryRepository;
    private final PgPropertyService pgPropertyService;
    private final UserService userService;
    private final OwnerMapper ownerMapper;
    private final PgPropertyMapper propertyMapper;
    private final AuditService auditService;

    public AdminPgVerificationService(PgPropertyRepository pgPropertyRepository,
                                      RoomRepository roomRepository,
                                      BedRepository bedRepository,
                                      PropertyVerificationHistoryRepository verificationHistoryRepository,
                                      PgPropertyService pgPropertyService,
                                      UserService userService,
                                      OwnerMapper ownerMapper,
                                      PgPropertyMapper propertyMapper,
                                      AuditService auditService) {
        this.pgPropertyRepository = pgPropertyRepository;
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.verificationHistoryRepository = verificationHistoryRepository;
        this.pgPropertyService = pgPropertyService;
        this.userService = userService;
        this.ownerMapper = ownerMapper;
        this.propertyMapper = propertyMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<AdminPropertySummaryResponse> list() {
        return pgPropertyRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AdminPropertySummaryResponse> pendingVerification() {
        return pgPropertyRepository.findAllByVerificationStatusInOrderBySubmittedForVerificationAtDesc(List.of(
                        PropertyVerificationStatus.PENDING,
                        PropertyVerificationStatus.UNDER_REVIEW
                )).stream()
                .map(this::summary)
                .toList();
    }

    @Transactional(readOnly = true)
    public AdminPropertyDetailsResponse get(Long propertyId) {
        PgProperty property = getProperty(propertyId);
        List<VerificationHistoryResponse> history = verificationHistoryRepository.findAllByPropertyOrderByCreatedAtDesc(property)
                .stream()
                .map(propertyMapper::toVerificationHistoryResponse)
                .toList();
        return new AdminPropertyDetailsResponse(
                ownerMapper.toResponse(property.getOwner()),
                pgPropertyService.detailsForProperty(property),
                history
        );
    }

    @Transactional
    public AdminPropertyDetailsResponse startReview(Long propertyId, Long adminUserId, String remarks, String ipAddress) {
        PgProperty property = getProperty(propertyId);
        if (property.getVerificationStatus() != PropertyVerificationStatus.PENDING) {
            throw new BusinessRuleException("Only pending PGs can be moved under review", "INVALID_VERIFICATION_TRANSITION");
        }
        User admin = userService.getUser(adminUserId);
        transition(property, PropertyVerificationStatus.UNDER_REVIEW, blankToNull(remarks), admin);
        auditService.log(admin, "PG_REVIEW_STARTED", "PROPERTY", "PgProperty", property.getId(),
                "PG verification review started", null, PropertyVerificationStatus.UNDER_REVIEW.name(), ipAddress);
        return get(propertyId);
    }

    @Transactional
    public AdminPropertyDetailsResponse verify(Long propertyId, Long adminUserId, String remarks, String ipAddress) {
        PgProperty property = getProperty(propertyId);
        if (property.getVerificationStatus() != PropertyVerificationStatus.PENDING
                && property.getVerificationStatus() != PropertyVerificationStatus.UNDER_REVIEW) {
            throw new BusinessRuleException("PG cannot be verified from current status", "INVALID_VERIFICATION_TRANSITION");
        }
        User admin = userService.getUser(adminUserId);
        PropertyVerificationStatus previous = property.getVerificationStatus();
        property.setVerificationStatus(PropertyVerificationStatus.VERIFIED);
        property.setVerifiedAt(LocalDateTime.now());
        property.setVerifiedBy(admin);
        property.setVerificationRemarks(blankToNull(remarks));
        property.setRejectionReason(null);
        if (property.getStatus() == PropertyStatus.DRAFT) {
            property.setStatus(PropertyStatus.ACTIVE);
        }
        pgPropertyRepository.save(property);
        recordHistory(property, previous, PropertyVerificationStatus.VERIFIED, blankToNull(remarks), admin);
        auditService.log(admin, "PG_VERIFIED", "PROPERTY", "PgProperty", property.getId(),
                "PG verified", null, PropertyVerificationStatus.VERIFIED.name(), ipAddress);
        return get(propertyId);
    }

    @Transactional
    public AdminPropertyDetailsResponse reject(Long propertyId, Long adminUserId, String remarks, String ipAddress) {
        String cleanedRemarks = requireRemarks(remarks);
        PgProperty property = getProperty(propertyId);
        if (property.getVerificationStatus() != PropertyVerificationStatus.PENDING
                && property.getVerificationStatus() != PropertyVerificationStatus.UNDER_REVIEW) {
            throw new BusinessRuleException("PG cannot be rejected from current status", "INVALID_VERIFICATION_TRANSITION");
        }
        User admin = userService.getUser(adminUserId);
        PropertyVerificationStatus previous = property.getVerificationStatus();
        property.setVerificationStatus(PropertyVerificationStatus.REJECTED);
        property.setVerifiedAt(null);
        property.setVerifiedBy(null);
        property.setVerificationRemarks(cleanedRemarks);
        property.setRejectionReason(cleanedRemarks);
        pgPropertyRepository.save(property);
        recordHistory(property, previous, PropertyVerificationStatus.REJECTED, cleanedRemarks, admin);
        auditService.log(admin, "PG_REJECTED", "PROPERTY", "PgProperty", property.getId(),
                "PG verification rejected", null, PropertyVerificationStatus.REJECTED.name(), ipAddress);
        return get(propertyId);
    }

    @Transactional
    public AdminPropertyDetailsResponse requestChanges(Long propertyId, Long adminUserId, String remarks, String ipAddress) {
        String cleanedRemarks = requireRemarks(remarks);
        PgProperty property = getProperty(propertyId);
        if (property.getVerificationStatus() != PropertyVerificationStatus.PENDING
                && property.getVerificationStatus() != PropertyVerificationStatus.UNDER_REVIEW) {
            throw new BusinessRuleException("Changes cannot be requested from current status", "INVALID_VERIFICATION_TRANSITION");
        }
        User admin = userService.getUser(adminUserId);
        transition(property, PropertyVerificationStatus.CHANGES_REQUESTED, cleanedRemarks, admin);
        property.setRejectionReason(null);
        auditService.log(admin, "PG_CHANGES_REQUESTED", "PROPERTY", "PgProperty", property.getId(),
                "PG verification changes requested", null, PropertyVerificationStatus.CHANGES_REQUESTED.name(), ipAddress);
        return get(propertyId);
    }

    private void transition(PgProperty property, PropertyVerificationStatus newStatus, String remarks, User actor) {
        PropertyVerificationStatus previous = property.getVerificationStatus();
        property.setVerificationStatus(newStatus);
        property.setVerificationRemarks(remarks);
        property.setVerifiedAt(null);
        property.setVerifiedBy(null);
        pgPropertyRepository.save(property);
        recordHistory(property, previous, newStatus, remarks, actor);
    }

    private void recordHistory(PgProperty property, PropertyVerificationStatus previousStatus,
                               PropertyVerificationStatus newStatus, String remarks, User actor) {
        PropertyVerificationHistory history = new PropertyVerificationHistory();
        history.setProperty(property);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setActionBy(actor);
        verificationHistoryRepository.save(history);
    }

    private AdminPropertySummaryResponse summary(PgProperty property) {
        String ownerName = property.getOwner().getBusinessName();
        return new AdminPropertySummaryResponse(
                property.getId(),
                property.getName(),
                ownerName,
                property.getOwner().getId(),
                property.getCity(),
                property.getSubmittedForVerificationAt(),
                property.getVerificationStatus(),
                property.getStatus(),
                roomRepository.countByPropertyAndStatusNot(property, RoomStatus.ARCHIVED),
                bedRepository.countByPropertyAndStatusNot(property, BedStatus.ARCHIVED)
        );
    }

    private PgProperty getProperty(Long propertyId) {
        return pgPropertyRepository.findById(propertyId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PG not found", "PG_NOT_FOUND"));
    }

    private String requireRemarks(String remarks) {
        if (remarks == null || remarks.isBlank()) {
            throw new BusinessRuleException("Remarks are required", "REMARKS_REQUIRED");
        }
        return remarks.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
