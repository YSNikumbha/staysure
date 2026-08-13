package com.staysure;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.enums.RoleName;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.mapper.OwnerMapper;
import com.staysure.owner.repository.OwnerProfileRepository;
import com.staysure.owner.service.FileStorageService;
import com.staysure.property.dto.PaginationResponse;
import com.staysure.property.dto.PgPropertyRequest;
import com.staysure.property.dto.admin.AdminPropertyDetailsResponse;
import com.staysure.property.dto.discovery.PublicPgCardResponse;
import com.staysure.property.dto.discovery.PublicPgSearchRequest;
import com.staysure.property.dto.verification.SubmitVerificationResponse;
import com.staysure.property.entity.PgImage;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Wishlist;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.FloorStatus;
import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyType;
import com.staysure.property.enums.PropertyVerificationStatus;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.mapper.PgPropertyMapper;
import com.staysure.property.repository.AmenityRepository;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.FloorRepository;
import com.staysure.property.repository.PgImageRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.PropertyRuleRepository;
import com.staysure.property.repository.PropertyVerificationHistoryRepository;
import com.staysure.property.repository.RoomRepository;
import com.staysure.property.repository.WishlistRepository;
import com.staysure.property.service.AdminPgVerificationService;
import com.staysure.property.service.PgPropertyService;
import com.staysure.property.service.PublicPgDiscoveryService;
import com.staysure.property.service.WishlistService;
import com.staysure.role.entity.Role;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class Phase3ServiceTest {

    @Mock private OwnerProfileRepository ownerProfileRepository;
    @Mock private UserService userService;
    @Mock private PgPropertyRepository pgPropertyRepository;
    @Mock private PropertyRuleRepository propertyRuleRepository;
    @Mock private FloorRepository floorRepository;
    @Mock private RoomRepository roomRepository;
    @Mock private BedRepository bedRepository;
    @Mock private AmenityRepository amenityRepository;
    @Mock private PgImageRepository pgImageRepository;
    @Mock private PropertyVerificationHistoryRepository verificationHistoryRepository;
    @Mock private WishlistRepository wishlistRepository;
    @Mock private FileStorageService fileStorageService;
    @Mock private OwnerMapper ownerMapper;
    @Mock private AuditService auditService;

    private PgPropertyMapper mapper;
    private PgPropertyService propertyService;
    private PublicPgDiscoveryService discoveryService;
    private AdminPgVerificationService adminService;
    private WishlistService wishlistService;

    @BeforeEach
    void setUp() {
        mapper = new PgPropertyMapper();
        propertyService = new PgPropertyService(
                ownerProfileRepository,
                userService,
                pgPropertyRepository,
                propertyRuleRepository,
                floorRepository,
                roomRepository,
                bedRepository,
                amenityRepository,
                pgImageRepository,
                verificationHistoryRepository,
                fileStorageService,
                mapper,
                auditService
        );
        discoveryService = new PublicPgDiscoveryService(
                pgPropertyRepository,
                propertyRuleRepository,
                pgImageRepository,
                amenityRepository,
                roomRepository,
                bedRepository,
                mapper
        );
        adminService = new AdminPgVerificationService(
                pgPropertyRepository,
                roomRepository,
                bedRepository,
                verificationHistoryRepository,
                propertyService,
                userService,
                ownerMapper,
                mapper,
                auditService
        );
        wishlistService = new WishlistService(
                wishlistRepository,
                pgPropertyRepository,
                userService,
                discoveryService,
                auditService
        );
    }

    @Test
    void ownerCanSubmitCompletePg() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner, PropertyVerificationStatus.NOT_SUBMITTED, PropertyStatus.ACTIVE);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(floorRepository.countByPropertyAndStatusNot(property, FloorStatus.ARCHIVED)).thenReturn(1L);
        when(roomRepository.countByPropertyAndStatusNot(property, RoomStatus.ARCHIVED)).thenReturn(1L);
        when(bedRepository.countByPropertyAndStatusNot(property, BedStatus.ARCHIVED)).thenReturn(1L);
        when(pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)).thenReturn(List.of(image(property, true)));
        when(pgPropertyRepository.save(property)).thenReturn(property);

        SubmitVerificationResponse response = propertyService.submitForVerification(1L, 10L, "ip");

        assertThat(response.verificationStatus()).isEqualTo(PropertyVerificationStatus.PENDING);
        assertThat(property.getSubmittedForVerificationAt()).isNotNull();
        verify(auditService).log(eq(owner.getUser()), eq("PG_SUBMITTED_FOR_VERIFICATION"), eq("PROPERTY"),
                eq("PgProperty"), eq(10L), eq("PG submitted for verification"), eq(null), eq("PENDING"), eq("ip"));
    }

    @Test
    void ownerCannotSubmitIncompletePg() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner, PropertyVerificationStatus.NOT_SUBMITTED, PropertyStatus.ACTIVE);
        property.setDescription(null);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)).thenReturn(List.of());

        assertThatThrownBy(() -> propertyService.submitForVerification(1L, 10L, "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("PG_INCOMPLETE"));
    }

    @Test
    void ownerCannotSubmitAnotherOwnersPg() {
        OwnerProfile owner = verifiedOwner(1L);
        OwnerProfile otherOwner = verifiedOwner(2L);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property(10L, otherOwner, PropertyVerificationStatus.NOT_SUBMITTED, PropertyStatus.ACTIVE)));

        assertThatThrownBy(() -> propertyService.submitForVerification(1L, 10L, "ip"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("PG_ACCESS_DENIED"));
    }

    @Test
    void normalUserCannotSubmitPg() {
        User user = user(5L, RoleName.USER);
        when(userService.getUser(5L)).thenReturn(user);

        assertThatThrownBy(() -> propertyService.submitForVerification(5L, 10L, "ip"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("PG_OWNER_ROLE_REQUIRED"));
    }

    @Test
    void adminCanStartReviewAndVerifyPendingPg() {
        User admin = user(99L, RoleName.SUPER_ADMIN);
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner, PropertyVerificationStatus.PENDING, PropertyStatus.ACTIVE);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(userService.getUser(99L)).thenReturn(admin);
        when(pgPropertyRepository.save(property)).thenReturn(property);
        when(ownerMapper.toResponse(owner)).thenReturn(null);
        when(pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)).thenReturn(List.of());
        when(floorRepository.findAllByPropertyAndStatusNotOrderByFloorNumberAsc(property, FloorStatus.ARCHIVED)).thenReturn(List.of());
        when(verificationHistoryRepository.findAllByPropertyOrderByCreatedAtDesc(property)).thenReturn(List.of());

        AdminPropertyDetailsResponse reviewed = adminService.startReview(10L, 99L, null, "ip");
        assertThat(property.getVerificationStatus()).isEqualTo(PropertyVerificationStatus.UNDER_REVIEW);
        assertThat(reviewed).isNotNull();

        adminService.verify(10L, 99L, "ok", "ip");
        assertThat(property.getVerificationStatus()).isEqualTo(PropertyVerificationStatus.VERIFIED);
        assertThat(property.getVerifiedBy()).isEqualTo(admin);
    }

    @Test
    void adminCanRejectPendingPg() {
        User admin = user(99L, RoleName.SUPER_ADMIN);
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner, PropertyVerificationStatus.PENDING, PropertyStatus.ACTIVE);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(userService.getUser(99L)).thenReturn(admin);
        when(pgPropertyRepository.save(property)).thenReturn(property);
        when(ownerMapper.toResponse(owner)).thenReturn(null);
        when(pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)).thenReturn(List.of());
        when(floorRepository.findAllByPropertyAndStatusNotOrderByFloorNumberAsc(property, FloorStatus.ARCHIVED)).thenReturn(List.of());
        when(verificationHistoryRepository.findAllByPropertyOrderByCreatedAtDesc(property)).thenReturn(List.of());

        adminService.reject(10L, 99L, "missing cover", "ip");

        assertThat(property.getVerificationStatus()).isEqualTo(PropertyVerificationStatus.REJECTED);
        assertThat(property.getRejectionReason()).isEqualTo("missing cover");
    }

    @Test
    void invalidVerificationTransitionBlocked() {
        User admin = user(99L, RoleName.SUPER_ADMIN);
        PgProperty property = property(10L, verifiedOwner(1L), PropertyVerificationStatus.REJECTED, PropertyStatus.ACTIVE);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> adminService.verify(10L, 99L, "ok", "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("INVALID_VERIFICATION_TRANSITION"));
    }

    @Test
    void onlyVerifiedActivePgAppearsPublicly() {
        PgProperty property = property(10L, verifiedOwner(1L), PropertyVerificationStatus.VERIFIED, PropertyStatus.ACTIVE);
        when(pgPropertyRepository.findBySlugAndStatusAndVerificationStatus("sai-residency", PropertyStatus.ACTIVE, PropertyVerificationStatus.VERIFIED))
                .thenReturn(Optional.of(property));
        when(pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)).thenReturn(List.of());
        when(roomRepository.findPublicRooms(property, RoomStatus.ACTIVE, FloorStatus.ACTIVE)).thenReturn(List.of());
        when(bedRepository.countByPublicPropertyAndStatusNot(property, BedStatus.ARCHIVED, RoomStatus.ACTIVE, FloorStatus.ACTIVE)).thenReturn(0L);
        when(bedRepository.countByPublicPropertyAndStatus(property, BedStatus.AVAILABLE, RoomStatus.ACTIVE, FloorStatus.ACTIVE)).thenReturn(0L);

        assertThat(discoveryService.details("sai-residency").slug()).isEqualTo("sai-residency");

        when(pgPropertyRepository.findBySlugAndStatusAndVerificationStatus("pending-pg", PropertyStatus.ACTIVE, PropertyVerificationStatus.VERIFIED))
                .thenReturn(Optional.empty());
        assertThatThrownBy(() -> discoveryService.details("pending-pg"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("PUBLIC_PG_NOT_FOUND"));
    }

    @Test
    void publicSearchReturnsPaginatedCardsAndRejectsInvalidRentRange() {
        PgProperty property = property(10L, verifiedOwner(1L), PropertyVerificationStatus.VERIFIED, PropertyStatus.ACTIVE);
        when(pgPropertyRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(property)));
        when(bedRepository.countByPublicPropertyIdsAndStatusNot(List.of(10L), BedStatus.ARCHIVED, RoomStatus.ACTIVE, FloorStatus.ACTIVE))
                .thenReturn(List.<Object[]>of(new Object[]{10L, 3L}));
        when(bedRepository.countByPublicPropertyIdsAndStatus(List.of(10L), BedStatus.AVAILABLE, RoomStatus.ACTIVE, FloorStatus.ACTIVE))
                .thenReturn(List.<Object[]>of(new Object[]{10L, 2L}));
        when(pgImageRepository.findAllByPropertyIdsForCards(List.of(10L))).thenReturn(List.of(image(property, true)));
        when(amenityRepository.findAmenitiesByPropertyIds(List.of(10L))).thenReturn(List.of());

        PaginationResponse<PublicPgCardResponse> response = discoveryService.search(new PublicPgSearchRequest(
                "hinjawadi", null, null, BigDecimal.ZERO, BigDecimal.valueOf(10000), GenderType.COED,
                null, null, null, List.of(), true, "latest"
        ), 0, 12);

        assertThat(response.content()).hasSize(1);
        assertThat(response.content().get(0).availableBeds()).isEqualTo(2);

        assertThatThrownBy(() -> discoveryService.search(new PublicPgSearchRequest(
                null, null, null, BigDecimal.TEN, BigDecimal.ONE, null, null, null,
                null, List.of(), false, "latest"
        ), 0, 12)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void wishlistAddWorksAndDuplicateBlocked() {
        User user = user(1L, RoleName.USER);
        PgProperty property = property(10L, verifiedOwner(2L), PropertyVerificationStatus.VERIFIED, PropertyStatus.ACTIVE);
        Wishlist saved = new Wishlist();
        saved.setId(50L);
        saved.setUser(user);
        saved.setProperty(property);
        when(userService.getUser(1L)).thenReturn(user);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(wishlistRepository.existsByUserAndProperty(user, property)).thenReturn(false, true);
        when(wishlistRepository.save(any(Wishlist.class))).thenReturn(saved);
        when(wishlistRepository.findAllByUserOrderByCreatedAtDesc(user)).thenReturn(List.of(saved));
        when(bedRepository.countByPublicPropertyIdsAndStatusNot(List.of(10L), BedStatus.ARCHIVED, RoomStatus.ACTIVE, FloorStatus.ACTIVE)).thenReturn(List.of());
        when(bedRepository.countByPublicPropertyIdsAndStatus(List.of(10L), BedStatus.AVAILABLE, RoomStatus.ACTIVE, FloorStatus.ACTIVE)).thenReturn(List.of());
        when(pgImageRepository.findAllByPropertyIdsForCards(List.of(10L))).thenReturn(List.of());
        when(amenityRepository.findAmenitiesByPropertyIds(List.of(10L))).thenReturn(List.of());

        assertThat(wishlistService.add(1L, 10L, "ip").id()).isEqualTo(50L);

        assertThatThrownBy(() -> wishlistService.add(1L, 10L, "ip"))
                .isInstanceOf(DuplicateResourceException.class)
                .satisfies(error -> assertThat(((DuplicateResourceException) error).getErrorCode()).isEqualTo("WISHLIST_ALREADY_EXISTS"));
    }

    private void mockVerifiedOwner(OwnerProfile owner) {
        when(userService.getUser(owner.getUser().getId())).thenReturn(owner.getUser());
        when(ownerProfileRepository.findByUser(owner.getUser())).thenReturn(Optional.of(owner));
    }

    private OwnerProfile verifiedOwner(Long userId) {
        OwnerProfile owner = new OwnerProfile();
        owner.setId(userId);
        owner.setUser(user(userId, RoleName.USER, RoleName.PG_OWNER));
        owner.setBusinessName("Owner Business " + userId);
        owner.setVerificationStatus(OwnerVerificationStatus.VERIFIED);
        return owner;
    }

    private User user(Long id, RoleName... roles) {
        User user = new User();
        user.setId(id);
        user.setFirstName("User");
        user.setLastName(String.valueOf(id));
        user.setEmail("user" + id + "@example.com");
        user.setPhone("999999" + id);
        for (RoleName roleName : roles) {
            Role role = new Role();
            role.setName(roleName);
            user.getRoles().add(role);
        }
        return user;
    }

    private PgProperty property(Long id, OwnerProfile owner, PropertyVerificationStatus verificationStatus, PropertyStatus status) {
        PgProperty property = new PgProperty();
        property.setId(id);
        property.setOwner(owner);
        property.setName("Sai Residency");
        property.setSlug("sai-residency");
        property.setDescription("Complete PG near Hinjawadi");
        property.setGenderType(GenderType.COED);
        property.setPropertyType(PropertyType.PG);
        property.setAddressLine1("Line 1");
        property.setArea("Hinjawadi");
        property.setCity("Pune");
        property.setState("Maharashtra");
        property.setPincode("411057");
        property.setStartingRent(BigDecimal.valueOf(8000));
        property.setSecurityDeposit(BigDecimal.valueOf(16000));
        property.setNoticePeriodDays(30);
        property.setLockInMonths(3);
        property.setStatus(status);
        property.setVerificationStatus(verificationStatus);
        return property;
    }

    private PgImage image(PgProperty property, boolean cover) {
        PgImage image = new PgImage();
        image.setId(99L);
        image.setProperty(property);
        image.setImageUrl("/uploads/pg-images/" + property.getId() + "/cover.jpg");
        image.setCoverImage(cover);
        image.setSortOrder(0);
        return image;
    }
}
