package com.staysure.operations.service;

import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.exception.ApiException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.service.OwnerService;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class OperationAccessService {

    private final UserService userService;
    private final OwnerService ownerService;
    private final TenantProfileRepository tenantProfileRepository;
    private final PgPropertyRepository propertyRepository;

    public OperationAccessService(UserService userService,
                                  OwnerService ownerService,
                                  TenantProfileRepository tenantProfileRepository,
                                  PgPropertyRepository propertyRepository) {
        this.userService = userService;
        this.ownerService = ownerService;
        this.tenantProfileRepository = tenantProfileRepository;
        this.propertyRepository = propertyRepository;
    }

    public User user(Long userId) {
        return userService.getUser(userId);
    }

    public OwnerProfile owner(Long ownerUserId) {
        return ownerService.getCurrentOwner(ownerUserId);
    }

    public TenantProfile activeTenant(Long userId) {
        User user = userService.getUser(userId);
        return tenantProfileRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, TenantStatus.ACTIVE)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Active tenancy required", "ACTIVE_TENANCY_REQUIRED"));
    }

    public PgProperty ownedProperty(Long propertyId, OwnerProfile owner) {
        return propertyRepository.findByIdAndOwner(propertyId, owner)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Operation access denied", "OPERATION_ACCESS_DENIED"));
    }
}
