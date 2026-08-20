package com.staysure;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.enums.RoleName;
import com.staysure.owner.dto.OwnerProfileResponse;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.mapper.OwnerMapper;
import com.staysure.owner.repository.OwnerDocumentRepository;
import com.staysure.owner.repository.OwnerProfileRepository;
import com.staysure.owner.service.AdminOwnerService;
import com.staysure.role.service.RoleService;
import com.staysure.user.entity.User;
import com.staysure.user.repository.UserRepository;
import com.staysure.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class AdminOwnerServiceTest {

    @Mock private OwnerProfileRepository ownerProfileRepository;
    @Mock private OwnerDocumentRepository ownerDocumentRepository;
    @Mock private OwnerMapper ownerMapper;
    @Mock private UserService userService;
    @Mock private UserRepository userRepository;
    @Mock private RoleService roleService;
    @Mock private AuditService auditService;

    private AdminOwnerService adminOwnerService;

    @BeforeEach
    void setUp() {
        adminOwnerService = new AdminOwnerService(
                ownerProfileRepository,
                ownerDocumentRepository,
                ownerMapper,
                userService,
                userRepository,
                roleService,
                auditService
        );
    }

    @Test
    void verifyAssignsPgOwnerRoleWhenMissing() {
        OwnerProfile owner = ownerProfile();
        User admin = new User();
        when(ownerProfileRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(userService.getUser(99L)).thenReturn(admin);
        when(roleService.assignRoleIfMissing(owner.getUser(), RoleName.PG_OWNER)).thenReturn(true);
        when(ownerProfileRepository.save(owner)).thenReturn(owner);
        when(ownerMapper.toResponse(owner)).thenReturn(dummyOwnerResponse(owner));

        adminOwnerService.verify(10L, 99L, "ok", "127.0.0.1");

        verify(roleService).assignRoleIfMissing(owner.getUser(), RoleName.PG_OWNER);
        verify(userRepository).save(owner.getUser());
        verify(auditService).log(eq(admin), eq("ROLE_ASSIGNED"), eq("ROLE"), eq("User"), any(), any(), any(), eq("PG_OWNER"), eq("127.0.0.1"));
    }

    @Test
    void verifyDoesNotDuplicatePgOwnerAuditWhenRoleAlreadyExists() {
        OwnerProfile owner = ownerProfile();
        User admin = new User();
        when(ownerProfileRepository.findById(10L)).thenReturn(Optional.of(owner));
        when(userService.getUser(99L)).thenReturn(admin);
        when(roleService.assignRoleIfMissing(owner.getUser(), RoleName.PG_OWNER)).thenReturn(false);
        when(ownerProfileRepository.save(owner)).thenReturn(owner);
        when(ownerMapper.toResponse(owner)).thenReturn(dummyOwnerResponse(owner));

        adminOwnerService.verify(10L, 99L, "ok", "127.0.0.1");

        verify(roleService).assignRoleIfMissing(owner.getUser(), RoleName.PG_OWNER);
        verify(auditService, never()).log(eq(admin), eq("ROLE_ASSIGNED"), eq("ROLE"), eq("User"), any(), any(), any(), eq("PG_OWNER"), eq("127.0.0.1"));
    }

    private OwnerProfile ownerProfile() {
        User ownerUser = new User();
        ownerUser.setFirstName("Owner");
        ownerUser.setLastName("User");
        ownerUser.setEmail("owner@example.com");
        ownerUser.setPhone("9999999999");
        OwnerProfile owner = new OwnerProfile();
        owner.setId(10L);
        owner.setUser(ownerUser);
        owner.setBusinessName("Owner Business");
        owner.setVerificationStatus(OwnerVerificationStatus.PENDING);
        return owner;
    }

    private OwnerProfileResponse dummyOwnerResponse(OwnerProfile owner) {
        return new OwnerProfileResponse(
                owner.getId(),
                null,
                owner.getBusinessName(),
                null,
                null,
                null,
                null,
                owner.getVerificationStatus(),
                owner.getVerificationRemarks(),
                owner.getVerifiedAt(),
                null,
                null,
                null
        );
    }
}
