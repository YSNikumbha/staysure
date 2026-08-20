package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.repository.BookingRepository;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.property.entity.Bed;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.repository.BedRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class CheckInService {

    private final BookingService bookingService;
    private final BookingRepository bookingRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final BedRepository bedRepository;
    private final UserService userService;
    private final AuditService auditService;

    public CheckInService(BookingService bookingService,
                          BookingRepository bookingRepository,
                          TenantProfileRepository tenantProfileRepository,
                          BedRepository bedRepository,
                          UserService userService,
                          AuditService auditService) {
        this.bookingService = bookingService;
        this.bookingRepository = bookingRepository;
        this.tenantProfileRepository = tenantProfileRepository;
        this.bedRepository = bedRepository;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional
    public TenantProfile checkIn(Long ownerId, Long bookingId, String ipAddress) {
        if (ownerId == null || bookingId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User owner = userService.getUser(ownerId);
        Booking booking = bookingService.getOwnerBooking(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw new BusinessRuleException("Booking not ready for check-in", "BOOKING_NOT_READY_FOR_CHECKIN");
        }

        if (tenantProfileRepository.existsByBookingId(bookingId)) {
            throw new BusinessRuleException("Tenant profile already exists", "TENANT_ALREADY_EXISTS");
        }

        Long bedId = Objects.requireNonNull(booking.getBed().getId(), "booking bed id must not be null");
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bed not found", "BED_NOT_FOUND"));

        if (bed.getStatus() != BedStatus.RESERVED) {
            throw new BusinessRuleException("Bed is not reserved", "BED_NOT_RESERVED");
        }

        bed.setStatus(BedStatus.OCCUPIED);
        bedRepository.save(bed);

        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setCheckedInAt(LocalDateTime.now());
        bookingRepository.save(booking);

        TenantProfile tenantProfile = new TenantProfile();
        tenantProfile.setUser(booking.getUser());
        tenantProfile.setBooking(booking);
        tenantProfile.setProperty(booking.getProperty());
        tenantProfile.setRoom(booking.getRoom());
        tenantProfile.setBed(bed);
        tenantProfile.setStatus(TenantStatus.ACTIVE);
        tenantProfile.setJoiningDate(LocalDateTime.now());

        TenantProfile saved = tenantProfileRepository.save(tenantProfile);
        auditService.log(owner, "TENANT_CHECKED_IN", "TENANT", "TenantProfile", saved.getId(),
                "Tenant checked in", null, saved.getUser().getFirstName() + " " + saved.getUser().getLastName(), ipAddress);

        return saved;
    }
}
