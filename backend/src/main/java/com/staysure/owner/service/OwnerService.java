package com.staysure.owner.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.owner.dto.OwnerApplicationRequest;
import com.staysure.owner.dto.OwnerDashboardResponse;
import com.staysure.owner.dto.OwnerProfileResponse;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.mapper.OwnerMapper;
import com.staysure.owner.repository.OwnerProfileRepository;
import com.staysure.property.dto.OwnerDashboardStats;
import com.staysure.property.service.PgPropertyService;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OwnerService {

    private final OwnerProfileRepository ownerProfileRepository;
    private final UserService userService;
    private final OwnerMapper ownerMapper;
    private final AuditService auditService;
    private final PgPropertyService pgPropertyService;

    public OwnerService(OwnerProfileRepository ownerProfileRepository,
                        UserService userService,
                        OwnerMapper ownerMapper,
                        AuditService auditService,
                        PgPropertyService pgPropertyService) {
        this.ownerProfileRepository = ownerProfileRepository;
        this.userService = userService;
        this.ownerMapper = ownerMapper;
        this.auditService = auditService;
        this.pgPropertyService = pgPropertyService;
    }

    @Transactional
    public OwnerProfileResponse apply(Long userId, OwnerApplicationRequest request, String ipAddress) {
        User user = userService.getUser(userId);
        if (ownerProfileRepository.existsByUser(user)) {
            throw new BusinessRuleException("Owner application already exists", "OWNER_APPLICATION_EXISTS");
        }
        OwnerProfile owner = new OwnerProfile();
        owner.setUser(user);
        applyRequest(owner, request);
        owner.setVerificationStatus(OwnerVerificationStatus.PENDING);
        OwnerProfile saved = ownerProfileRepository.save(owner);
        auditService.log(user, "OWNER_APPLICATION_SUBMITTED", "OWNER", "OwnerProfile", saved.getId(),
                "Owner application submitted", null, null, ipAddress);
        return ownerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OwnerProfile getCurrentOwner(Long userId) {
        User user = userService.getUser(userId);
        return ownerProfileRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Owner application not found"));
    }

    @Transactional(readOnly = true)
    public OwnerProfileResponse getMyProfile(Long userId) {
        return ownerMapper.toResponse(getCurrentOwner(userId));
    }

    @Transactional
    public OwnerProfileResponse updateMyProfile(Long userId, OwnerApplicationRequest request, String ipAddress) {
        OwnerProfile owner = getCurrentOwner(userId);
        applyRequest(owner, request);
        OwnerProfile saved = ownerProfileRepository.save(owner);
        auditService.log(owner.getUser(), "OWNER_APPLICATION_UPDATED", "OWNER", "OwnerProfile", saved.getId(),
                "Owner application updated", null, null, ipAddress);
        return ownerMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public OwnerDashboardResponse getDashboard(Long userId) {
        OwnerProfile owner = getCurrentOwner(userId);
        OwnerDashboardStats stats = pgPropertyService.dashboardStats(owner);
        return new OwnerDashboardResponse(
                owner.getId(),
                owner.getBusinessName(),
                owner.getVerificationStatus(),
                stats.totalPgs(),
                stats.activePgs(),
                stats.totalRooms(),
                stats.totalBeds(),
                stats.availableBeds()
        );
    }

    private void applyRequest(OwnerProfile owner, OwnerApplicationRequest request) {
        owner.setBusinessName(request.businessName().trim());
        owner.setAlternatePhone(blankToNull(request.alternatePhone()));
        owner.setBusinessEmail(blankToNullLower(request.businessEmail()));
        owner.setExperienceYears(request.experienceYears());
        owner.setDescription(blankToNull(request.description()));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String blankToNullLower(String value) {
        String cleaned = blankToNull(value);
        return cleaned == null ? null : cleaned.toLowerCase();
    }
}
