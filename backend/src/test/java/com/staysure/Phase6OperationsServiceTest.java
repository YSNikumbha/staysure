package com.staysure;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.operations.dto.ComplaintRequest;
import com.staysure.operations.dto.FoodFeedbackRequest;
import com.staysure.operations.dto.FoodMenuRequest;
import com.staysure.operations.dto.NoticeRequest;
import com.staysure.operations.dto.OperationActionRequest;
import com.staysure.operations.dto.VisitorRequest;
import com.staysure.operations.entity.Complaint;
import com.staysure.operations.entity.ComplaintStatusHistory;
import com.staysure.operations.entity.FoodFeedback;
import com.staysure.operations.entity.FoodMenu;
import com.staysure.operations.entity.Notice;
import com.staysure.operations.entity.Notification;
import com.staysure.operations.entity.VisitorEntry;
import com.staysure.operations.enums.ComplaintCategory;
import com.staysure.operations.enums.ComplaintStatus;
import com.staysure.operations.enums.MealType;
import com.staysure.operations.enums.NotificationType;
import com.staysure.operations.enums.NoticeStatus;
import com.staysure.operations.enums.OperationalPriority;
import com.staysure.operations.enums.VisitorStatus;
import com.staysure.operations.mapper.OperationMapper;
import com.staysure.operations.repository.ComplaintCommentRepository;
import com.staysure.operations.repository.ComplaintRepository;
import com.staysure.operations.repository.ComplaintStatusHistoryRepository;
import com.staysure.operations.repository.FoodFeedbackRepository;
import com.staysure.operations.repository.FoodMenuRepository;
import com.staysure.operations.repository.NoticeRepository;
import com.staysure.operations.repository.NotificationRepository;
import com.staysure.operations.repository.VisitorEntryRepository;
import com.staysure.operations.service.ComplaintService;
import com.staysure.operations.service.FoodService;
import com.staysure.operations.service.NotificationService;
import com.staysure.operations.service.NoticeService;
import com.staysure.operations.service.OperationAccessService;
import com.staysure.operations.service.VisitorService;
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
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressWarnings("null")
class Phase6OperationsServiceTest {

    @Mock private UserService userService;
    @Mock private OwnerService ownerService;
    @Mock private TenantProfileRepository tenantProfileRepository;
    @Mock private PgPropertyRepository propertyRepository;
    @Mock private AuditService auditService;
    @Mock private ComplaintRepository complaintRepository;
    @Mock private ComplaintCommentRepository commentRepository;
    @Mock private ComplaintStatusHistoryRepository historyRepository;
    @Mock private NoticeRepository noticeRepository;
    @Mock private FoodMenuRepository foodMenuRepository;
    @Mock private FoodFeedbackRepository foodFeedbackRepository;
    @Mock private VisitorEntryRepository visitorRepository;
    @Mock private NotificationRepository notificationRepository;

    private OperationAccessService accessService;
    private OperationMapper mapper;
    private NotificationService notificationService;
    private ComplaintService complaintService;
    private NoticeService noticeService;
    private FoodService foodService;
    private VisitorService visitorService;
    private User tenantUser;
    private User ownerUser;
    private OwnerProfile owner;
    private PgProperty property;
    private TenantProfile activeTenant;

    @BeforeEach
    void setUp() {
        mapper = new OperationMapper();
        accessService = new OperationAccessService(userService, ownerService, tenantProfileRepository, propertyRepository);
        notificationService = new NotificationService(notificationRepository, userService, mapper);
        complaintService = new ComplaintService(complaintRepository, commentRepository, historyRepository,
                accessService, notificationService, auditService, mapper);
        noticeService = new NoticeService(noticeRepository, tenantProfileRepository, accessService,
                notificationService, auditService, mapper);
        foodService = new FoodService(foodMenuRepository, foodFeedbackRepository, accessService, auditService, mapper);
        visitorService = new VisitorService(visitorRepository, accessService, notificationService, auditService, mapper);

        tenantUser = user(1L, "Tenant");
        ownerUser = user(2L, "Owner");
        owner = owner(ownerUser);
        property = property(10L, owner);
        activeTenant = tenant(70L, tenantUser, property, TenantStatus.ACTIVE);

        when(userService.getUser(1L)).thenReturn(tenantUser);
        when(userService.getUser(2L)).thenReturn(ownerUser);
        when(ownerService.getCurrentOwner(2L)).thenReturn(owner);
        when(tenantProfileRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(tenantUser, TenantStatus.ACTIVE))
                .thenReturn(Optional.of(activeTenant));
        when(propertyRepository.findByIdAndOwner(10L, owner)).thenReturn(Optional.of(property));
        when(complaintRepository.existsByComplaintNumber(any())).thenReturn(false);
        when(complaintRepository.countByComplaintNumberStartingWith(any())).thenReturn(0L);
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> {
            Complaint complaint = invocation.getArgument(0);
            if (complaint.getId() == null) complaint.setId(500L);
            return complaint;
        });
        when(historyRepository.save(any(ComplaintStatusHistory.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(commentRepository.findAllByComplaintOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(historyRepository.findAllByComplaintOrderByCreatedAtAsc(any())).thenReturn(List.of());
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> {
            Notification notification = invocation.getArgument(0);
            if (notification.getId() == null) notification.setId(900L);
            return notification;
        });
    }

    @Test
    void activeTenantCanCreateComplaint() {
        var response = complaintService.create(1L, new ComplaintRequest(
                ComplaintCategory.ELECTRICAL,
                "Fan not working",
                "The fan stopped working last night",
                OperationalPriority.HIGH
        ), "ip");

        assertThat(response.status()).isEqualTo(ComplaintStatus.OPEN);
        assertThat(response.propertyId()).isEqualTo(property.getId());
        verify(historyRepository).save(any(ComplaintStatusHistory.class));
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void nonActiveTenantCannotCreateComplaint() {
        when(tenantProfileRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(tenantUser, TenantStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> complaintService.create(1L, new ComplaintRequest(
                ComplaintCategory.ROOM, "Issue", "Description", OperationalPriority.MEDIUM
        ), "ip"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("ACTIVE_TENANCY_REQUIRED"));
    }

    @Test
    void ownerComplaintAccessAndTransitionsAreProtected() {
        Complaint complaint = complaint(500L, activeTenant, ComplaintStatus.OPEN);
        when(complaintRepository.findByIdAndOwner(500L, owner)).thenReturn(Optional.of(complaint));

        var acknowledged = complaintService.acknowledge(2L, 500L, new OperationActionRequest("seen"), "ip");

        assertThat(acknowledged.status()).isEqualTo(ComplaintStatus.ACKNOWLEDGED);

        complaint.setStatus(ComplaintStatus.RESOLVED);
        assertThatThrownBy(() -> complaintService.acknowledge(2L, 500L, new OperationActionRequest(null), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("INVALID_COMPLAINT_TRANSITION"));

        when(complaintRepository.findByIdAndOwner(501L, owner)).thenReturn(Optional.empty());
        when(complaintRepository.existsById(501L)).thenReturn(true);
        assertThatThrownBy(() -> complaintService.getForOwner(2L, 501L))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("COMPLAINT_ACCESS_DENIED"));
    }

    @Test
    void noticePublishNotifiesOnlyActiveTenantsForOwnedProperty() {
        Notice notice = notice(300L, NoticeStatus.DRAFT);
        when(noticeRepository.findByIdAndOwner(300L, owner)).thenReturn(Optional.of(notice));
        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tenantProfileRepository.findAllByPropertyAndOwnerAndStatus(property, owner, TenantStatus.ACTIVE))
                .thenReturn(List.of(activeTenant));

        var response = noticeService.publish(2L, 300L, "ip");

        assertThat(response.status()).isEqualTo(NoticeStatus.PUBLISHED);
        verify(notificationRepository).existsByUserAndTypeAndReferenceTypeAndReferenceId(
                tenantUser, NotificationType.NOTICE_PUBLISHED, "NOTICE", 300L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void foodMenuDuplicatesBlockedAndFeedbackUpdatesExistingRecord() {
        FoodMenu existingMenu = foodMenu(200L);
        when(foodMenuRepository.findByPropertyAndMenuDateAndMealType(property, LocalDate.now(), MealType.LUNCH))
                .thenReturn(Optional.of(existingMenu));

        assertThatThrownBy(() -> foodService.createMenu(2L, new FoodMenuRequest(
                10L, LocalDate.now(), MealType.LUNCH, "Rice and dal", null
        ), "ip")).isInstanceOf(DuplicateResourceException.class);

        FoodFeedback existingFeedback = feedback(201L);
        when(foodFeedbackRepository.findByTenantProfileAndMenuDateAndMealType(activeTenant, LocalDate.now(), MealType.LUNCH))
                .thenReturn(Optional.of(existingFeedback));
        when(foodFeedbackRepository.save(any(FoodFeedback.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = foodService.submitFeedback(1L, new FoodFeedbackRequest(LocalDate.now(), MealType.LUNCH, 4, "Good"), "ip");

        assertThat(response.id()).isEqualTo(201L);
        assertThat(response.rating()).isEqualTo(4);
        assertThat(response.comment()).isEqualTo("Good");
    }

    @Test
    void visitorApprovalAndCheckInTransitionsAreEnforced() {
        when(visitorRepository.existsByVisitorNumber(any())).thenReturn(false);
        when(visitorRepository.countByVisitorNumberStartingWith(any())).thenReturn(0L);
        when(visitorRepository.save(any(VisitorEntry.class))).thenAnswer(invocation -> {
            VisitorEntry visitor = invocation.getArgument(0);
            if (visitor.getId() == null) visitor.setId(800L);
            return visitor;
        });

        var requested = visitorService.request(1L, new VisitorRequest(
                "Rahul", "9999999999", "Brother", LocalDate.now().plusDays(1),
                LocalTime.of(10, 0), LocalTime.of(18, 0), "Family visit"
        ), "ip");

        assertThat(requested.status()).isEqualTo(VisitorStatus.REQUESTED);

        VisitorEntry visitor = visitor(800L, VisitorStatus.REQUESTED);
        when(visitorRepository.findByIdAndOwner(800L, owner)).thenReturn(Optional.of(visitor));
        var approved = visitorService.approve(2L, 800L, "ip");
        assertThat(approved.status()).isEqualTo(VisitorStatus.APPROVED);

        var checkedIn = visitorService.checkIn(2L, 800L, "ip");
        assertThat(checkedIn.status()).isEqualTo(VisitorStatus.CHECKED_IN);

        assertThatThrownBy(() -> visitorService.approve(2L, 800L, "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("INVALID_VISITOR_TRANSITION"));
    }

    @Test
    void notificationOwnershipAndReadStateAreProtected() {
        Notification notification = notification(900L, tenantUser);
        when(notificationRepository.findByIdAndUser(900L, tenantUser)).thenReturn(Optional.of(notification));
        when(notificationRepository.countByUserAndReadAtIsNull(tenantUser)).thenReturn(1L);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThat(notificationService.unreadCount(1L).unreadCount()).isEqualTo(1);
        var response = notificationService.markRead(1L, 900L);
        assertThat(response.readAt()).isNotNull();

        when(notificationRepository.findByIdAndUser(901L, tenantUser)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> notificationService.markRead(1L, 901L))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("NOTIFICATION_NOT_FOUND"));
    }

    private Complaint complaint(Long id, TenantProfile tenant, ComplaintStatus status) {
        Complaint complaint = new Complaint();
        complaint.setId(id);
        complaint.setComplaintNumber("CMP-2026-000001");
        complaint.setTenantProfile(tenant);
        complaint.setProperty(tenant.getProperty());
        complaint.setRoom(tenant.getRoom());
        complaint.setCategory(ComplaintCategory.ROOM);
        complaint.setTitle("Room issue");
        complaint.setDescription("Description");
        complaint.setPriority(OperationalPriority.MEDIUM);
        complaint.setStatus(status);
        return complaint;
    }

    private Notice notice(Long id, NoticeStatus status) {
        Notice notice = new Notice();
        notice.setId(id);
        notice.setProperty(property);
        notice.setTitle("Water supply");
        notice.setContent("Water supply maintenance");
        notice.setStatus(status);
        notice.setPriority(OperationalPriority.MEDIUM);
        notice.setCreatedBy(ownerUser);
        return notice;
    }

    private FoodMenu foodMenu(Long id) {
        FoodMenu menu = new FoodMenu();
        menu.setId(id);
        menu.setProperty(property);
        menu.setMenuDate(LocalDate.now());
        menu.setMealType(MealType.LUNCH);
        menu.setItems("Rice and dal");
        return menu;
    }

    private FoodFeedback feedback(Long id) {
        FoodFeedback feedback = new FoodFeedback();
        feedback.setId(id);
        feedback.setTenantProfile(activeTenant);
        feedback.setProperty(property);
        feedback.setMenuDate(LocalDate.now());
        feedback.setMealType(MealType.LUNCH);
        feedback.setRating(3);
        return feedback;
    }

    private VisitorEntry visitor(Long id, VisitorStatus status) {
        VisitorEntry visitor = new VisitorEntry();
        visitor.setId(id);
        visitor.setVisitorNumber("VIS-2026-000001");
        visitor.setTenantProfile(activeTenant);
        visitor.setProperty(property);
        visitor.setVisitorName("Rahul");
        visitor.setVisitorPhone("9999999999");
        visitor.setRelationship("Brother");
        visitor.setVisitDate(LocalDate.now().plusDays(1));
        visitor.setExpectedArrivalTime(LocalTime.of(10, 0));
        visitor.setExpectedDepartureTime(LocalTime.of(18, 0));
        visitor.setPurpose("Family visit");
        visitor.setStatus(status);
        return visitor;
    }

    private Notification notification(Long id, User user) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUser(user);
        notification.setType(NotificationType.NOTICE_PUBLISHED);
        notification.setTitle("New notice");
        notification.setMessage("Notice published");
        notification.setReferenceType("NOTICE");
        notification.setReferenceId(300L);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    private TenantProfile tenant(Long id, User user, PgProperty property, TenantStatus status) {
        Room room = room(20L, property);
        Bed bed = bed(30L, room);
        Booking booking = new Booking();
        booking.setId(100L);
        booking.setBookingNumber("BK-1");
        booking.setUser(user);
        booking.setProperty(property);
        booking.setRoom(room);
        booking.setBed(bed);
        booking.setStatus(BookingStatus.CHECKED_IN);
        booking.setMonthlyRent(BigDecimal.valueOf(9000));
        booking.setSecurityDeposit(BigDecimal.valueOf(10000));

        TenantProfile tenant = new TenantProfile();
        tenant.setId(id);
        tenant.setBooking(booking);
        tenant.setUser(user);
        tenant.setProperty(property);
        tenant.setRoom(room);
        tenant.setBed(bed);
        tenant.setStatus(status);
        tenant.setJoiningDate(LocalDateTime.now().minusDays(10));
        return tenant;
    }

    private User user(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setFirstName(name);
        user.setLastName("User");
        user.setEmail(name.toLowerCase() + "@example.com");
        user.setPhone("999999999" + id);
        return user;
    }

    private OwnerProfile owner(User user) {
        OwnerProfile owner = new OwnerProfile();
        owner.setId(2L);
        owner.setUser(user);
        owner.setBusinessName("Owner");
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
        property.setVerificationStatus(PropertyVerificationStatus.VERIFIED);
        property.setStatus(PropertyStatus.ACTIVE);
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
        room.setMonthlyRent(BigDecimal.valueOf(9000));
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
