package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.dto.CreateBookingRequest;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.BookingStatusHistory;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.entity.TenantDocument;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.AgreementStatus;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.DepositStatus;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.mapper.BookingMapper;
import com.staysure.booking.repository.BookingRepository;
import com.staysure.booking.repository.BookingStatusHistoryRepository;
import com.staysure.booking.repository.RentalAgreementRepository;
import com.staysure.booking.repository.SecurityDepositRepository;
import com.staysure.booking.repository.TenantDocumentRepository;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.enums.DocumentType;
import com.staysure.common.enums.DocumentVerificationStatus;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.service.OwnerService;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.FloorStatus;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.RoomRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService {

    private static final Set<DocumentType> GOVERNMENT_ID_TYPES = Set.of(
            DocumentType.AADHAAR,
            DocumentType.PAN,
            DocumentType.PASSPORT,
            DocumentType.DRIVING_LICENSE
    );

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository historyRepository;
    private final TenantDocumentRepository tenantDocumentRepository;
    private final SecurityDepositRepository securityDepositRepository;
    private final RentalAgreementRepository rentalAgreementRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final PgPropertyRepository propertyRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final UserService userService;
    private final OwnerService ownerService;
    private final BookingMapper bookingMapper;
    private final AuditService auditService;

    public BookingService(BookingRepository bookingRepository,
                          BookingStatusHistoryRepository historyRepository,
                          TenantDocumentRepository tenantDocumentRepository,
                          SecurityDepositRepository securityDepositRepository,
                          RentalAgreementRepository rentalAgreementRepository,
                          TenantProfileRepository tenantProfileRepository,
                          PgPropertyRepository propertyRepository,
                          RoomRepository roomRepository,
                          BedRepository bedRepository,
                          UserService userService,
                          OwnerService ownerService,
                          BookingMapper bookingMapper,
                          AuditService auditService) {
        this.bookingRepository = bookingRepository;
        this.historyRepository = historyRepository;
        this.tenantDocumentRepository = tenantDocumentRepository;
        this.securityDepositRepository = securityDepositRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.tenantProfileRepository = tenantProfileRepository;
        this.propertyRepository = propertyRepository;
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.userService = userService;
        this.ownerService = ownerService;
        this.bookingMapper = bookingMapper;
        this.auditService = auditService;
    }

    @Transactional
    public BookingResponse request(Long userId, CreateBookingRequest request, String ipAddress) {
        User user = userService.getUser(userId);
        PgProperty property = propertyRepository.findById(request.propertyId())
                .orElseThrow(() -> new ResourceNotFoundException("PG not found"));
        if (property.getStatus() != PropertyStatus.ACTIVE
                || property.getVerificationStatus() != PropertyVerificationStatus.VERIFIED) {
            throw new BusinessRuleException("Only verified active PGs can be booked", "PG_NOT_BOOKABLE");
        }
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        validateRoom(property, room);
        Bed bed = bedRepository.findById(request.bedId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        validateBed(room, bed);
        validateDates(request.moveInDate(), request.expectedMoveOutDate());

        Booking booking = new Booking();
        booking.setBookingNumber(nextBookingNumber());
        booking.setUser(user);
        booking.setProperty(property);
        booking.setRoom(room);
        booking.setBed(bed);
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setMoveInDate(request.moveInDate());
        booking.setExpectedMoveOutDate(request.expectedMoveOutDate());
        booking.setMonthlyRent(room.getMonthlyRent());
        booking.setSecurityDeposit(room.getSecurityDeposit());
        booking.setRequestedAt(LocalDateTime.now());
        booking.setRemarks(blankToNull(request.remarks()));
        Booking saved = bookingRepository.save(booking);
        recordHistory(saved, null, BookingStatus.REQUESTED, user, "Booking requested");
        auditService.log(user, "BOOKING_REQUESTED", "BOOKING", "Booking", saved.getId(),
                "Booking requested", null, BookingStatus.REQUESTED.name(), ipAddress);
        return response(saved);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listForUser(Long userId) {
        User user = userService.getUser(userId);
        return bookingRepository.findAllByUserOrderByCreatedAtDesc(user).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getForUser(Long userId, Long bookingId) {
        return response(getUserBooking(userId, bookingId));
    }

    @Transactional(readOnly = true)
    public Booking getUserBooking(Long userId, Long bookingId) {
        User user = userService.getUser(userId);
        return bookingRepository.findByIdAndUser(bookingId, user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found", "BOOKING_NOT_FOUND"));
    }

    @Transactional
    public BookingResponse cancel(Long userId, Long bookingId, String remarks, String ipAddress) {
        Booking booking = getUserBooking(userId, bookingId);
        User actor = booking.getUser();
        if (!List.of(BookingStatus.REQUESTED, BookingStatus.AWAITING_KYC, BookingStatus.KYC_VERIFICATION,
                BookingStatus.AWAITING_DEPOSIT, BookingStatus.AWAITING_AGREEMENT).contains(booking.getStatus())) {
            throw new BusinessRuleException("Booking cannot be cancelled from current status", "INVALID_BOOKING_TRANSITION");
        }
        releaseReservedBedIfNeeded(booking, actor, ipAddress);
        booking.setCancellationReason(blankToNull(remarks));
        booking.setCancelledAt(LocalDateTime.now());
        transition(booking, BookingStatus.CANCELLED, actor, remarks);
        auditService.log(actor, "BOOKING_CANCELLED", "BOOKING", "Booking", booking.getId(),
                "Booking cancelled", null, BookingStatus.CANCELLED.name(), ipAddress);
        return response(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> listForOwner(Long userId) {
        OwnerProfile owner = ownerService.getCurrentOwner(userId);
        return bookingRepository.findAllByOwner(owner).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getForOwner(Long userId, Long bookingId) {
        return response(getOwnerBooking(userId, bookingId));
    }

    @Transactional(readOnly = true)
    public Booking getOwnerBooking(Long userId, Long bookingId) {
        OwnerProfile owner = ownerService.getCurrentOwner(userId);
        return bookingRepository.findByIdAndOwner(bookingId, owner)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found", "BOOKING_NOT_FOUND"));
    }

    @Transactional
    public BookingResponse approve(Long userId, Long bookingId, String remarks, String ipAddress) {
        User actor = userService.getUser(userId);
        Booking booking = getOwnerBooking(userId, bookingId);
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BusinessRuleException("Only requested bookings can be approved", "INVALID_BOOKING_TRANSITION");
        }
        Bed lockedBed = bedRepository.findLockedById(booking.getBed().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        if (lockedBed.getStatus() != BedStatus.AVAILABLE) {
            throw new BusinessRuleException("Selected bed is not available", "BED_NOT_AVAILABLE");
        }
        lockedBed.setStatus(BedStatus.RESERVED);
        bedRepository.save(lockedBed);
        booking.setBed(lockedBed);
        booking.setApprovedAt(LocalDateTime.now());
        transition(booking, BookingStatus.AWAITING_KYC, actor, remarks);
        auditService.log(actor, "BOOKING_APPROVED", "BOOKING", "Booking", booking.getId(),
                "Booking approved", BookingStatus.REQUESTED.name(), BookingStatus.AWAITING_KYC.name(), ipAddress);
        auditService.log(actor, "BED_RESERVED", "BOOKING", "Bed", lockedBed.getId(),
                "Bed reserved for booking", BedStatus.AVAILABLE.name(), BedStatus.RESERVED.name(), ipAddress);
        return response(booking);
    }

    @Transactional
    public BookingResponse reject(Long userId, Long bookingId, String remarks, String ipAddress) {
        User actor = userService.getUser(userId);
        Booking booking = getOwnerBooking(userId, bookingId);
        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BusinessRuleException("Only requested bookings can be rejected", "INVALID_BOOKING_TRANSITION");
        }
        String reason = requireRemarks(remarks);
        booking.setRejectedAt(LocalDateTime.now());
        booking.setRejectionReason(reason);
        transition(booking, BookingStatus.REJECTED, actor, reason);
        auditService.log(actor, "BOOKING_REJECTED", "BOOKING", "Booking", booking.getId(),
                "Booking rejected", BookingStatus.REQUESTED.name(), BookingStatus.REJECTED.name(), ipAddress);
        return response(booking);
    }

    @Transactional(readOnly = true)
    public BookingResponse myPg(Long userId) {
        User user = userService.getUser(userId);
        return bookingRepository.findAllByUserAndStatusInOrderByCreatedAtDesc(
                        user, List.of(BookingStatus.CHECKED_IN, BookingStatus.CONFIRMED)).stream()
                .findFirst()
                .map(this::response)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Current PG not found", "MY_PG_NOT_FOUND"));
    }

    @Transactional
    public void markKycInVerification(Booking booking, User actor, String ipAddress) {
        if (booking.getStatus() == BookingStatus.AWAITING_KYC) {
            transition(booking, BookingStatus.KYC_VERIFICATION, actor, "KYC documents submitted");
            auditService.log(actor, "BOOKING_KYC_SUBMITTED", "BOOKING", "Booking", booking.getId(),
                    "Booking moved to KYC verification", BookingStatus.AWAITING_KYC.name(),
                    BookingStatus.KYC_VERIFICATION.name(), ipAddress);
        }
    }

    @Transactional
    public void evaluateBookingReadiness(Booking booking, User actor, String ipAddress) {
        Booking managed = bookingRepository.findLockedById(booking.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found", "BOOKING_NOT_FOUND"));
        if (managed.getStatus() == BookingStatus.KYC_VERIFICATION && kycComplete(managed)) {
            ensureDeposit(managed);
            transition(managed, BookingStatus.AWAITING_DEPOSIT, actor, "KYC requirements verified");
        }
        if (managed.getStatus() == BookingStatus.AWAITING_DEPOSIT && depositPaid(managed)) {
            transition(managed, BookingStatus.AWAITING_AGREEMENT, actor, "Security deposit paid");
        }
        if (managed.getStatus() == BookingStatus.AWAITING_AGREEMENT
                && kycComplete(managed)
                && depositPaid(managed)
                && agreementAccepted(managed)) {
            managed.setConfirmedAt(LocalDateTime.now());
            transition(managed, BookingStatus.CONFIRMED, actor, "Booking confirmed");
            auditService.log(actor, "BOOKING_CONFIRMED", "BOOKING", "Booking", managed.getId(),
                    "Booking confirmed", BookingStatus.AWAITING_AGREEMENT.name(), BookingStatus.CONFIRMED.name(), ipAddress);
            createTenantIfMissing(managed, actor, ipAddress);
        }
    }

    public BookingResponse response(Booking booking) {
        List<TenantDocument> documents = tenantDocumentRepository.findAllByBookingOrderByCreatedAtDesc(booking);
        SecurityDeposit deposit = securityDepositRepository.findByBooking(booking).orElse(null);
        RentalAgreement agreement = rentalAgreementRepository.findByBooking(booking).orElse(null);
        TenantProfile tenant = tenantProfileRepository.findByBooking(booking).orElse(null);
        List<BookingStatusHistory> history = historyRepository.findAllByBookingOrderByCreatedAtAsc(booking);
        return bookingMapper.toResponse(booking, documents, deposit, agreement, tenant, history);
    }

    public void transition(Booking booking, BookingStatus newStatus, User actor, String remarks) {
        BookingStatus previous = booking.getStatus();
        booking.setStatus(newStatus);
        bookingRepository.save(booking);
        recordHistory(booking, previous, newStatus, actor, remarks);
    }

    public SecurityDeposit ensureDeposit(Booking booking) {
        return securityDepositRepository.findByBooking(booking).orElseGet(() -> {
            SecurityDeposit deposit = new SecurityDeposit();
            deposit.setBooking(booking);
            deposit.setRequiredAmount(booking.getSecurityDeposit());
            deposit.setPaidAmount(BigDecimal.ZERO);
            deposit.setStatus(DepositStatus.PENDING);
            return securityDepositRepository.save(deposit);
        });
    }

    public boolean kycComplete(Booking booking) {
        boolean hasGovId = tenantDocumentRepository.existsVerifiedByTypeIn(
                booking, DocumentVerificationStatus.VERIFIED, GOVERNMENT_ID_TYPES);
        boolean hasPhoto = tenantDocumentRepository.existsVerifiedByTypeIn(
                booking, DocumentVerificationStatus.VERIFIED, Set.of(DocumentType.PHOTO));
        return hasGovId && hasPhoto;
    }

    public boolean depositPaid(Booking booking) {
        return securityDepositRepository.findByBooking(booking)
                .map(deposit -> deposit.getStatus() == DepositStatus.PAID)
                .orElse(false);
    }

    public boolean agreementAccepted(Booking booking) {
        return rentalAgreementRepository.findByBooking(booking)
                .map(agreement -> agreement.getStatus() == AgreementStatus.ACCEPTED)
                .orElse(false);
    }

    private void createTenantIfMissing(Booking booking, User actor, String ipAddress) {
        if (tenantProfileRepository.findByBooking(booking).isPresent()) {
            return;
        }
        TenantProfile tenant = new TenantProfile();
        tenant.setBooking(booking);
        tenant.setUser(booking.getUser());
        tenant.setProperty(booking.getProperty());
        tenant.setRoom(booking.getRoom());
        tenant.setBed(booking.getBed());
        tenant.setStatus(TenantStatus.UPCOMING);
        tenant.setExpectedCheckoutDate(booking.getExpectedMoveOutDate());
        TenantProfile saved = tenantProfileRepository.save(tenant);
        auditService.log(actor, "TENANT_CREATED", "BOOKING", "TenantProfile", saved.getId(),
                "Tenant profile created for confirmed booking", null, TenantStatus.UPCOMING.name(), ipAddress);
    }

    private void releaseReservedBedIfNeeded(Booking booking, User actor, String ipAddress) {
        if (booking.getStatus() == BookingStatus.REQUESTED) {
            return;
        }
        Bed lockedBed = bedRepository.findLockedById(booking.getBed().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Bed not found"));
        if (lockedBed.getStatus() == BedStatus.RESERVED) {
            lockedBed.setStatus(BedStatus.AVAILABLE);
            bedRepository.save(lockedBed);
            auditService.log(actor, "BED_RELEASED", "BOOKING", "Bed", lockedBed.getId(),
                    "Reserved bed released after booking cancellation", BedStatus.RESERVED.name(),
                    BedStatus.AVAILABLE.name(), ipAddress);
        }
    }

    private void recordHistory(Booking booking, BookingStatus previousStatus, BookingStatus newStatus,
                               User actor, String remarks) {
        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(blankToNull(remarks));
        history.setActionBy(actor);
        historyRepository.save(history);
    }

    private void validateRoom(PgProperty property, Room room) {
        if (!Objects.equals(room.getFloor().getProperty().getId(), property.getId())) {
            throw new BusinessRuleException("Room does not belong to selected PG", "ROOM_PROPERTY_MISMATCH");
        }
        if (room.getStatus() != RoomStatus.ACTIVE || room.getFloor().getStatus() != FloorStatus.ACTIVE) {
            throw new BusinessRuleException("Room is not active", "ROOM_NOT_BOOKABLE");
        }
    }

    private void validateBed(Room room, Bed bed) {
        if (!Objects.equals(bed.getRoom().getId(), room.getId())) {
            throw new BusinessRuleException("Bed does not belong to selected room", "BED_ROOM_MISMATCH");
        }
        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new BusinessRuleException("Selected bed is not available", "BED_NOT_AVAILABLE");
        }
    }

    private void validateDates(LocalDate moveInDate, LocalDate expectedMoveOutDate) {
        if (moveInDate == null || moveInDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Move-in date must be today or later", "INVALID_MOVE_IN_DATE");
        }
        if (expectedMoveOutDate != null && !expectedMoveOutDate.isAfter(moveInDate)) {
            throw new BusinessRuleException("Expected move-out date must be after move-in date", "INVALID_MOVE_OUT_DATE");
        }
    }

    private String nextBookingNumber() {
        return "BK-" + LocalDate.now().toString().replace("-", "") + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String requireRemarks(String value) {
        String cleaned = blankToNull(value);
        if (cleaned == null) {
            throw new BusinessRuleException("Remarks are required", "REMARKS_REQUIRED");
        }
        return cleaned;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
