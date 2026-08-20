package com.staysure;

import com.staysure.audit.service.AuditService;
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
import com.staysure.booking.repository.RentalAgreementRepository;
import com.staysure.booking.repository.SecurityDepositRepository;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.service.OwnerService;
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
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.rent.dto.GenerateRentRequest;
import com.staysure.rent.dto.RecordRentPaymentRequest;
import com.staysure.rent.dto.UpdateRentChargesRequest;
import com.staysure.rent.entity.RentInvoice;
import com.staysure.rent.entity.RentPayment;
import com.staysure.rent.enums.RentInvoiceStatus;
import com.staysure.rent.mapper.RentMapper;
import com.staysure.rent.repository.RentInvoiceRepository;
import com.staysure.rent.repository.RentPaymentRepository;
import com.staysure.rent.service.RentService;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.time.YearMonth;

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
class Phase5RentServiceTest {

    @Mock private RentInvoiceRepository rentInvoiceRepository;
    @Mock private RentPaymentRepository rentPaymentRepository;
    @Mock private TenantProfileRepository tenantProfileRepository;
    @Mock private RentalAgreementRepository rentalAgreementRepository;
    @Mock private SecurityDepositRepository securityDepositRepository;
    @Mock private PgPropertyRepository propertyRepository;
    @Mock private OwnerService ownerService;
    @Mock private UserService userService;
    @Mock private AuditService auditService;

    private RentService rentService;
    private OwnerProfile owner;
    private User ownerUser;
    private User tenantUser;
    private PgProperty property;
    private TenantProfile activeTenant;

    @BeforeEach
    void setUp() {
        RentMapper rentMapper = new RentMapper(new BookingMapper());
        rentService = new RentService(
                rentInvoiceRepository,
                rentPaymentRepository,
                tenantProfileRepository,
                rentalAgreementRepository,
                securityDepositRepository,
                propertyRepository,
                ownerService,
                userService,
                auditService,
                rentMapper,
                5
        );
        owner = owner(2L);
        ownerUser = owner.getUser();
        tenantUser = user(1L);
        property = property(10L, owner);
        activeTenant = tenant(70L, tenantUser, property, TenantStatus.ACTIVE, BigDecimal.valueOf(9000));

        when(userService.getUser(2L)).thenReturn(ownerUser);
        when(userService.getUser(1L)).thenReturn(tenantUser);
        when(ownerService.getCurrentOwner(2L)).thenReturn(owner);
        when(propertyRepository.findByIdAndOwner(10L, owner)).thenReturn(Optional.of(property));
        when(rentInvoiceRepository.existsByInvoiceNumber(any())).thenReturn(false);
        when(rentPaymentRepository.existsByPaymentNumber(any())).thenReturn(false);
        when(rentInvoiceRepository.save(any(RentInvoice.class))).thenAnswer(invocation -> {
            RentInvoice invoice = invocation.getArgument(0);
            if (invoice.getId() == null) invoice.setId(500L);
            return invoice;
        });
        when(rentPaymentRepository.save(any(RentPayment.class))).thenAnswer(invocation -> {
            RentPayment payment = invocation.getArgument(0);
            if (payment.getId() == null) payment.setId(600L);
            payment.setCreatedAt(LocalDateTime.now());
            return payment;
        });
        when(rentPaymentRepository.findAllByRentInvoiceOrderByPaymentDateDescCreatedAtDesc(any())).thenReturn(List.of());
        when(securityDepositRepository.findByBooking(any())).thenReturn(Optional.empty());
    }

    @Test
    void activeTenantInvoiceGeneratedUsingAcceptedAgreementRent() {
        YearMonth billingPeriod = YearMonth.now().plusMonths(1);
        RentalAgreement agreement = agreement(activeTenant.getBooking(), BigDecimal.valueOf(8500), AgreementStatus.ACCEPTED);
        when(tenantProfileRepository.findAllByPropertyAndOwnerAndStatus(property, owner, TenantStatus.ACTIVE)).thenReturn(List.of(activeTenant));
        when(rentalAgreementRepository.findByBooking(activeTenant.getBooking())).thenReturn(Optional.of(agreement));

        var response = rentService.generate(2L, new GenerateRentRequest(10L, billingPeriod.getMonthValue(), billingPeriod.getYear()), "ip");

        assertThat(response.generatedCount()).isEqualTo(1);
        assertThat(response.invoices().getFirst().totalAmount()).isEqualByComparingTo("8500");
        assertThat(response.invoices().getFirst().status()).isEqualTo(RentInvoiceStatus.PENDING);
        verify(tenantProfileRepository).findAllByPropertyAndOwnerAndStatus(property, owner, TenantStatus.ACTIVE);
        verify(rentInvoiceRepository).save(any(RentInvoice.class));
    }

    @Test
    void duplicateMonthlyInvoiceIsSkippedSafely() {
        when(tenantProfileRepository.findAllByPropertyAndOwnerAndStatus(property, owner, TenantStatus.ACTIVE)).thenReturn(List.of(activeTenant));
        when(rentInvoiceRepository.findByTenantProfileAndBillingMonthAndBillingYear(activeTenant, 8, 2026))
                .thenReturn(Optional.of(invoice(501L, activeTenant, BigDecimal.valueOf(9000))));

        var response = rentService.generate(2L, new GenerateRentRequest(10L, 8, 2026), "ip");

        assertThat(response.generatedCount()).isZero();
        assertThat(response.alreadyGeneratedCount()).isEqualTo(1);
        verify(rentInvoiceRepository, never()).save(any(RentInvoice.class));
    }

    @Test
    void chargeUpdateRecalculatesTotalAndBlocksPaidInvoiceEdits() {
        RentInvoice invoice = invoice(500L, activeTenant, BigDecimal.valueOf(9000));
        when(rentInvoiceRepository.findLockedByIdAndOwner(500L, owner)).thenReturn(Optional.of(invoice));

        var detail = rentService.updateCharges(2L, 500L, new UpdateRentChargesRequest(
                BigDecimal.valueOf(500),
                BigDecimal.valueOf(750),
                BigDecimal.valueOf(250),
                BigDecimal.valueOf(100),
                "meter reading"
        ), "ip");

        assertThat(detail.totalAmount()).isEqualByComparingTo("10600");
        assertThat(detail.balanceAmount()).isEqualByComparingTo("10600");

        invoice.setStatus(RentInvoiceStatus.PAID);
        assertThatThrownBy(() -> rentService.updateCharges(2L, 500L, new UpdateRentChargesRequest(null, null, null, null, null), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("INVALID_RENT_STATUS"));
    }

    @Test
    void partialAndFullPaymentsPreserveHistoryAndStatuses() {
        RentInvoice invoice = invoice(500L, activeTenant, BigDecimal.valueOf(10000));
        when(rentInvoiceRepository.findLockedByIdAndOwner(500L, owner)).thenReturn(Optional.of(invoice));

        var partial = rentService.recordPayment(2L, 500L, payment(BigDecimal.valueOf(4000)), "ip");
        assertThat(partial.status()).isEqualTo(RentInvoiceStatus.PARTIALLY_PAID);
        assertThat(partial.balanceAmount()).isEqualByComparingTo("6000");

        var full = rentService.recordPayment(2L, 500L, payment(BigDecimal.valueOf(6000)), "ip");
        assertThat(full.status()).isEqualTo(RentInvoiceStatus.PAID);
        assertThat(full.balanceAmount()).isEqualByComparingTo("0");
        verify(rentPaymentRepository, org.mockito.Mockito.times(2)).save(any(RentPayment.class));
    }

    @Test
    void invalidPaymentAndOverpaymentAreBlocked() {
        RentInvoice invoice = invoice(500L, activeTenant, BigDecimal.valueOf(5000));
        when(rentInvoiceRepository.findLockedByIdAndOwner(500L, owner)).thenReturn(Optional.of(invoice));

        assertThatThrownBy(() -> rentService.recordPayment(2L, 500L, payment(BigDecimal.ZERO), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("INVALID_PAYMENT_AMOUNT"));

        assertThatThrownBy(() -> rentService.recordPayment(2L, 500L, payment(BigDecimal.valueOf(6000)), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("PAYMENT_EXCEEDS_BALANCE"));
    }

    @Test
    void overdueLogicIsAppliedForPastDueOutstandingInvoice() {
        RentInvoice invoice = invoice(500L, activeTenant, BigDecimal.valueOf(9000));
        invoice.setDueDate(LocalDate.now().minusDays(1));
        when(rentInvoiceRepository.findByIdAndTenantUser(500L, tenantUser)).thenReturn(Optional.of(invoice));

        var detail = rentService.getForUser(1L, 500L);

        assertThat(detail.status()).isEqualTo(RentInvoiceStatus.OVERDUE);
    }

    @Test
    void ownerAndTenantAccessAreProtected() {
        when(rentInvoiceRepository.findByIdAndOwner(500L, owner)).thenReturn(Optional.empty());
        when(rentInvoiceRepository.findByIdAndTenantUser(500L, tenantUser)).thenReturn(Optional.empty());
        when(rentInvoiceRepository.existsById(500L)).thenReturn(true);

        assertThatThrownBy(() -> rentService.getForOwner(2L, 500L))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("RENT_ACCESS_DENIED"));
        assertThatThrownBy(() -> rentService.getForUser(1L, 500L))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("RENT_ACCESS_DENIED"));
    }

    @Test
    void receiptIsGeneratedOnlyFromStoredAuthorizedPayment() {
        RentInvoice invoice = invoice(500L, activeTenant, BigDecimal.valueOf(9000));
        RentPayment payment = rentPayment(600L, invoice, BigDecimal.valueOf(9000));
        when(rentPaymentRepository.findByIdAndTenantUser(600L, tenantUser)).thenReturn(Optional.of(payment));

        String receipt = rentService.receiptForUser(1L, 600L);

        assertThat(receipt).contains("StaySure Rent Receipt");
        assertThat(receipt).contains("PAY-2026-000001");
        assertThat(receipt).contains("Invoice Number: RENT-2026-08-000001");
    }

    private RecordRentPaymentRequest payment(BigDecimal amount) {
        return new RecordRentPaymentRequest(amount, PaymentMethod.UPI, "upi-ref", LocalDate.now(), "received");
    }

    private RentInvoice invoice(Long id, TenantProfile tenant, BigDecimal amount) {
        RentInvoice invoice = new RentInvoice();
        invoice.setId(id);
        invoice.setInvoiceNumber("RENT-2026-08-000001");
        invoice.setTenantProfile(tenant);
        invoice.setProperty(tenant.getProperty());
        invoice.setRoom(tenant.getRoom());
        invoice.setBed(tenant.getBed());
        invoice.setBillingMonth(8);
        invoice.setBillingYear(2026);
        invoice.setBaseRent(amount);
        invoice.setTotalAmount(amount);
        invoice.setPaidAmount(BigDecimal.ZERO);
        invoice.setBalanceAmount(amount);
        invoice.setDueDate(LocalDate.now().plusDays(5));
        invoice.setStatus(RentInvoiceStatus.PENDING);
        invoice.setGeneratedAt(LocalDateTime.now());
        return invoice;
    }

    private RentPayment rentPayment(Long id, RentInvoice invoice, BigDecimal amount) {
        RentPayment payment = new RentPayment();
        payment.setId(id);
        payment.setPaymentNumber("PAY-2026-000001");
        payment.setRentInvoice(invoice);
        payment.setTenantProfile(invoice.getTenantProfile());
        payment.setProperty(invoice.getProperty());
        payment.setAmount(amount);
        payment.setPaymentMethod(PaymentMethod.UPI);
        payment.setPaymentReference("upi-ref");
        payment.setPaymentDate(LocalDate.now());
        payment.setRecordedBy(ownerUser);
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }

    private TenantProfile tenant(Long id, User user, PgProperty property, TenantStatus status, BigDecimal rent) {
        Room room = room(20L, property, rent);
        Bed bed = bed(30L, room);
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingNumber("BK-1");
        booking.setUser(user);
        booking.setProperty(property);
        booking.setRoom(room);
        booking.setBed(bed);
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setMoveInDate(LocalDate.now().minusMonths(1));
        booking.setMonthlyRent(rent);
        booking.setSecurityDeposit(BigDecimal.valueOf(10000));
        booking.setRequestedAt(LocalDateTime.now().minusMonths(2));

        TenantProfile tenant = new TenantProfile();
        tenant.setId(id);
        tenant.setBooking(booking);
        tenant.setUser(user);
        tenant.setProperty(property);
        tenant.setRoom(room);
        tenant.setBed(bed);
        tenant.setStatus(status);
        tenant.setJoiningDate(LocalDateTime.now().minusMonths(1));
        return tenant;
    }

    private RentalAgreement agreement(Booking booking, BigDecimal rent, AgreementStatus status) {
        RentalAgreement agreement = new RentalAgreement();
        agreement.setId(60L);
        agreement.setBooking(booking);
        agreement.setAgreementNumber("AG-1");
        agreement.setStatus(status);
        agreement.setStartDate(booking.getMoveInDate());
        agreement.setMonthlyRent(rent);
        agreement.setSecurityDeposit(booking.getSecurityDeposit());
        agreement.setNoticePeriodDays(30);
        agreement.setLockInMonths(1);
        agreement.setIssuedAt(LocalDateTime.now());
        return agreement;
    }

    private SecurityDeposit deposit(Booking booking) {
        SecurityDeposit deposit = new SecurityDeposit();
        deposit.setId(50L);
        deposit.setBooking(booking);
        deposit.setRequiredAmount(BigDecimal.valueOf(10000));
        deposit.setPaidAmount(BigDecimal.valueOf(10000));
        deposit.setStatus(DepositStatus.PAID);
        return deposit;
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

    private PgProperty property(Long id, OwnerProfile owner) {
        PgProperty property = new PgProperty();
        property.setId(id);
        property.setOwner(owner);
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
        property.setVerificationStatus(PropertyVerificationStatus.VERIFIED);
        property.setStatus(PropertyStatus.ACTIVE);
        return property;
    }

    private Room room(Long id, PgProperty property, BigDecimal rent) {
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
        room.setMonthlyRent(rent);
        room.setSecurityDeposit(BigDecimal.valueOf(10000));
        room.setAcAvailable(true);
        room.setAttachedBathroom(true);
        room.setFurnishingType(FurnishingType.FULLY_FURNISHED);
        room.setStatus(RoomStatus.ACTIVE);
        return room;
    }

    private Bed bed(Long id, Room room) {
        Bed bed = new Bed();
        bed.setId(id);
        bed.setRoom(room);
        bed.setBedNumber("A");
        bed.setBedLabel("Bed A");
        bed.setStatus(BedStatus.OCCUPIED);
        return bed;
    }
}
