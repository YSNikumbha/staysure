package com.staysure.property.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.common.enums.RoleName;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.DuplicateResourceException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.repository.OwnerProfileRepository;
import com.staysure.owner.service.FileStorageService;
import com.staysure.owner.service.StoredFile;
import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.dto.BedRequest;
import com.staysure.property.dto.BedResponse;
import com.staysure.property.dto.FloorRequest;
import com.staysure.property.dto.FloorResponse;
import com.staysure.property.dto.OwnerDashboardStats;
import com.staysure.property.dto.PgImageOrderRequest;
import com.staysure.property.dto.PgImageResponse;
import com.staysure.property.dto.PgPropertyRequest;
import com.staysure.property.dto.PropertyAmenityUpdateRequest;
import com.staysure.property.dto.PropertyDetailsResponse;
import com.staysure.property.dto.PropertyInventoryCountsResponse;
import com.staysure.property.dto.PropertyRuleRequest;
import com.staysure.property.dto.PropertySummaryResponse;
import com.staysure.property.dto.RoomRequest;
import com.staysure.property.dto.RoomResponse;
import com.staysure.property.dto.verification.SubmitVerificationResponse;
import com.staysure.property.entity.Amenity;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.Floor;
import com.staysure.property.entity.PgImage;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.PropertyRule;
import com.staysure.property.entity.PropertyVerificationHistory;
import com.staysure.property.entity.Room;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.FloorStatus;
import com.staysure.property.enums.ImageCategory;
import com.staysure.property.enums.PropertyVerificationAction;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.mapper.PgPropertyMapper;
import com.staysure.property.repository.AmenityRepository;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.FloorRepository;
import com.staysure.property.repository.PgImageRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.PropertyVerificationHistoryRepository;
import com.staysure.property.repository.PropertyRuleRepository;
import com.staysure.property.repository.RoomRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class PgPropertyService {

    private static final Set<BedStatus> OWNER_EDITABLE_BED_STATUSES = Set.of(
            BedStatus.AVAILABLE,
            BedStatus.MAINTENANCE,
            BedStatus.INACTIVE
    );

    private final OwnerProfileRepository ownerProfileRepository;
    private final UserService userService;
    private final PgPropertyRepository pgPropertyRepository;
    private final PropertyRuleRepository propertyRuleRepository;
    private final FloorRepository floorRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final AmenityRepository amenityRepository;
    private final PgImageRepository pgImageRepository;
    private final PropertyVerificationHistoryRepository verificationHistoryRepository;
    private final FileStorageService fileStorageService;
    private final PgPropertyMapper mapper;
    private final AuditService auditService;

    public PgPropertyService(OwnerProfileRepository ownerProfileRepository,
                             UserService userService,
                             PgPropertyRepository pgPropertyRepository,
                             PropertyRuleRepository propertyRuleRepository,
                             FloorRepository floorRepository,
                             RoomRepository roomRepository,
                             BedRepository bedRepository,
                             AmenityRepository amenityRepository,
                             PgImageRepository pgImageRepository,
                             PropertyVerificationHistoryRepository verificationHistoryRepository,
                             FileStorageService fileStorageService,
                             PgPropertyMapper mapper,
                             AuditService auditService) {
        this.ownerProfileRepository = ownerProfileRepository;
        this.userService = userService;
        this.pgPropertyRepository = pgPropertyRepository;
        this.propertyRuleRepository = propertyRuleRepository;
        this.floorRepository = floorRepository;
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.amenityRepository = amenityRepository;
        this.pgImageRepository = pgImageRepository;
        this.verificationHistoryRepository = verificationHistoryRepository;
        this.fileStorageService = fileStorageService;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public OwnerDashboardStats dashboardStats(OwnerProfile owner) {
        return new OwnerDashboardStats(
                pgPropertyRepository.countByOwnerAndStatusNot(owner, PropertyStatus.ARCHIVED),
                pgPropertyRepository.countByOwnerAndStatus(owner, PropertyStatus.ACTIVE),
                roomRepository.countByOwnerAndStatusesNot(owner, RoomStatus.ARCHIVED, FloorStatus.ARCHIVED, PropertyStatus.ARCHIVED),
                bedRepository.countByOwnerAndStatusesNot(owner, BedStatus.ARCHIVED, RoomStatus.ARCHIVED, FloorStatus.ARCHIVED, PropertyStatus.ARCHIVED),
                bedRepository.countByOwnerAndStatusWithParentsNot(owner, BedStatus.AVAILABLE, RoomStatus.ARCHIVED, FloorStatus.ARCHIVED, PropertyStatus.ARCHIVED)
        );
    }

    @Transactional
    public PropertyDetailsResponse createProperty(Long userId, PgPropertyRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = new PgProperty();
        property.setOwner(owner);
        property.setSlug(uniqueSlug(request.name()));
        property.setVerificationStatus(PropertyVerificationStatus.NOT_SUBMITTED);
        applyPropertyRequest(property, request, true);
        PgProperty saved = pgPropertyRepository.save(property);

        PropertyRule rule = new PropertyRule();
        rule.setProperty(saved);
        applyRuleRequest(rule, request.rules(), saved.getNoticePeriodDays());
        propertyRuleRepository.save(rule);

        auditService.log(owner.getUser(), "PG_CREATED", "PROPERTY", "PgProperty", saved.getId(),
                "PG property created", null, saved.getName(), ipAddress);
        return buildDetails(saved);
    }

    @Transactional(readOnly = true)
    public List<PropertySummaryResponse> listProperties(Long userId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        return pgPropertyRepository.findAllByOwnerAndStatusNotOrderByCreatedAtDesc(owner, PropertyStatus.ARCHIVED)
                .stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyDetailsResponse getProperty(Long userId, Long pgId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        return buildDetails(getOwnedProperty(owner, pgId));
    }

    @Transactional
    public SubmitVerificationResponse submitForVerification(Long userId, Long pgId, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        if (property.getStatus() == PropertyStatus.ARCHIVED) {
            throw new BusinessRuleException("Archived PG cannot be submitted for verification", "PG_ARCHIVED");
        }
        if (property.getVerificationStatus() == PropertyVerificationStatus.PENDING
                || property.getVerificationStatus() == PropertyVerificationStatus.UNDER_REVIEW) {
            throw new BusinessRuleException("PG is already under verification", "PG_ALREADY_SUBMITTED");
        }
        if (property.getVerificationStatus() == PropertyVerificationStatus.VERIFIED) {
            throw new BusinessRuleException("PG is already verified", "PG_ALREADY_VERIFIED");
        }

        List<String> missingItems = missingVerificationItems(property);
        if (!missingItems.isEmpty()) {
            throw new BusinessRuleException("PG cannot be submitted for verification", "PG_INCOMPLETE",
                    Map.of("missingItems", missingItems));
        }

        PropertyVerificationStatus previousStatus = property.getVerificationStatus();
        property.setVerificationStatus(PropertyVerificationStatus.PENDING);
        property.setSubmittedForVerificationAt(LocalDateTime.now());
        property.setVerifiedAt(null);
        property.setVerifiedBy(null);
        property.setRejectionReason(null);
        property.setVerificationRemarks(null);
        PgProperty saved = pgPropertyRepository.save(property);

        boolean resubmitted = previousStatus == PropertyVerificationStatus.REJECTED
                || previousStatus == PropertyVerificationStatus.CHANGES_REQUESTED;
        recordVerificationHistory(saved, previousStatus, PropertyVerificationStatus.PENDING,
                resubmitted ? PropertyVerificationAction.RESUBMITTED.name() : PropertyVerificationAction.SUBMITTED.name(),
                owner.getUser());
        auditService.log(owner.getUser(), resubmitted ? "PG_RESUBMITTED" : "PG_SUBMITTED_FOR_VERIFICATION",
                "PROPERTY", "PgProperty", saved.getId(), "PG submitted for verification", null,
                PropertyVerificationStatus.PENDING.name(), ipAddress);
        return new SubmitVerificationResponse(saved.getId(), saved.getVerificationStatus(), saved.getSubmittedForVerificationAt(), List.of());
    }

    @Transactional
    public PropertyDetailsResponse updateProperty(Long userId, Long pgId, PgPropertyRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        PropertySnapshot previous = PropertySnapshot.of(property);
        applyPropertyRequest(property, request, false);
        boolean requiresReverification = property.getVerificationStatus() == PropertyVerificationStatus.VERIFIED
                && previous.criticalChanged(property);
        if (requiresReverification) {
            property.setVerificationStatus(PropertyVerificationStatus.PENDING);
            property.setSubmittedForVerificationAt(LocalDateTime.now());
            property.setVerifiedAt(null);
            property.setVerifiedBy(null);
            property.setVerificationRemarks("Critical property details updated and require re-verification");
            property.setRejectionReason(null);
        }
        PgProperty saved = pgPropertyRepository.save(property);
        PropertyRule rule = propertyRuleRepository.findByProperty(saved).orElseGet(() -> {
            PropertyRule created = new PropertyRule();
            created.setProperty(saved);
            return created;
        });
        applyRuleRequest(rule, request.rules(), saved.getNoticePeriodDays());
        propertyRuleRepository.save(rule);
        auditService.log(owner.getUser(), "PG_UPDATED", "PROPERTY", "PgProperty", saved.getId(),
                "PG property updated", null, saved.getName(), ipAddress);
        if (requiresReverification) {
            recordVerificationHistory(saved, PropertyVerificationStatus.VERIFIED, PropertyVerificationStatus.PENDING,
                    "Critical property details updated and require re-verification", owner.getUser());
            auditService.log(owner.getUser(), "PG_RESUBMITTED", "PROPERTY", "PgProperty", saved.getId(),
                    "PG requires re-verification after critical update", null, PropertyVerificationStatus.PENDING.name(), ipAddress);
        }
        return buildDetails(saved);
    }

    @Transactional
    public PropertyDetailsResponse updatePropertyStatus(Long userId, Long pgId, PropertyStatus status, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        if (status == PropertyStatus.ARCHIVED) {
            archiveProperty(owner, property, ipAddress);
        } else {
            property.setStatus(status);
            pgPropertyRepository.save(property);
            auditService.log(owner.getUser(), "PG_UPDATED", "PROPERTY", "PgProperty", property.getId(),
                    "PG status updated", null, status.name(), ipAddress);
        }
        return buildDetails(property);
    }

    @Transactional
    public void archiveProperty(Long userId, Long pgId, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        archiveProperty(owner, getOwnedProperty(owner, pgId), ipAddress);
    }

    @Transactional
    public FloorResponse createFloor(Long userId, Long pgId, FloorRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        if (floorRepository.existsByPropertyAndFloorNumber(property, request.floorNumber())) {
            throw new DuplicateResourceException("Floor number already exists for this PG", "DUPLICATE_FLOOR_NUMBER");
        }
        Floor floor = new Floor();
        floor.setProperty(property);
        applyFloorRequest(floor, request);
        Floor saved = floorRepository.save(floor);
        auditService.log(owner.getUser(), "FLOOR_CREATED", "PROPERTY", "Floor", saved.getId(),
                "Floor created", null, saved.getName(), ipAddress);
        return floorResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<FloorResponse> listFloors(Long userId, Long pgId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        return floorRepository.findAllByPropertyAndStatusNotOrderByFloorNumberAsc(property, FloorStatus.ARCHIVED)
                .stream()
                .map(floor -> floorResponse(floor, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public FloorResponse getFloor(Long userId, Long pgId, Long floorId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        return floorResponse(getFloorInProperty(property, floorId), true);
    }

    @Transactional
    public FloorResponse updateFloor(Long userId, Long pgId, Long floorId, FloorRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Floor floor = getFloorInProperty(property, floorId);
        if (!floor.getFloorNumber().equals(request.floorNumber())
                && floorRepository.existsByPropertyAndFloorNumber(property, request.floorNumber())) {
            throw new DuplicateResourceException("Floor number already exists for this PG", "DUPLICATE_FLOOR_NUMBER");
        }
        applyFloorRequest(floor, request);
        Floor saved = floorRepository.save(floor);
        auditService.log(owner.getUser(), "FLOOR_UPDATED", "PROPERTY", "Floor", saved.getId(),
                "Floor updated", null, saved.getName(), ipAddress);
        return floorResponse(saved, true);
    }

    @Transactional
    public void archiveFloor(Long userId, Long pgId, Long floorId, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Floor floor = getFloorInProperty(property, floorId);
        if (roomRepository.countByFloorAndStatusNot(floor, RoomStatus.ARCHIVED) > 0) {
            throw new BusinessRuleException("Archive rooms before archiving this floor", "FLOOR_HAS_ACTIVE_ROOMS");
        }
        floor.setStatus(FloorStatus.ARCHIVED);
        floorRepository.save(floor);
        auditService.log(owner.getUser(), "FLOOR_ARCHIVED", "PROPERTY", "Floor", floor.getId(),
                "Floor archived", null, null, ipAddress);
    }

    @Transactional
    public RoomResponse createRoom(Long userId, Long pgId, Long floorId, RoomRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Floor floor = getFloorInProperty(property, floorId);
        if (roomRepository.existsByFloorAndRoomNumber(floor, normalizeRoomNumber(request.roomNumber()))) {
            throw new DuplicateResourceException("Room number already exists on this floor", "DUPLICATE_ROOM_NUMBER");
        }
        Room room = new Room();
        room.setFloor(floor);
        applyRoomRequest(room, request);
        Room saved = roomRepository.save(room);
        auditService.log(owner.getUser(), "ROOM_CREATED", "PROPERTY", "Room", saved.getId(),
                "Room created", null, saved.getRoomNumber(), ipAddress);
        return roomResponse(saved, true);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> listRooms(Long userId, Long pgId, Long floorId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Floor floor = getFloorInProperty(property, floorId);
        return roomRepository.findAllByFloorAndStatusNotOrderByRoomNumberAsc(floor, RoomStatus.ARCHIVED)
                .stream()
                .map(room -> roomResponse(room, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long userId, Long pgId, Long floorId, Long roomId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Floor floor = getFloorInProperty(property, floorId);
        return roomResponse(getRoomInFloor(floor, roomId), true);
    }

    @Transactional
    public RoomResponse updateRoom(Long userId, Long pgId, Long floorId, Long roomId, RoomRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Floor floor = getFloorInProperty(property, floorId);
        Room room = getRoomInFloor(floor, roomId);
        String normalizedRoomNumber = normalizeRoomNumber(request.roomNumber());
        if (!room.getRoomNumber().equals(normalizedRoomNumber)
                && roomRepository.existsByFloorAndRoomNumber(floor, normalizedRoomNumber)) {
            throw new DuplicateResourceException("Room number already exists on this floor", "DUPLICATE_ROOM_NUMBER");
        }
        long currentBeds = bedRepository.countByRoomAndStatusNot(room, BedStatus.ARCHIVED);
        if (request.capacity() < currentBeds) {
            throw new BusinessRuleException("Room capacity exceeded", "ROOM_CAPACITY_EXCEEDED");
        }
        applyRoomRequest(room, request);
        Room saved = roomRepository.save(room);
        auditService.log(owner.getUser(), "ROOM_UPDATED", "PROPERTY", "Room", saved.getId(),
                "Room updated", null, saved.getRoomNumber(), ipAddress);
        return roomResponse(saved, true);
    }

    @Transactional
    public void archiveRoom(Long userId, Long pgId, Long floorId, Long roomId, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Floor floor = getFloorInProperty(property, floorId);
        Room room = getRoomInFloor(floor, roomId);
        if (bedRepository.countByRoomAndStatusNot(room, BedStatus.ARCHIVED) > 0) {
            throw new BusinessRuleException("Archive beds before archiving this room", "ROOM_HAS_ACTIVE_BEDS");
        }
        room.setStatus(RoomStatus.ARCHIVED);
        roomRepository.save(room);
        auditService.log(owner.getUser(), "ROOM_ARCHIVED", "PROPERTY", "Room", room.getId(),
                "Room archived", null, null, ipAddress);
    }

    @Transactional
    public BedResponse createBed(Long userId, Long pgId, Long floorId, Long roomId, BedRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Room room = getRoomInFloor(getFloorInProperty(property, floorId), roomId);
        BedStatus status = request.status() == null ? BedStatus.AVAILABLE : request.status();
        validateManualBedStatus(status);
        if (bedRepository.existsByRoomAndBedNumber(room, normalizeBedNumber(request.bedNumber()))) {
            throw new DuplicateResourceException("Bed number already exists in this room", "DUPLICATE_BED_NUMBER");
        }
        ensureRoomHasCapacity(room);
        Bed bed = new Bed();
        bed.setRoom(room);
        bed.setBedNumber(normalizeBedNumber(request.bedNumber()));
        bed.setBedLabel(blankToNull(request.bedLabel()));
        bed.setStatus(status);
        Bed saved = bedRepository.save(bed);
        auditService.log(owner.getUser(), "BED_CREATED", "PROPERTY", "Bed", saved.getId(),
                "Bed created", null, saved.getBedNumber(), ipAddress);
        return mapper.toBedResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<BedResponse> listBeds(Long userId, Long pgId, Long floorId, Long roomId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Room room = getRoomInFloor(getFloorInProperty(property, floorId), roomId);
        return bedRepository.findAllByRoomAndStatusNotOrderByBedNumberAsc(room, BedStatus.ARCHIVED)
                .stream()
                .map(mapper::toBedResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public BedResponse getBed(Long userId, Long pgId, Long floorId, Long roomId, Long bedId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Room room = getRoomInFloor(getFloorInProperty(property, floorId), roomId);
        return mapper.toBedResponse(getBedInRoom(room, bedId));
    }

    @Transactional
    public BedResponse updateBed(Long userId, Long pgId, Long floorId, Long roomId, Long bedId, BedRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Room room = getRoomInFloor(getFloorInProperty(property, floorId), roomId);
        Bed bed = getBedInRoom(room, bedId);
        BedStatus status = request.status() == null ? bed.getStatus() : request.status();
        validateManualBedStatus(status);
        String bedNumber = normalizeBedNumber(request.bedNumber());
        if (!bed.getBedNumber().equals(bedNumber) && bedRepository.existsByRoomAndBedNumber(room, bedNumber)) {
            throw new DuplicateResourceException("Bed number already exists in this room", "DUPLICATE_BED_NUMBER");
        }
        bed.setBedNumber(bedNumber);
        bed.setBedLabel(blankToNull(request.bedLabel()));
        bed.setStatus(status);
        Bed saved = bedRepository.save(bed);
        auditService.log(owner.getUser(), "BED_UPDATED", "PROPERTY", "Bed", saved.getId(),
                "Bed updated", null, saved.getBedNumber(), ipAddress);
        return mapper.toBedResponse(saved);
    }

    @Transactional
    public void archiveBed(Long userId, Long pgId, Long floorId, Long roomId, Long bedId, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Room room = getRoomInFloor(getFloorInProperty(property, floorId), roomId);
        Bed bed = getBedInRoom(room, bedId);
        bed.setStatus(BedStatus.ARCHIVED);
        bedRepository.save(bed);
        auditService.log(owner.getUser(), "BED_ARCHIVED", "PROPERTY", "Bed", bed.getId(),
                "Bed archived", null, null, ipAddress);
    }

    @Transactional(readOnly = true)
    public List<AmenityResponse> listAmenities(Long userId) {
        requireVerifiedOwner(userId);
        return amenityRepository.findAllByActiveTrueOrderByNameAsc()
                .stream()
                .map(mapper::toAmenityResponse)
                .toList();
    }

    @Transactional
    public List<AmenityResponse> updateAmenities(Long userId, Long pgId, PropertyAmenityUpdateRequest request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Set<Long> amenityIds = request.amenityIds() == null ? Set.of() : request.amenityIds();
        List<Amenity> amenities = amenityIds.isEmpty()
                ? List.of()
                : amenityRepository.findAllByIdInAndActiveTrue(amenityIds);
        if (amenities.size() != amenityIds.size()) {
            throw new BusinessRuleException("One or more amenities are invalid", "AMENITY_NOT_FOUND");
        }
        property.getAmenities().clear();
        property.getAmenities().addAll(new LinkedHashSet<>(amenities));
        pgPropertyRepository.save(property);
        auditService.log(owner.getUser(), "AMENITIES_UPDATED", "PROPERTY", "PgProperty", property.getId(),
                "Property amenities updated", null, String.valueOf(amenityIds.size()), ipAddress);
        return amenities.stream()
                .sorted(Comparator.comparing(Amenity::getName))
                .map(mapper::toAmenityResponse)
                .toList();
    }

    @Transactional
    public PgImageResponse uploadImage(Long userId, Long pgId, MultipartFile file, ImageCategory category,
                                       boolean coverImage, Integer sortOrder, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        StoredFile storedFile = fileStorageService.storePgImage(property.getId(), file);
        PgImage image = new PgImage();
        image.setProperty(property);
        image.setImageUrl(storedFile.publicUrl());
        image.setCategory(category == null ? ImageCategory.OTHER : category);
        image.setSortOrder(sortOrder == null ? 0 : sortOrder);
        image.setCoverImage(false);
        PgImage saved = pgImageRepository.save(image);
        if (coverImage) {
            makeCoverImage(property, saved);
        }
        auditService.log(owner.getUser(), "PG_IMAGE_UPLOADED", "PROPERTY", "PgImage", saved.getId(),
                "PG image uploaded", null, saved.getCategory().name(), ipAddress);
        return mapper.toImageResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<PgImageResponse> listImages(Long userId, Long pgId) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        return pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)
                .stream()
                .map(mapper::toImageResponse)
                .toList();
    }

    @Transactional
    public PgImageResponse setCoverImage(Long userId, Long pgId, Long imageId, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        PgImage image = getImageInProperty(property, imageId);
        makeCoverImage(property, image);
        auditService.log(owner.getUser(), "PG_COVER_IMAGE_CHANGED", "PROPERTY", "PgImage", image.getId(),
                "PG cover image changed", null, null, ipAddress);
        return mapper.toImageResponse(image);
    }

    @Transactional
    public List<PgImageResponse> reorderImages(Long userId, Long pgId, List<PgImageOrderRequest> request, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        Map<Long, PgImage> images = pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)
                .stream()
                .collect(Collectors.toMap(PgImage::getId, Function.identity()));
        for (PgImageOrderRequest item : request) {
            PgImage image = images.get(item.imageId());
            if (image == null) {
                throw notFound("Image not found", "PG_IMAGE_NOT_FOUND");
            }
            image.setSortOrder(item.sortOrder());
            pgImageRepository.save(image);
        }
        auditService.log(owner.getUser(), "PG_IMAGE_REORDERED", "PROPERTY", "PgProperty", property.getId(),
                "PG images reordered", null, String.valueOf(request.size()), ipAddress);
        return pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)
                .stream()
                .map(mapper::toImageResponse)
                .toList();
    }

    @Transactional
    public void deleteImage(Long userId, Long pgId, Long imageId, String ipAddress) {
        OwnerProfile owner = requireVerifiedOwner(userId);
        PgProperty property = getOwnedProperty(owner, pgId);
        PgImage image = getImageInProperty(property, imageId);
        pgImageRepository.delete(image);
        fileStorageService.deleteByPublicUrl(image.getImageUrl());
        auditService.log(owner.getUser(), "PG_IMAGE_DELETED", "PROPERTY", "PgImage", image.getId(),
                "PG image deleted", null, null, ipAddress);
    }

    private void archiveProperty(OwnerProfile owner, PgProperty property, String ipAddress) {
        if (floorRepository.countByPropertyAndStatusNot(property, FloorStatus.ARCHIVED) > 0) {
            throw new BusinessRuleException("Archive floors before archiving this PG", "PG_HAS_ACTIVE_FLOORS");
        }
        property.setStatus(PropertyStatus.ARCHIVED);
        pgPropertyRepository.save(property);
        auditService.log(owner.getUser(), "PG_ARCHIVED", "PROPERTY", "PgProperty", property.getId(),
                "PG property archived", null, null, ipAddress);
    }

    private OwnerProfile requireVerifiedOwner(Long userId) {
        User user = userService.getUser(userId);
        boolean hasPgOwnerRole = user.getRoles().stream().anyMatch(role -> role.getName() == RoleName.PG_OWNER);
        if (!hasPgOwnerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PG owner role is required", "PG_OWNER_ROLE_REQUIRED");
        }
        OwnerProfile owner = ownerProfileRepository.findByUser(user)
                .orElseThrow(() -> new BusinessRuleException("Owner verification is required before creating a PG", "OWNER_NOT_VERIFIED"));
        if (owner.getVerificationStatus() != OwnerVerificationStatus.VERIFIED) {
            throw new BusinessRuleException("Owner verification is required before creating a PG", "OWNER_NOT_VERIFIED");
        }
        return owner;
    }

    private PgProperty getOwnedProperty(OwnerProfile owner, Long pgId) {
        PgProperty property = pgPropertyRepository.findById(pgId)
                .orElseThrow(() -> notFound("PG not found", "PG_NOT_FOUND"));
        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "PG access denied", "PG_ACCESS_DENIED");
        }
        return property;
    }

    private Floor getFloorInProperty(PgProperty property, Long floorId) {
        Floor floor = floorRepository.findById(floorId)
                .orElseThrow(() -> notFound("Floor not found", "FLOOR_NOT_FOUND"));
        if (!floor.getProperty().getId().equals(property.getId())) {
            throw notFound("Floor not found", "FLOOR_NOT_FOUND");
        }
        return floor;
    }

    private Room getRoomInFloor(Floor floor, Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> notFound("Room not found", "ROOM_NOT_FOUND"));
        if (!room.getFloor().getId().equals(floor.getId())) {
            throw notFound("Room not found", "ROOM_NOT_FOUND");
        }
        return room;
    }

    private Bed getBedInRoom(Room room, Long bedId) {
        Bed bed = bedRepository.findById(bedId)
                .orElseThrow(() -> notFound("Bed not found", "BED_NOT_FOUND"));
        if (!bed.getRoom().getId().equals(room.getId())) {
            throw notFound("Bed not found", "BED_NOT_FOUND");
        }
        return bed;
    }

    private PgImage getImageInProperty(PgProperty property, Long imageId) {
        PgImage image = pgImageRepository.findById(imageId)
                .orElseThrow(() -> notFound("Image not found", "PG_IMAGE_NOT_FOUND"));
        if (!image.getProperty().getId().equals(property.getId())) {
            throw notFound("Image not found", "PG_IMAGE_NOT_FOUND");
        }
        return image;
    }

    private ApiException notFound(String message, String code) {
        return new ApiException(HttpStatus.NOT_FOUND, message, code);
    }

    @Transactional(readOnly = true)
    public PropertyDetailsResponse detailsForProperty(PgProperty property) {
        return buildDetails(property);
    }

    private PropertyDetailsResponse buildDetails(PgProperty property) {
        PropertyRule rule = propertyRuleRepository.findByProperty(property).orElse(null);
        List<AmenityResponse> amenities = property.getAmenities().stream()
                .sorted(Comparator.comparing(Amenity::getName))
                .map(mapper::toAmenityResponse)
                .toList();
        List<PgImageResponse> images = pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)
                .stream()
                .map(mapper::toImageResponse)
                .toList();
        List<FloorResponse> floors = floorRepository.findAllByPropertyAndStatusNotOrderByFloorNumberAsc(property, FloorStatus.ARCHIVED)
                .stream()
                .map(floor -> floorResponse(floor, true))
                .toList();
        PropertyInventoryCountsResponse counts = counts(property);
        return new PropertyDetailsResponse(
                mapper.toPropertyResponse(property),
                mapper.toRuleResponse(rule),
                amenities,
                images,
                floors,
                counts.totalRooms(),
                counts.totalBeds(),
                counts.availableBeds(),
                counts
        );
    }

    private PropertySummaryResponse toSummary(PgProperty property) {
        PropertyInventoryCountsResponse counts = counts(property);
        String coverImageUrl = pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)
                .stream()
                .filter(PgImage::isCoverImage)
                .findFirst()
                .or(() -> pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property).stream().findFirst())
                .map(PgImage::getImageUrl)
                .orElse(null);
        return new PropertySummaryResponse(
                property.getId(),
                property.getName(),
                property.getSlug(),
                property.getArea(),
                property.getCity(),
                property.getState(),
                property.getStatus(),
                property.getVerificationStatus(),
                coverImageUrl,
                counts.totalRooms(),
                counts.totalBeds(),
                counts.availableBeds(),
                property.getCreatedAt()
        );
    }

    private PropertyInventoryCountsResponse counts(PgProperty property) {
        return new PropertyInventoryCountsResponse(
                floorRepository.countByPropertyAndStatusNot(property, FloorStatus.ARCHIVED),
                roomRepository.countByPropertyAndStatusNot(property, RoomStatus.ARCHIVED),
                bedRepository.countByPropertyAndStatusNot(property, BedStatus.ARCHIVED),
                bedRepository.countByPropertyAndStatus(property, BedStatus.AVAILABLE),
                bedRepository.countByPropertyAndStatus(property, BedStatus.MAINTENANCE),
                bedRepository.countByPropertyAndStatus(property, BedStatus.INACTIVE)
        );
    }

    private List<String> missingVerificationItems(PgProperty property) {
        List<String> missingItems = new ArrayList<>();
        if (isBlank(property.getName())) missingItems.add("PG name");
        if (isBlank(property.getDescription())) missingItems.add("Description");
        if (property.getGenderType() == null) missingItems.add("Gender type");
        if (property.getPropertyType() == null) missingItems.add("Property type");
        if (isBlank(property.getAddressLine1())) missingItems.add("Address line 1");
        if (isBlank(property.getArea())) missingItems.add("Area");
        if (isBlank(property.getCity())) missingItems.add("City");
        if (isBlank(property.getState())) missingItems.add("State");
        if (isBlank(property.getPincode())) missingItems.add("Pincode");
        if (property.getStartingRent() == null || property.getStartingRent().compareTo(BigDecimal.ZERO) < 0) missingItems.add("Starting rent");
        if (property.getSecurityDeposit() == null || property.getSecurityDeposit().compareTo(BigDecimal.ZERO) < 0) missingItems.add("Security deposit");
        if (floorRepository.countByPropertyAndStatusNot(property, FloorStatus.ARCHIVED) < 1) missingItems.add("At least one floor");
        if (roomRepository.countByPropertyAndStatusNot(property, RoomStatus.ARCHIVED) < 1) missingItems.add("At least one room");
        if (bedRepository.countByPropertyAndStatusNot(property, BedStatus.ARCHIVED) < 1) missingItems.add("At least one bed");
        List<PgImage> images = pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property);
        if (images.isEmpty()) {
            missingItems.add("At least one image");
        } else if (images.stream().noneMatch(PgImage::isCoverImage)) {
            missingItems.add("Cover image");
        }
        return missingItems;
    }

    private void recordVerificationHistory(PgProperty property, PropertyVerificationStatus previousStatus,
                                           PropertyVerificationStatus newStatus, String remarks, User actor) {
        PropertyVerificationHistory history = new PropertyVerificationHistory();
        history.setProperty(property);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setRemarks(remarks);
        history.setActionBy(actor);
        verificationHistoryRepository.save(history);
    }

    private FloorResponse floorResponse(Floor floor, boolean includeRooms) {
        List<Room> rooms = roomRepository.findAllByFloorAndStatusNotOrderByRoomNumberAsc(floor, RoomStatus.ARCHIVED);
        List<RoomResponse> roomResponses = includeRooms
                ? rooms.stream().map(room -> roomResponse(room, true)).toList()
                : List.of();
        long bedCount = rooms.stream()
                .mapToLong(room -> bedRepository.countByRoomAndStatusNot(room, BedStatus.ARCHIVED))
                .sum();
        return mapper.toFloorResponse(floor, rooms.size(), bedCount, roomResponses);
    }

    private RoomResponse roomResponse(Room room, boolean includeBeds) {
        List<Bed> beds = bedRepository.findAllByRoomAndStatusNotOrderByBedNumberAsc(room, BedStatus.ARCHIVED);
        List<BedResponse> bedResponses = includeBeds
                ? beds.stream().map(mapper::toBedResponse).toList()
                : List.of();
        return mapper.toRoomResponse(room, beds.size(), bedResponses);
    }

    private void applyPropertyRequest(PgProperty property, PgPropertyRequest request, boolean creating) {
        if (request.status() == PropertyStatus.ARCHIVED) {
            throw new BusinessRuleException("Use archive action for PG", "INVALID_PROPERTY_STATUS");
        }
        property.setName(request.name().trim());
        property.setDescription(blankToNull(request.description()));
        property.setGenderType(request.genderType());
        property.setPropertyType(request.propertyType());
        property.setAddressLine1(request.addressLine1().trim());
        property.setAddressLine2(blankToNull(request.addressLine2()));
        property.setArea(request.area().trim());
        property.setCity(request.city().trim());
        property.setState(request.state().trim());
        property.setPincode(request.pincode().trim());
        property.setLatitude(request.latitude());
        property.setLongitude(request.longitude());
        property.setStartingRent(defaultAmount(request.startingRent()));
        property.setSecurityDeposit(defaultAmount(request.securityDeposit()));
        property.setNoticePeriodDays(request.noticePeriodDays());
        property.setLockInMonths(request.lockInMonths());
        property.setEntryTime(request.entryTime());
        property.setFoodAvailable(request.foodAvailable());
        if (request.status() != null || creating) {
            property.setStatus(request.status() == null ? PropertyStatus.DRAFT : request.status());
        }
    }

    private void applyRuleRequest(PropertyRule rule, PropertyRuleRequest request, Integer propertyNoticePeriodDays) {
        if (request == null) {
            rule.setNoticePeriodDays(propertyNoticePeriodDays == null ? 0 : propertyNoticePeriodDays);
            return;
        }
        rule.setVisitorAllowed(request.visitorAllowed());
        rule.setSmokingAllowed(request.smokingAllowed());
        rule.setAlcoholAllowed(request.alcoholAllowed());
        rule.setCookingAllowed(request.cookingAllowed());
        rule.setGateClosingTime(request.gateClosingTime());
        rule.setLateEntryAllowed(request.lateEntryAllowed());
        rule.setNoticePeriodDays(request.noticePeriodDays() == null ? propertyNoticePeriodDays : request.noticePeriodDays());
        rule.setAdditionalRules(blankToNull(request.additionalRules()));
    }

    private void applyFloorRequest(Floor floor, FloorRequest request) {
        if (request.status() == FloorStatus.ARCHIVED) {
            throw new BusinessRuleException("Use archive action for floor", "INVALID_FLOOR_STATUS");
        }
        floor.setName(request.name().trim());
        floor.setFloorNumber(request.floorNumber());
        floor.setDescription(blankToNull(request.description()));
        floor.setStatus(request.status() == null ? FloorStatus.ACTIVE : request.status());
    }

    private void applyRoomRequest(Room room, RoomRequest request) {
        if (request.status() == RoomStatus.ARCHIVED) {
            throw new BusinessRuleException("Use archive action for room", "INVALID_ROOM_STATUS");
        }
        room.setRoomNumber(normalizeRoomNumber(request.roomNumber()));
        room.setRoomName(blankToNull(request.roomName()));
        room.setSharingType(request.sharingType());
        room.setCapacity(request.capacity());
        room.setMonthlyRent(defaultAmount(request.monthlyRent()));
        room.setSecurityDeposit(defaultAmount(request.securityDeposit()));
        room.setAcAvailable(request.acAvailable());
        room.setAttachedBathroom(request.attachedBathroom());
        room.setFurnishingType(request.furnishingType());
        room.setStatus(request.status() == null ? RoomStatus.ACTIVE : request.status());
        room.setDescription(blankToNull(request.description()));
    }

    private void ensureRoomHasCapacity(Room room) {
        long existingBeds = bedRepository.countByRoomAndStatusNot(room, BedStatus.ARCHIVED);
        if (existingBeds >= room.getCapacity()) {
            throw new BusinessRuleException("Room capacity exceeded", "ROOM_CAPACITY_EXCEEDED");
        }
    }

    private void validateManualBedStatus(BedStatus status) {
        if (!OWNER_EDITABLE_BED_STATUSES.contains(status)) {
            throw new BusinessRuleException("Invalid bed status for owner update", "INVALID_BED_STATUS");
        }
    }

    private void makeCoverImage(PgProperty property, PgImage newCover) {
        List<PgImage> existingCovers = new ArrayList<>(pgImageRepository.findAllByPropertyAndCoverImageTrue(property));
        for (PgImage existingCover : existingCovers) {
            if (!existingCover.getId().equals(newCover.getId())) {
                existingCover.setCoverImage(false);
                pgImageRepository.save(existingCover);
            }
        }
        newCover.setCoverImage(true);
        pgImageRepository.save(newCover);
    }

    private String uniqueSlug(String name) {
        String base = slugBase(name);
        String candidate = base;
        int suffix = 2;
        while (pgPropertyRepository.existsBySlug(candidate)) {
            candidate = base + "-" + suffix;
            suffix++;
        }
        return candidate;
    }

    private String slugBase(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return normalized.isBlank() ? "pg" : normalized;
    }

    private String normalizeRoomNumber(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeBedNumber(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record PropertySnapshot(
            String name,
            String addressLine1,
            String addressLine2,
            String area,
            String city,
            String state,
            String pincode,
            Object genderType,
            Object propertyType,
            BigDecimal startingRent,
            BigDecimal securityDeposit
    ) {
        static PropertySnapshot of(PgProperty property) {
            return new PropertySnapshot(
                    property.getName(),
                    property.getAddressLine1(),
                    property.getAddressLine2(),
                    property.getArea(),
                    property.getCity(),
                    property.getState(),
                    property.getPincode(),
                    property.getGenderType(),
                    property.getPropertyType(),
                    property.getStartingRent(),
                    property.getSecurityDeposit()
            );
        }

        boolean criticalChanged(PgProperty property) {
            return !Objects.equals(name, property.getName())
                    || !Objects.equals(addressLine1, property.getAddressLine1())
                    || !Objects.equals(addressLine2, property.getAddressLine2())
                    || !Objects.equals(area, property.getArea())
                    || !Objects.equals(city, property.getCity())
                    || !Objects.equals(state, property.getState())
                    || !Objects.equals(pincode, property.getPincode())
                    || !Objects.equals(genderType, property.getGenderType())
                    || !Objects.equals(propertyType, property.getPropertyType())
                    || !amountEquals(startingRent, property.getStartingRent())
                    || !amountEquals(securityDeposit, property.getSecurityDeposit());
        }

        private boolean amountEquals(BigDecimal left, BigDecimal right) {
            if (left == null || right == null) {
                return left == right;
            }
            return left.compareTo(right) == 0;
        }
    }
}
