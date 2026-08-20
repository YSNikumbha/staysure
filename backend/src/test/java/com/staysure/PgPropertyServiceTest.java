package com.staysure;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.enums.RoleName;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.repository.OwnerProfileRepository;
import com.staysure.owner.service.FileStorageService;
import com.staysure.property.dto.BedRequest;
import com.staysure.property.dto.FloorRequest;
import com.staysure.property.dto.PgPropertyRequest;
import com.staysure.property.dto.PropertyAmenityUpdateRequest;
import com.staysure.property.dto.PropertyDetailsResponse;
import com.staysure.property.dto.RoomRequest;
import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.entity.Amenity;
import com.staysure.property.entity.Floor;
import com.staysure.property.entity.PgImage;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.PropertyRule;
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
import com.staysure.property.mapper.PgPropertyMapper;
import com.staysure.property.repository.AmenityRepository;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.FloorRepository;
import com.staysure.property.repository.PgImageRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.PropertyRuleRepository;
import com.staysure.property.repository.PropertyVerificationHistoryRepository;
import com.staysure.property.repository.RoomRepository;
import com.staysure.property.service.PgPropertyService;
import com.staysure.role.entity.Role;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class PgPropertyServiceTest {

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
    @Mock private FileStorageService fileStorageService;
    @Mock private AuditService auditService;

    private PgPropertyService service;

    @BeforeEach
    void setUp() {
        service = new PgPropertyService(
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
                new PgPropertyMapper(),
                auditService
        );
    }

    @Test
    void verifiedOwnerCreatesPgSuccessfully() {
        OwnerProfile owner = verifiedOwner(1L);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.existsBySlug("sai-residency-hinjawadi")).thenReturn(false);
        when(pgPropertyRepository.save(any(PgProperty.class))).thenAnswer(invocation -> {
            PgProperty property = invocation.getArgument(0);
            property.setId(10L);
            return property;
        });
        when(propertyRuleRepository.save(any(PropertyRule.class))).thenAnswer(invocation -> {
            PropertyRule rule = invocation.getArgument(0);
            rule.setId(20L);
            return rule;
        });
        when(propertyRuleRepository.findByProperty(any(PgProperty.class))).thenAnswer(invocation -> {
            PropertyRule rule = new PropertyRule();
            rule.setId(20L);
            rule.setProperty(invocation.getArgument(0));
            return Optional.of(rule);
        });
        mockEmptyDetails();

        PropertyDetailsResponse response = service.createProperty(1L, propertyRequest("Sai Residency Hinjawadi"), "127.0.0.1");

        assertThat(response.property().id()).isEqualTo(10L);
        assertThat(response.property().slug()).isEqualTo("sai-residency-hinjawadi");
        verify(auditService).log(eq(owner.getUser()), eq("PG_CREATED"), eq("PROPERTY"), eq("PgProperty"), eq(10L),
                eq("PG property created"), eq(null), eq("Sai Residency Hinjawadi"), eq("127.0.0.1"));
    }

    @Test
    void unverifiedOwnerCannotCreatePg() {
        User user = user(1L, RoleName.PG_OWNER);
        OwnerProfile owner = owner(1L, user, OwnerVerificationStatus.PENDING);
        when(userService.getUser(1L)).thenReturn(user);
        when(ownerProfileRepository.findByUser(user)).thenReturn(Optional.of(owner));

        assertThatThrownBy(() -> service.createProperty(1L, propertyRequest("Sai Residency"), "127.0.0.1"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("OWNER_NOT_VERIFIED"))
                .hasMessage("Owner verification is required before creating a PG");
    }

    @Test
    void normalUserCannotCreatePg() {
        User user = user(1L, RoleName.USER);
        when(userService.getUser(1L)).thenReturn(user);

        assertThatThrownBy(() -> service.createProperty(1L, propertyRequest("Sai Residency"), "127.0.0.1"))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("PG_OWNER_ROLE_REQUIRED"));
    }

    @Test
    void ownerCannotAccessAnotherOwnersPg() {
        OwnerProfile currentOwner = verifiedOwner(2L);
        OwnerProfile otherOwner = verifiedOwner(3L);
        PgProperty property = property(10L, otherOwner);
        mockVerifiedOwner(currentOwner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));

        assertThatThrownBy(() -> service.getProperty(2L, 10L))
                .isInstanceOf(ApiException.class)
                .satisfies(error -> assertThat(((ApiException) error).getErrorCode()).isEqualTo("PG_ACCESS_DENIED"));
    }

    @Test
    void duplicateSlugGetsNumericSuffix() {
        OwnerProfile owner = verifiedOwner(1L);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.existsBySlug("sai-residency")).thenReturn(true);
        when(pgPropertyRepository.existsBySlug("sai-residency-2")).thenReturn(false);
        when(pgPropertyRepository.save(any(PgProperty.class))).thenAnswer(invocation -> {
            PgProperty property = invocation.getArgument(0);
            property.setId(10L);
            return property;
        });
        when(propertyRuleRepository.save(any(PropertyRule.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(propertyRuleRepository.findByProperty(any(PgProperty.class))).thenReturn(Optional.empty());
        mockEmptyDetails();

        service.createProperty(1L, propertyRequest("Sai Residency"), "127.0.0.1");

        ArgumentCaptor<PgProperty> captor = ArgumentCaptor.forClass(PgProperty.class);
        verify(pgPropertyRepository).save(captor.capture());
        assertThat(captor.getValue().getSlug()).isEqualTo("sai-residency-2");
    }

    @Test
    void duplicateFloorBlocked() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(floorRepository.existsByPropertyAndFloorNumber(property, 1)).thenReturn(true);

        assertThatThrownBy(() -> service.createFloor(1L, 10L, new FloorRequest("Floor 1", 1, null, FloorStatus.ACTIVE), "ip"))
                .isInstanceOf(DuplicateResourceException.class)
                .satisfies(error -> assertThat(((DuplicateResourceException) error).getErrorCode()).isEqualTo("DUPLICATE_FLOOR_NUMBER"));
    }

    @Test
    void duplicateRoomBlocked() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner);
        Floor floor = floor(11L, property);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(floorRepository.findById(11L)).thenReturn(Optional.of(floor));
        when(roomRepository.existsByFloorAndRoomNumber(floor, "101")).thenReturn(true);

        assertThatThrownBy(() -> service.createRoom(1L, 10L, 11L, roomRequest("101", 3), "ip"))
                .isInstanceOf(DuplicateResourceException.class)
                .satisfies(error -> assertThat(((DuplicateResourceException) error).getErrorCode()).isEqualTo("DUPLICATE_ROOM_NUMBER"));
    }

    @Test
    void duplicateBedBlocked() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner);
        Floor floor = floor(11L, property);
        Room room = room(12L, floor, 3);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(floorRepository.findById(11L)).thenReturn(Optional.of(floor));
        when(roomRepository.findById(12L)).thenReturn(Optional.of(room));
        when(bedRepository.existsByRoomAndBedNumber(room, "A")).thenReturn(true);

        assertThatThrownBy(() -> service.createBed(1L, 10L, 11L, 12L, new BedRequest("A", "Bed A", BedStatus.AVAILABLE), "ip"))
                .isInstanceOf(DuplicateResourceException.class)
                .satisfies(error -> assertThat(((DuplicateResourceException) error).getErrorCode()).isEqualTo("DUPLICATE_BED_NUMBER"));
    }

    @Test
    void roomCapacityCannotBeExceeded() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner);
        Floor floor = floor(11L, property);
        Room room = room(12L, floor, 3);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(floorRepository.findById(11L)).thenReturn(Optional.of(floor));
        when(roomRepository.findById(12L)).thenReturn(Optional.of(room));
        when(bedRepository.existsByRoomAndBedNumber(room, "D")).thenReturn(false);
        when(bedRepository.countByRoomAndStatusNot(room, BedStatus.ARCHIVED)).thenReturn(3L);

        assertThatThrownBy(() -> service.createBed(1L, 10L, 11L, 12L, new BedRequest("D", "Bed D", BedStatus.AVAILABLE), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("ROOM_CAPACITY_EXCEEDED"));
    }

    @Test
    void ownerCannotManuallySetBedOccupiedOrReserved() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner);
        Floor floor = floor(11L, property);
        Room room = room(12L, floor, 3);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(floorRepository.findById(11L)).thenReturn(Optional.of(floor));
        when(roomRepository.findById(12L)).thenReturn(Optional.of(room));

        assertThatThrownBy(() -> service.createBed(1L, 10L, 11L, 12L, new BedRequest("A", "Bed A", BedStatus.OCCUPIED), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("INVALID_BED_STATUS"));
        assertThatThrownBy(() -> service.createBed(1L, 10L, 11L, 12L, new BedRequest("B", "Bed B", BedStatus.RESERVED), "ip"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(error -> assertThat(((BusinessRuleException) error).getErrorCode()).isEqualTo("INVALID_BED_STATUS"));
    }

    @Test
    void coverImageRemainsUnique() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner);
        PgImage oldCover = image(20L, property, true);
        PgImage newCover = image(21L, property, false);
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(pgImageRepository.findById(21L)).thenReturn(Optional.of(newCover));
        when(pgImageRepository.findAllByPropertyAndCoverImageTrue(property)).thenReturn(List.of(oldCover));

        service.setCoverImage(1L, 10L, 21L, "ip");

        assertThat(oldCover.isCoverImage()).isFalse();
        assertThat(newCover.isCoverImage()).isTrue();
        verify(pgImageRepository).save(oldCover);
        verify(pgImageRepository).save(newCover);
    }

    @Test
    void amenitiesUpdateWorks() {
        OwnerProfile owner = verifiedOwner(1L);
        PgProperty property = property(10L, owner);
        Amenity wifi = amenity(1L, "Wi-Fi");
        Amenity cctv = amenity(2L, "CCTV");
        mockVerifiedOwner(owner);
        when(pgPropertyRepository.findById(10L)).thenReturn(Optional.of(property));
        when(amenityRepository.findAllByIdInAndActiveTrue(Set.of(1L, 2L))).thenReturn(List.of(wifi, cctv));

        List<AmenityResponse> response = service.updateAmenities(1L, 10L, new PropertyAmenityUpdateRequest(Set.of(1L, 2L)), "ip");

        assertThat(response).hasSize(2);
        assertThat(property.getAmenities()).containsExactlyInAnyOrder(wifi, cctv);
        verify(auditService).log(eq(owner.getUser()), eq("AMENITIES_UPDATED"), eq("PROPERTY"), eq("PgProperty"),
                eq(10L), eq("Property amenities updated"), eq(null), eq("2"), eq("ip"));
    }

    private void mockVerifiedOwner(OwnerProfile owner) {
        when(userService.getUser(owner.getUser().getId())).thenReturn(owner.getUser());
        when(ownerProfileRepository.findByUser(owner.getUser())).thenReturn(Optional.of(owner));
    }

    private void mockEmptyDetails() {
        when(pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(any(PgProperty.class))).thenReturn(List.of());
        when(floorRepository.findAllByPropertyAndStatusNotOrderByFloorNumberAsc(any(PgProperty.class), eq(FloorStatus.ARCHIVED))).thenReturn(List.of());
        when(floorRepository.countByPropertyAndStatusNot(any(PgProperty.class), eq(FloorStatus.ARCHIVED))).thenReturn(0L);
        when(roomRepository.countByPropertyAndStatusNot(any(PgProperty.class), eq(RoomStatus.ARCHIVED))).thenReturn(0L);
        when(bedRepository.countByPropertyAndStatusNot(any(PgProperty.class), eq(BedStatus.ARCHIVED))).thenReturn(0L);
        when(bedRepository.countByPropertyAndStatus(any(PgProperty.class), eq(BedStatus.AVAILABLE))).thenReturn(0L);
        when(bedRepository.countByPropertyAndStatus(any(PgProperty.class), eq(BedStatus.MAINTENANCE))).thenReturn(0L);
        when(bedRepository.countByPropertyAndStatus(any(PgProperty.class), eq(BedStatus.INACTIVE))).thenReturn(0L);
    }

    private PgPropertyRequest propertyRequest(String name) {
        return new PgPropertyRequest(
                name,
                "Well maintained PG",
                GenderType.COED,
                PropertyType.PG,
                "Line 1",
                "Line 2",
                "Hinjawadi",
                "Pune",
                "Maharashtra",
                "411057",
                null,
                null,
                BigDecimal.valueOf(8000),
                BigDecimal.valueOf(16000),
                30,
                3,
                null,
                true,
                PropertyStatus.DRAFT,
                null
        );
    }

    private RoomRequest roomRequest(String roomNumber, int capacity) {
        return new RoomRequest(
                roomNumber,
                "Room " + roomNumber,
                SharingType.TRIPLE,
                capacity,
                BigDecimal.valueOf(9000),
                BigDecimal.valueOf(9000),
                false,
                true,
                FurnishingType.FULLY_FURNISHED,
                RoomStatus.ACTIVE,
                null
        );
    }

    private OwnerProfile verifiedOwner(Long userId) {
        User user = user(userId, RoleName.USER, RoleName.PG_OWNER);
        return owner(userId, user, OwnerVerificationStatus.VERIFIED);
    }

    private OwnerProfile owner(Long id, User user, OwnerVerificationStatus status) {
        OwnerProfile owner = new OwnerProfile();
        owner.setId(id);
        owner.setUser(user);
        owner.setBusinessName("Owner " + id);
        owner.setVerificationStatus(status);
        return owner;
    }

    private User user(Long id, RoleName... roles) {
        User user = new User();
        user.setId(id);
        user.setFirstName("Owner");
        user.setLastName("User");
        user.setEmail("owner" + id + "@example.com");
        user.setPhone("99999999" + id);
        for (RoleName roleName : roles) {
            Role role = new Role();
            role.setName(roleName);
            user.getRoles().add(role);
        }
        return user;
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
        property.setState("Maharashtra");
        property.setPincode("411057");
        property.setStartingRent(BigDecimal.valueOf(8000));
        property.setSecurityDeposit(BigDecimal.valueOf(16000));
        property.setNoticePeriodDays(30);
        property.setLockInMonths(3);
        property.setStatus(PropertyStatus.ACTIVE);
        property.setVerificationStatus(PropertyVerificationStatus.NOT_SUBMITTED);
        return property;
    }

    private Floor floor(Long id, PgProperty property) {
        Floor floor = new Floor();
        floor.setId(id);
        floor.setProperty(property);
        floor.setName("Floor 1");
        floor.setFloorNumber(1);
        floor.setStatus(FloorStatus.ACTIVE);
        return floor;
    }

    private Room room(Long id, Floor floor, int capacity) {
        Room room = new Room();
        room.setId(id);
        room.setFloor(floor);
        room.setRoomNumber("101");
        room.setSharingType(SharingType.TRIPLE);
        room.setCapacity(capacity);
        room.setMonthlyRent(BigDecimal.valueOf(9000));
        room.setSecurityDeposit(BigDecimal.valueOf(9000));
        room.setFurnishingType(FurnishingType.FULLY_FURNISHED);
        room.setStatus(RoomStatus.ACTIVE);
        return room;
    }

    private PgImage image(Long id, PgProperty property, boolean cover) {
        PgImage image = new PgImage();
        image.setId(id);
        image.setProperty(property);
        image.setImageUrl("/uploads/pg-images/" + property.getId() + "/" + id + ".jpg");
        image.setCoverImage(cover);
        image.setSortOrder(0);
        return image;
    }

    private Amenity amenity(Long id, String name) {
        Amenity amenity = new Amenity();
        amenity.setId(id);
        amenity.setName(name);
        amenity.setCode(name.toUpperCase().replaceAll("[^A-Z0-9]+", "_"));
        amenity.setActive(true);
        return amenity;
    }
}
