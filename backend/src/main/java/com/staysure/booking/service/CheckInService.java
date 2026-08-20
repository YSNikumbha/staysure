package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.property.entity.Bed;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.repository.BedRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CheckInService {

    private final BookingService bookingService;
    private final TenantProfileRepository tenantProfileRepository;
    private final BedRepository bedRepository;
    private final UserService userService;
    private final AuditService auditService;

    public CheckInService(BookingService bookingService,
                          TenantProfileRepository tenantProfileRepository,
                          BedRepository bedRepository,
                          UserService userService,
                          AuditService auditService) {
        this.bookingService = bookingService;
        this.tenantProfileRepository = tenantProfileRepository;
        this.bedRepository = bedRepository;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional
    public BookingResponse checkIn(Long ownerUserId, Long bookingId, String ipAddress) {
        User actor = userService.getUser(ownerUserId);
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed bookings can be checked in", "INVALID_BOOKING_TRANSITION");
        }
        TenantProfile tenant = tenantProfileRepository.findByBooking(booking)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant profile not found"));
        if (tenant.getStatus() != TenantStatus.UPCOMING) {
            throw new BusinessRuleException("Tenant cannot be checked in from current status", "INVALID_TENANT_TRANSITION");
        }
        Bed lockedBed = bedRepository.findLockedById(booking.getBed().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        if (lockedBed.getStatus() != BedStatus.RESERVED) {
            throw new BusinessRuleException("Reserved bed is not available for check-in", "BED_NOT_RESERVED");
        }
        lockedBed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(lockedBed);
        LocalDateTime now = LocalDateTime.now();
        tenant.setStatus(TenantStatus.ACTIVE);
        tenant.setJoiningDate(now);
        tenantProfileRepository.save(tenant);
        booking.setCheckedInAt(now);
        bookingService.transition(booking, BookingStatus.CHECKED_IN, actor, "Tenant checked in");
        auditService.log(actor, "BED_OCCUPIED", "BOOKING", "Bed", lockedBed.getId(),
                "Bed marked occupied during check-in", BedStatus.RESERVED.name(), BedStatus.OCCUPIED.name(), ipAddress);
        auditService.log(actor, "TENANT_CHECKED_IN", "BOOKING", "TenantProfile", tenant.getId(),
                "Tenant checked in", TenantStatus.UPCOMING.name(), TenantStatus.ACTIVE.name(), ipAddress);
        return bookingService.response(booking);
    }
}
