package com.staysure;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.dto.CreateBookingRequest;
import com.staysure.booking.dto.RecordDepositRequest;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.AgreementStatus;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.DepositStatus;
import com.staysure.booking.enums.PaymentMethod;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.mapper.BookingMapper;
import com.staysure.booking.repository.BookingRepository;
import com.staysure.booking.repository.BookingStatusHistoryRepository;
import com.staysure.booking.repository.RentalAgreementRepository;
import com.staysure.booking.repository.SecurityDepositRepository;
import com.staysure.booking.repository.TenantDocumentRepository;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.booking.service.BookingService;
import com.staysure.booking.service.CheckInService;
import com.staysure.booking.service.RentalAgreementService;
import com.staysure.booking.service.SecurityDepositService;
import com.staysure.booking.service.TenantDocumentService;
import com.staysure.common.enums.DocumentType;
import com.staysure.common.enums.DocumentVerificationStatus;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.service.FileStorageService;
import com.staysure.owner.service.OwnerService;
import com.staysure.owner.service.StoredFile;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.Floor;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.FloorStatus;
import com.staysure.property.enums.FurnishingType;
import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyType;
import com.staysure.property.enums.PropertyVerificationStatus;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.enums.SharingType;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.RoomRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class Phase4ServiceTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingStatusHistoryRepository historyRepository;
    @Mock private TenantDocumentRepository tenantDocumentRepository;
    @Mock private SecurityDepositRepository securityDepositRepository;
    @Mock private RentalAgreementRepository rentalAgreementRepository;
    @Mock private TenantProfileRepository tenantProfileRepository;
    @Mock private PgPropertyRepository propertyRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private BedRepository bedRepository;
    @Mock private UserService userService;
    @Mock private OwnerService ownerService;
    @Mock private FileStorageService fileStorageService;
    @Mock private AuditService auditService;

    private BookingMapper mapper;
    private BookingService bookingService;
    private TenantDocumentService documentService;
    private SecurityDepositService depositService;
    private RentalAgreementService agreementService;
    private CheckInService checkInService;

    @BeforeEach
    void setUp() {
        mapper = new BookingMapper();
        bookingService = new BookingService(
                bookingRepository, historyRepository, tenantDocumentRepository, securityDepositRepository,
                rentalAgreementRepository, tenantProfileRepository, propertyRepository, roomRepository, bedRepository,
                userService, ownerService, mapper, auditService
        );
        documentService = new TenantDocumentService(
                tenantDocumentRepository, bookingService, fileStorageService, mapper, userService, auditService
        );
        depositService = new SecurityDepositService(
                securityDepositRepository, bookingService, mapper, userService, auditService
        );
        agreementService = new RentalAgreementService(
                rentalAgreementRepository, bookingService, mapper, fileStorageService, userService, auditService
        );
        checkInService = new CheckInService(bookingService, tenantProfileRepository, bedRepository, userService, auditService);
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> {
            Booking booking = invocation.getArgument(0);
            if (booking.getId() == null) booking.setId(100L);
            return booking;
        });
        when(historyRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(securityDepositRepository.save(any(SecurityDeposit.class))).thenAnswer(invocation -> {
            SecurityDeposit deposit = invocation.getArgument(0);
            if (deposit.getId() == null) deposit.setId(50L);
            return deposit;
        });
        when(rentalAgreementRepository.save(any(RentalAgreement.class))).thenAnswer(invocation -> {
            RentalAgreement agreement = invocation.getArgument(0);
            if (agreement.getId() == null) agreement.setId(60L);
            return agreement;
        });
        when(tenantProfileRepository.save(any(TenantProfile.class))).thenAnswer(invocation -> {
            TenantProfile tenant = invocation.getArgument(0);
            if (tenant.getId() == null) tenant.setId(70L);
            return tenant;
        });
        when(tenantDocumentRepository.findAllByBookingOrderByCreatedAtDesc(any())).thenReturn(List.of());
        when(historyRepository.findAllByBookingOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(securityDepositRepository.findByBooking(any())).thenReturn(Optional.empty());
        when(rentalAgreementRepository.findByBooking(any())).thenReturn(Optional.empty());
        when(tenantProfileRepository.findByBooking(any())).thenReturn(Optional.empty());
    }

    @Test
    void validBookingRequestUsesTrustedRoomPricing() {
        User user = user(1L);
        PgProperty property = property(10L, PropertyVerificationStatus.VERIFIED, PropertyStatus.ACTIVE);
        Room room = room(20L, property);
        Bed bed = bed(30L, room, BedStatus.AVAILABLE);
        when(userService.getUser(1L)).thenReturn(user);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(roomRepository.findById(20L)).thenReturn(Optional.of(room));
        when(bedRepository.findById(30L)).thenReturn(Optional.of(bed));

        var response = bookingService.request(1L, new CreateBookingRequest(
                10L, 20L, 30L, LocalDate.now().plusDays(1), LocalDate.now().plusMonths(6), "near office"
        ), "ip");

        assertThat(response.status()).isEqualTo(BookingStatus.REQUESTED);
        assertThat(response.monthlyRent()).isEqualByComparingTo(room.getMonthlyRent());
        assertThat(response.securityDeposit()).isEqualByComparingTo(room.getSecurityDeposit());
        verify(bookingRepository).save(any(Booking.class));
    }

    @Test
    void bookingRequestBlocksUnverifiedInactiveUnavailableAndMismatchedBeds() {
        User user = user(1L);
        PgProperty pending = property(10L, PropertyVerificationStatus.PENDING, PropertyStatus.ACTIVE);
        PgProperty active = property(11L, PropertyVerificationStatus.VERIFIED, PropertyStatus.ACTIVE);
        Room room = room(20L, active);
        Bed occupied = bed(30L, room, BedStatus.OCCUPIED);
        when(userService.getUser(1L)).thenReturn(user);
        when(propertyRepository.findById(10L)).thenReturn(Optional.of(pending));

        assertThatThrownBy(() -> bookingService.request(1L, request(10L, 20L, 30L), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("PG_NOT_BOOKABLE"));

        when(propertyRepository.findById(11L)).thenReturn(Optional.of(active));
        when(roomRepository.findById(20L)).thenReturn(Optional.of(room));
        when(bedRepository.findById(30L)).thenReturn(Optional.of(occupied));
        assertThatThrownBy(() -> bookingService.request(1L, request(11L, 20L, 30L), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("BED_NOT_AVAILABLE"));

        Bed otherRoomBed = bed(31L, room(21L, active), BedStatus.AVAILABLE);
        when(bedRepository.findById(31L)).thenReturn(Optional.of(otherRoomBed));
        assertThatThrownBy(() -> bookingService.request(1L, request(11L, 20L, 31L), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("BED_ROOM_MISMATCH"));
        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void ownerApprovalReservesBedAndUnavailableBedBlocksDoubleBooking() {
        OwnerProfile owner = owner(2L);
        User actor = owner.getUser();
        Booking booking = booking(100L, BookingStatus.REQUESTED, owner);
        when(userService.getUser(2L)).thenReturn(actor);
        when(ownerService.getCurrentOwner(2L)).thenReturn(owner);
        when(bookingRepository.findByIdAndOwner(100L, owner)).thenReturn(Optional.of(booking));
        when(bedRepository.findLockedById(booking.getBed().getId())).thenReturn(Optional.of(booking.getBed()));

        bookingService.approve(2L, 100L, null, "ip");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.AWAITING_KYC);
        assertThat(booking.getBed().getStatus()).isEqualTo(BedStatus.RESERVED);

        Booking second = booking(101L, BookingStatus.REQUESTED, owner);
        second.setBed(booking.getBed());
        when(bookingRepository.findByIdAndOwner(101L, owner)).thenReturn(Optional.of(second));
        assertThatThrownBy(() -> bookingService.approve(2L, 101L, null, "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("BED_NOT_AVAILABLE"));
    }

    @Test
    void cancellationReleasesReservedBed() {
        User user = user(1L);
        Booking booking = booking(100L, BookingStatus.AWAITING_KYC, owner(2L));
        booking.setUser(user);
        booking.getBed().setStatus(BedStatus.RESERVED);
        when(userService.getUser(1L)).thenReturn(user);
        when(bookingRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(booking));
        when(bedRepository.findLockedById(booking.getBed().getId())).thenReturn(Optional.of(booking.getBed()));

        bookingService.cancel(1L, 100L, "changed plans", "ip");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
        assertThat(booking.getBed().getStatus()).isEqualTo(BedStatus.AVAILABLE);
    }

    @Test
    void kycUploadMovesBookingToVerificationAndRequiredKycMovesToDeposit() {
        User user = user(1L);
        Booking booking = booking(100L, BookingStatus.AWAITING_KYC, owner(2L));
        booking.setUser(user);
        when(userService.getUser(1L)).thenReturn(user);
        when(bookingRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(booking));
        when(fileStorageService.storeTenantDocument(eq(100L), any(MultipartFile.class)))
                .thenReturn(new StoredFile("/uploads/tenant-documents/100/a.pdf", "a.pdf", "application/pdf", 10));
        when(tenantDocumentRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        documentService.upload(1L, 100L, DocumentType.AADHAAR, "1234", file(), "ip");
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.KYC_VERIFICATION);

        when(bookingRepository.findLockedById(100L)).thenReturn(Optional.of(booking));
        when(tenantDocumentRepository.existsVerifiedByTypeIn(eq(booking), eq(DocumentVerificationStatus.VERIFIED), any()))
                .thenReturn(true);

        bookingService.evaluateBookingReadiness(booking, user, "ip");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.AWAITING_DEPOSIT);
        verify(securityDepositRepository).save(any(SecurityDeposit.class));
    }

    @Test
    void fullDepositMovesBookingToAgreement() {
        OwnerProfile owner = owner(2L);
        User actor = owner.getUser();
        Booking booking = booking(100L, BookingStatus.AWAITING_DEPOSIT, owner);
        SecurityDeposit deposit = deposit(booking, BigDecimal.valueOf(1000), BigDecimal.valueOf(400), DepositStatus.PARTIALLY_PAID);
        when(userService.getUser(2L)).thenReturn(actor);
        when(ownerService.getCurrentOwner(2L)).thenReturn(owner);
        when(bookingRepository.findByIdAndOwner(100L, owner)).thenReturn(Optional.of(booking));
        when(bookingRepository.findLockedById(100L)).thenReturn(Optional.of(booking));
        when(securityDepositRepository.findByBooking(booking)).thenReturn(Optional.of(deposit));

        depositService.record(2L, 100L, new RecordDepositRequest(BigDecimal.valueOf(600), PaymentMethod.UPI, "upi-1", null), "ip");

        assertThat(deposit.getStatus()).isEqualTo(DepositStatus.PAID);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.AWAITING_AGREEMENT);
    }

    @Test
    void agreementAcceptanceConfirmsBookingAndCreatesUpcomingTenantOnce() {
        User user = user(1L);
        Booking booking = booking(100L, BookingStatus.AWAITING_AGREEMENT, owner(2L));
        booking.setUser(user);
        RentalAgreement agreement = agreement(booking, AgreementStatus.ISSUED);
        SecurityDeposit deposit = deposit(booking, BigDecimal.valueOf(1000), BigDecimal.valueOf(1000), DepositStatus.PAID);
        when(userService.getUser(1L)).thenReturn(user);
        when(bookingRepository.findByIdAndUser(100L, user)).thenReturn(Optional.of(booking));
        when(bookingRepository.findLockedById(100L)).thenReturn(Optional.of(booking));
        when(rentalAgreementRepository.findByBooking(booking)).thenReturn(Optional.of(agreement));
        when(securityDepositRepository.findByBooking(booking)).thenReturn(Optional.of(deposit));
        when(tenantDocumentRepository.existsVerifiedByTypeIn(any(), any(), any())).thenReturn(true);

        agreementService.accept(1L, 100L, "ip");

        assertThat(agreement.getStatus()).isEqualTo(AgreementStatus.ACCEPTED);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
        verify(tenantProfileRepository).save(any(TenantProfile.class));

        TenantProfile existing = new TenantProfile();
        existing.setId(70L);
        existing.setBooking(booking);
        when(tenantProfileRepository.findByBooking(booking)).thenReturn(Optional.of(existing));
        bookingService.evaluateBookingReadiness(booking, user, "ip");
        verify(tenantProfileRepository).save(any(TenantProfile.class));
    }

    @Test
    void checkInMovesBookingTenantAndBedToActiveOccupied() {
        OwnerProfile owner = owner(2L);
        User actor = owner.getUser();
        Booking booking = booking(100L, BookingStatus.CONFIRMED, owner);
        booking.getBed().setStatus(BedStatus.RESERVED);
        TenantProfile tenant = new TenantProfile();
        tenant.setId(70L);
        tenant.setBooking(booking);
        tenant.setUser(booking.getUser());
        tenant.setProperty(booking.getProperty());
        tenant.setRoom(booking.getRoom());
        tenant.setBed(booking.getBed());
        tenant.setStatus(TenantStatus.UPCOMING);
        when(userService.getUser(2L)).thenReturn(actor);
        when(ownerService.getCurrentOwner(2L)).thenReturn(owner);
        when(bookingRepository.findByIdAndOwner(100L, owner)).thenReturn(Optional.of(booking));
        when(tenantProfileRepository.findByBooking(booking)).thenReturn(Optional.of(tenant));
        when(bedRepository.findLockedById(booking.getBed().getId())).thenReturn(Optional.of(booking.getBed()));

        checkInService.checkIn(2L, 100L, "ip");

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CHECKED_IN);
        assertThat(tenant.getStatus()).isEqualTo(TenantStatus.ACTIVE);
        assertThat(booking.getBed().getStatus()).isEqualTo(BedStatus.OCCUPIED);
    }

    @Test
    void userOwnershipAndOwnerOwnershipAreProtected() {
        User user = user(1L);
        OwnerProfile owner = owner(2L);
        when(userService.getUser(1L)).thenReturn(user);
        when(ownerService.getCurrentOwner(2L)).thenReturn(owner);
        when(bookingRepository.findByIdAndUser(100L, user)).thenReturn(Optional.empty());
        when(bookingRepository.findByIdAndOwner(100L, owner)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.getForUser(1L, 100L)).isInstanceOf(ApiException.class);
        assertThatThrownBy(() -> bookingService.getForOwner(2L, 100L)).isInstanceOf(ApiException.class);
    }

    private CreateBookingRequest request(Long propertyId, Long roomId, Long bedId) {
        return new CreateBookingRequest(propertyId, roomId, bedId, LocalDate.now().plusDays(1), null, null);
    }

    private MockMultipartFile file() {
        return new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[]{1});
    }

    private Booking booking(Long id, BookingStatus status, OwnerProfile owner) {
        PgProperty property = property(10L, PropertyVerificationStatus.VERIFIED, PropertyStatus.ACTIVE);
        property.setOwner(owner);
        Room room = room(20L, property);
        Bed bed = bed(30L, room, BedStatus.AVAILABLE);
        Booking booking = new Booking();
        booking.setId(id);
        booking.setBookingNumber("BK-1");
        booking.setUser(user(1L));
        booking.setProperty(property);
        booking.setRoom(room);
        booking.setBed(bed);
        booking.setStatus(status);
        booking.setMoveInDate(LocalDate.now().plusDays(1));
        booking.setExpectedMoveOutDate(LocalDate.now().plusMonths(6));
        booking.setMonthlyRent(room.getMonthlyRent());
        booking.setSecurityDeposit(room.getSecurityDeposit());
        booking.setRequestedAt(java.time.LocalDateTime.now());
        return booking;
    }

    private SecurityDeposit deposit(Booking booking, BigDecimal required, BigDecimal paid, DepositStatus status) {
        SecurityDeposit deposit = new SecurityDeposit();
        deposit.setId(50L);
        deposit.setBooking(booking);
        deposit.setRequiredAmount(required);
        deposit.setPaidAmount(paid);
        deposit.setStatus(status);
        return deposit;
    }

    private RentalAgreement agreement(Booking booking, AgreementStatus status) {
        RentalAgreement agreement = new RentalAgreement();
        agreement.setId(60L);
        agreement.setBooking(booking);
        agreement.setAgreementNumber("AG-1");
        agreement.setStatus(status);
        agreement.setStartDate(booking.getMoveInDate());
        agreement.setMonthlyRent(booking.getMonthlyRent());
        agreement.setSecurityDeposit(booking.getSecurityDeposit());
        agreement.setNoticePeriodDays(30);
        agreement.setLockInMonths(1);
        agreement.setIssuedAt(java.time.LocalDateTime.now());
        return agreement;
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setEmail("user" + id + "@example.com");
        user.setPhone("99999999" + id);
        return user;
    }

    private OwnerProfile owner(Long userId) {
        OwnerProfile owner = new OwnerProfile();
        owner.setId(userId);
        owner.setUser(user(userId));
        owner.setBusinessName("Owner " + userId);
        owner.setVerificationStatus(OwnerVerificationStatus.VERIFIED);
        return owner;
    }

    private PgProperty property(Long id, PropertyVerificationStatus verificationStatus, PropertyStatus status) {
        PgProperty property = new PgProperty();
        property.setId(id);
        property.setOwner(owner(2L));
        property.setName("Sai Residency");
        property.setSlug("sai-residency");
        property.setGenderType(GenderType.COED);
        property.setPropertyType(PropertyType.PG);
        property.setAddressLine1("Line 1");
        property.setArea("Hinjawadi");
        property.setCity("Pune");
        property.setState("MH");
        property.setPincode("411057");
        property.setStartingRent(BigDecimal.valueOf(8000));
        property.setSecurityDeposit(BigDecimal.valueOf(10000));
        property.setNoticePeriodDays(30);
        property.setLockInMonths(1);
        property.setVerificationStatus(verificationStatus);
        property.setStatus(status);
        return property;
    }

    private Room room(Long id, PgProperty property) {
        Floor floor = new Floor();
        floor.setId(id + 100);
        floor.setProperty(property);
        floor.setName("Floor 1");
        floor.setFloorNumber(1);
        floor.setStatus(FloorStatus.ACTIVE);
        Room room = new Room();
        room.setId(id);
        room.setFloor(floor);
        room.setRoomNumber("101");
        room.setSharingType(SharingType.DOUBLE);
        room.setCapacity(2);
        room.setMonthlyRent(BigDecimal.valueOf(8000));
        room.setSecurityDeposit(BigDecimal.valueOf(10000));
        room.setAcAvailable(true);
        room.setAttachedBathroom(true);
        room.setFurnishingType(FurnishingType.FULLY_FURNISHED);
        room.setStatus(RoomStatus.ACTIVE);
        return room;
    }

    private Bed bed(Long id, Room room, BedStatus status) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setRoom(room);
        bed.setBedNumber("A");
        bed.setBedLabel("Bed A");
        bed.setStatus(status);
        return bed;
    }
}
