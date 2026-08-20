package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.booking.dto.CreateBookingRequest;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.BookingStatusHistory;
import com.staysure.booking.repository.BookingRepository;
import com.staysure.booking.repository.BookingStatusHistoryRepository;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import com.staysure.owner.repository.OwnerProfileRepository;
import com.staysure.owner.entity.OwnerProfile;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingStatusHistoryRepository statusHistoryRepository;
    private final BedRepository bedRepository;
    private final PgPropertyRepository pgPropertyRepository;
    private final OwnerProfileRepository ownerProfileRepository;
    private final UserService userService;
    private final AuditService auditService;

    public BookingService(BookingRepository bookingRepository,
                          BookingStatusHistoryRepository statusHistoryRepository,
                          BedRepository bedRepository,
                          PgPropertyRepository pgPropertyRepository,
                          OwnerProfileRepository ownerProfileRepository,
                          UserService userService,
                          AuditService auditService) {
        this.bookingRepository = bookingRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.bedRepository = bedRepository;
        this.pgPropertyRepository = pgPropertyRepository;
        this.ownerProfileRepository = ownerProfileRepository;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional
    public Booking createBooking(Long userId, CreateBookingRequest request, String ipAddress) {
        if (userId == null || request == null || request.bedId() == null) {
            throw new BusinessRuleException("Invalid booking request", "INVALID_BOOKING_REQUEST");
        }
        Long bedId = Objects.requireNonNull(request.bedId(), "bedId must not be null");
        User user = userService.getUser(userId);

        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bed not found", "BED_NOT_FOUND"));
        Room room = bed.getRoom();
        PgProperty property = room.getFloor().getProperty();

        validateBookingEligibility(property, room, bed, request.moveInDate());

        if (bookingRepository.findByBedIdAndStatusIn(bedId, List.of(
                BookingStatus.REQUESTED, BookingStatus.APPROVED, BookingStatus.AWAITING_KYC,
                BookingStatus.KYC_VERIFICATION, BookingStatus.AWAITING_DEPOSIT,
                BookingStatus.AWAITING_AGREEMENT, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN
        )).isPresent()) {
            throw new BusinessRuleException("Bed is already booked", "BED_NOT_AVAILABLE");
        }

        Booking booking = new Booking();
        booking.setBookingNumber(generateBookingNumber());
        booking.setUser(user);
        booking.setProperty(property);
        booking.setRoom(room);
        booking.setBed(bed);
        booking.setMoveInDate(request.moveInDate());
        booking.setExpectedMoveOutDate(request.expectedMoveOutDate());
        booking.setMonthlyRent(room.getMonthlyRent());
        booking.setSecurityDepositAmount(room.getSecurityDeposit());
        booking.setStatus(BookingStatus.REQUESTED);
        booking.setUserRemarks(request.remarks());

        Booking saved = bookingRepository.save(booking);
        recordStatusHistory(saved, null, BookingStatus.REQUESTED, "Booking requested", user);

        auditService.log(user, "BOOKING_REQUESTED", "BOOKING", "Booking", saved.getId(),
                "Booking requested", null, saved.getBookingNumber(), ipAddress);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<Booking> getUserBookings(Long userId) {
        User user = userService.getUser(userId);
        return bookingRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Transactional(readOnly = true)
    public Booking getUserBooking(Long userId, Long bookingId) {
        if (userId == null || bookingId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User user = userService.getUser(userId);
        Booking booking = getBooking(bookingId);
        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Booking access denied", "BOOKING_ACCESS_DENIED");
        }
        return booking;
    }

    @Transactional
    public void cancelBooking(Long userId, Long bookingId, String ipAddress) {
        if (userId == null || bookingId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User user = userService.getUser(userId);
        Booking booking = getBooking(bookingId);

        if (!booking.getUser().getId().equals(user.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Booking access denied", "BOOKING_ACCESS_DENIED");
        }

        if (booking.getStatus() == BookingStatus.CHECKED_IN || booking.getStatus() == BookingStatus.CHECKED_OUT) {
            throw new BusinessRuleException("Cannot cancel booking in current status", "INVALID_BOOKING_STATUS");
        }

        BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepository.save(booking);

        if (previousStatus == BookingStatus.APPROVED || previousStatus == BookingStatus.AWAITING_KYC ||
            previousStatus == BookingStatus.KYC_VERIFICATION || previousStatus == BookingStatus.AWAITING_DEPOSIT ||
            previousStatus == BookingStatus.AWAITING_AGREEMENT || previousStatus == BookingStatus.CONFIRMED) {
            Bed bed = booking.getBed();
            bed.setStatus(BedStatus.AVAILABLE);
            bedRepository.save(bed);
            auditService.log(user, "BED_RELEASED", "BED", "Bed", bed.getId(),
                    "Bed released due to booking cancellation", null, bed.getBedNumber(), ipAddress);
        }

        recordStatusHistory(booking, previousStatus, BookingStatus.CANCELLED, "Booking cancelled by user", user);
        auditService.log(user, "BOOKING_CANCELLED", "BOOKING", "Booking", booking.getId(),
                "Booking cancelled", null, booking.getBookingNumber(), ipAddress);
    }

    @Transactional
    public Booking approveBooking(Long ownerId, Long bookingId, String remarks, String ipAddress) {
        if (ownerId == null || bookingId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User owner = userService.getUser(ownerId);
        Booking booking = getBooking(bookingId);

        if (!booking.getProperty().getOwner().getUser().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Booking access denied", "BOOKING_ACCESS_DENIED");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BusinessRuleException("Booking cannot be approved from current status", "INVALID_BOOKING_TRANSITION");
        }

        Long bedId = Objects.requireNonNull(booking.getBed().getId(), "booking bed id must not be null");
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Bed not found", "BED_NOT_FOUND"));

        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new BusinessRuleException("Bed is not available", "BED_NOT_AVAILABLE");
        }

        bed.setStatus(BedStatus.RESERVED);
        bedRepository.save(bed);

        BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BookingStatus.AWAITING_KYC);
        booking.setApprovedAt(LocalDateTime.now());
        booking.setApprovedBy(owner);
        booking.setOwnerRemarks(remarks);
        Booking saved = bookingRepository.save(booking);

        recordStatusHistory(saved, previousStatus, BookingStatus.AWAITING_KYC, remarks, owner);
        auditService.log(owner, "BOOKING_APPROVED", "BOOKING", "Booking", saved.getId(),
                "Booking approved", null, saved.getBookingNumber(), ipAddress);
        auditService.log(owner, "BED_RESERVED", "BED", "Bed", bed.getId(),
                "Bed reserved for booking", null, bed.getBedNumber(), ipAddress);

        return saved;
    }

    @Transactional
    public Booking rejectBooking(Long ownerId, Long bookingId, String reason, String ipAddress) {
        if (ownerId == null || bookingId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User owner = userService.getUser(ownerId);
        Booking booking = getBooking(bookingId);

        if (!booking.getProperty().getOwner().getUser().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Booking access denied", "BOOKING_ACCESS_DENIED");
        }

        if (booking.getStatus() != BookingStatus.REQUESTED) {
            throw new BusinessRuleException("Booking cannot be rejected from current status", "INVALID_BOOKING_TRANSITION");
        }

        BookingStatus previousStatus = booking.getStatus();
        booking.setStatus(BookingStatus.REJECTED);
        booking.setRejectedAt(LocalDateTime.now());
        booking.setRejectedBy(owner);
        booking.setRejectionReason(reason);
        bookingRepository.save(booking);

        recordStatusHistory(booking, previousStatus, BookingStatus.REJECTED, reason, owner);
        auditService.log(owner, "BOOKING_REJECTED", "BOOKING", "Booking", booking.getId(),
                "Booking rejected", null, booking.getBookingNumber(), ipAddress);

        return booking;
    }

    @Transactional(readOnly = true)
    public List<Booking> getOwnerBookings(Long ownerId) {
        if (ownerId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User owner = userService.getUser(ownerId);
        OwnerProfile ownerProfile = ownerProfileRepository.findByUser(owner)
                .orElseThrow(() -> new BusinessRuleException("Owner profile not found", "OWNER_NOT_FOUND"));
        List<PgProperty> properties = pgPropertyRepository.findAllByOwnerAndStatusNotOrderByCreatedAtDesc(
                ownerProfile, PropertyStatus.ARCHIVED
        );
        if (properties.isEmpty()) {
            return List.of();
        }
        return bookingRepository.findByPropertyOrderByCreatedAtDesc(properties.get(0));
    }

    @Transactional(readOnly = true)
    public Booking getOwnerBooking(Long ownerId, Long bookingId) {
        if (ownerId == null || bookingId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User owner = userService.getUser(ownerId);
        Booking booking = getBooking(bookingId);
        if (!booking.getProperty().getOwner().getUser().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Booking access denied", "BOOKING_ACCESS_DENIED");
        }
        return booking;
    }

    private void validateBookingEligibility(PgProperty property, Room room, Bed bed, LocalDate moveInDate) {
        if (property.getStatus() != PropertyStatus.ACTIVE) {
            throw new BusinessRuleException("Property is not active", "PG_NOT_BOOKABLE");
        }
        if (property.getVerificationStatus() != PropertyVerificationStatus.VERIFIED) {
            throw new BusinessRuleException("Property is not verified", "PG_NOT_BOOKABLE");
        }
        if (room.getStatus() != RoomStatus.ACTIVE) {
            throw new BusinessRuleException("Room is not available", "ROOM_NOT_AVAILABLE");
        }
        if (bed.getStatus() != BedStatus.AVAILABLE) {
            throw new BusinessRuleException("Bed is not available", "BED_NOT_AVAILABLE");
        }
        if (moveInDate.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Move-in date cannot be in the past", "INVALID_MOVE_IN_DATE");
        }
    }

    private Booking getBooking(Long bookingId) {
        Long id = Objects.requireNonNull(bookingId, "bookingId must not be null");
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Booking not found", "BOOKING_NOT_FOUND"));
    }

    private void recordStatusHistory(Booking booking, BookingStatus previousStatus, BookingStatus newStatus, String remarks, User actor) {
        BookingStatusHistory history = new BookingStatusHistory();
        history.setBooking(booking);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setChangedBy(actor);
        statusHistoryRepository.save(history);
    }

    private String generateBookingNumber() {
        return "BK-" + LocalDate.now().getYear() + "-" + String.format("%06d", UUID.randomUUID().getLeastSignificantBits() & 0xFFFFF).substring(0, 6);
    }
}
