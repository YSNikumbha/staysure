package com.staysure.booking.service;

import com.staysure.booking.dto.TenantProfileResponse;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.mapper.BookingMapper;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.exception.ApiException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.service.OwnerService;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TenantProfileService {

    private final TenantProfileRepository tenantProfileRepository;
    private final OwnerService ownerService;
    private final UserService userService;
    private final BookingMapper bookingMapper;

    public TenantProfileService(TenantProfileRepository tenantProfileRepository,
                                OwnerService ownerService,
                                UserService userService,
                                BookingMapper bookingMapper) {
        this.tenantProfileRepository = tenantProfileRepository;
        this.ownerService = ownerService;
        this.userService = userService;
        this.bookingMapper = bookingMapper;
    }

    @Transactional(readOnly = true)
    public List<TenantProfileResponse> listForOwner(Long ownerUserId) {
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        return tenantProfileRepository.findAllByOwner(owner).stream()
                .map(bookingMapper::toTenantResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TenantProfileResponse getForOwner(Long ownerUserId, Long tenantId) {
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        TenantProfile tenant = tenantProfileRepository.findByIdAndOwner(tenantId, owner)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Tenant not found", "TENANT_NOT_FOUND"));
        return bookingMapper.toTenantResponse(tenant);
    }

    @Transactional(readOnly = true)
    public List<TenantProfileResponse> listForUser(Long userId) {
        User user = userService.getUser(userId);
        return tenantProfileRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(bookingMapper::toTenantResponse)
                .toList();
    }
}
