package com.staysure.owner.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.enums.RoleName;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.owner.dto.OwnerDetailResponse;
import com.staysure.owner.dto.OwnerDocumentResponse;
import com.staysure.owner.dto.OwnerProfileResponse;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.mapper.OwnerMapper;
import com.staysure.owner.repository.OwnerDocumentRepository;
import com.staysure.owner.repository.OwnerProfileRepository;
import com.staysure.role.service.RoleService;
import com.staysure.user.entity.User;
import com.staysure.user.repository.UserRepository;
import com.staysure.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class AdminOwnerService {

    private final OwnerProfileRepository ownerProfileRepository;
    private final OwnerDocumentRepository ownerDocumentRepository;
    private final OwnerMapper ownerMapper;
    private final UserService userService;
    private final UserRepository userRepository;
    private final RoleService roleService;
    private final AuditService auditService;

    public AdminOwnerService(OwnerProfileRepository ownerProfileRepository,
                             OwnerDocumentRepository ownerDocumentRepository,
                             OwnerMapper ownerMapper,
                             UserService userService,
                             UserRepository userRepository,
                             RoleService roleService,
                             AuditService auditService) {
        this.ownerProfileRepository = ownerProfileRepository;
        this.ownerDocumentRepository = ownerDocumentRepository;
        this.ownerMapper = ownerMapper;
        this.userService = userService;
        this.userRepository = userRepository;
        this.roleService = roleService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OwnerProfileResponse> list(OwnerVerificationStatus status) {
        List<OwnerProfile> owners = status == null
                ? ownerProfileRepository.findAll()
                : ownerProfileRepository.findAllByVerificationStatus(status);
        return owners.stream().map(ownerMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<OwnerProfileResponse> pending() {
        return ownerProfileRepository.findAllByVerificationStatusIn(List.of(
                        OwnerVerificationStatus.PENDING,
                        OwnerVerificationStatus.UNDER_REVIEW
                )).stream()
                .map(ownerMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OwnerDetailResponse get(Long ownerId) {
        OwnerProfile owner = findOwner(ownerId);
        List<OwnerDocumentResponse> documents = ownerDocumentRepository.findAllByOwnerOrderByCreatedAtDesc(owner)
                .stream()
                .map(ownerMapper::toDocumentResponse)
                .toList();
        return new OwnerDetailResponse(ownerMapper.toResponse(owner), documents);
    }

    @Transactional
    public OwnerProfileResponse verify(Long ownerId, Long adminUserId, String remarks, String ipAddress) {
        if (ownerId == null || adminUserId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        OwnerProfile owner = findOwner(ownerId);
        User admin = userService.getUser(adminUserId);
        owner.setVerificationStatus(OwnerVerificationStatus.VERIFIED);
        owner.setVerificationRemarks(blankToNull(remarks));
        owner.setVerifiedAt(LocalDateTime.now());
        owner.setVerifiedBy(admin);
        User ownerUser = Objects.requireNonNull(owner.getUser(), "owner user must not be null");
        boolean assigned = roleService.assignRoleIfMissing(ownerUser, RoleName.PG_OWNER);
        userRepository.save(ownerUser);
        OwnerProfile saved = ownerProfileRepository.save(owner);
        auditService.log(admin, "OWNER_VERIFIED", "OWNER", "OwnerProfile", saved.getId(),
                "Owner application verified", null, "VERIFIED", ipAddress);
        if (assigned) {
            auditService.log(admin, "ROLE_ASSIGNED", "ROLE", "User", ownerUser.getId(),
                    "PG_OWNER role assigned after owner verification", null, "PG_OWNER", ipAddress);
        }
        return ownerMapper.toResponse(saved);
    }

    @Transactional
    public OwnerProfileResponse reject(Long ownerId, Long adminUserId, String reason, String ipAddress) {
        if (ownerId == null || adminUserId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        OwnerProfile owner = findOwner(ownerId);
        User admin = userService.getUser(adminUserId);
        owner.setVerificationStatus(OwnerVerificationStatus.REJECTED);
        owner.setVerificationRemarks(reason.trim());
        owner.setVerifiedAt(null);
        owner.setVerifiedBy(null);
        OwnerProfile saved = ownerProfileRepository.save(owner);
        auditService.log(admin, "OWNER_REJECTED", "OWNER", "OwnerProfile", saved.getId(),
                "Owner application rejected", null, "REJECTED", ipAddress);
        return ownerMapper.toResponse(saved);
    }

    @Transactional
    public OwnerProfileResponse suspend(Long ownerId, Long adminUserId, String reason, String ipAddress) {
        if (ownerId == null || adminUserId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        OwnerProfile owner = findOwner(ownerId);
        User admin = userService.getUser(adminUserId);
        owner.setVerificationStatus(OwnerVerificationStatus.SUSPENDED);
        owner.setVerificationRemarks(reason.trim());
        OwnerProfile saved = ownerProfileRepository.save(owner);
        auditService.log(admin, "OWNER_SUSPENDED", "OWNER", "OwnerProfile", saved.getId(),
                "Owner profile suspended", null, "SUSPENDED", ipAddress);
        return ownerMapper.toResponse(saved);
    }

    private OwnerProfile findOwner(Long ownerId) {
        Long id = Objects.requireNonNull(ownerId, "ownerId must not be null");
        return ownerProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Owner application not found"));
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
