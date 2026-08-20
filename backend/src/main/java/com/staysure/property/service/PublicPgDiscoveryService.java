package com.staysure.property.service;

import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.dto.PaginationResponse;
import com.staysure.property.dto.PgImageResponse;
import com.staysure.property.dto.discovery.PublicOwnerSummaryResponse;
import com.staysure.property.dto.discovery.PublicPgCardResponse;
import com.staysure.property.dto.discovery.PublicPgDetailsResponse;
import com.staysure.property.dto.discovery.PublicPgSearchRequest;
import com.staysure.property.dto.discovery.PublicRoomAvailabilityResponse;
import com.staysure.property.entity.Amenity;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.PgImage;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.PropertyRule;
import com.staysure.property.entity.Room;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.FloorStatus;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;
import com.staysure.property.enums.RoomStatus;
import com.staysure.property.mapper.PgPropertyMapper;
import com.staysure.property.repository.AmenityRepository;
import com.staysure.property.repository.BedRepository;
import com.staysure.property.repository.PgImageRepository;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.property.repository.PropertyRuleRepository;
import com.staysure.property.repository.RoomRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PublicPgDiscoveryService {

    private static final int MAX_PAGE_SIZE = 48;

    private final PgPropertyRepository pgPropertyRepository;
    private final PropertyRuleRepository propertyRuleRepository;
    private final PgImageRepository pgImageRepository;
    private final AmenityRepository amenityRepository;
    private final RoomRepository roomRepository;
    private final BedRepository bedRepository;
    private final PgPropertyMapper mapper;

    public PublicPgDiscoveryService(PgPropertyRepository pgPropertyRepository,
                                    PropertyRuleRepository propertyRuleRepository,
                                    PgImageRepository pgImageRepository,
                                    AmenityRepository amenityRepository,
                                    RoomRepository roomRepository,
                                    BedRepository bedRepository,
                                    PgPropertyMapper mapper) {
        this.pgPropertyRepository = pgPropertyRepository;
        this.propertyRuleRepository = propertyRuleRepository;
        this.pgImageRepository = pgImageRepository;
        this.amenityRepository = amenityRepository;
        this.roomRepository = roomRepository;
        this.bedRepository = bedRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PaginationResponse<PublicPgCardResponse> search(PublicPgSearchRequest request, int page, int size) {
        validateRentRange(request.minRent(), request.maxRent());
        Pageable pageable = pageable(page, size, request.sort());
        Page<PgProperty> properties = pgPropertyRepository.findAll(publicSpec(request),
                Objects.requireNonNull(pageable, "pageable must not be null"));
        List<PublicPgCardResponse> cards = cardsForProperties(properties.getContent());
        return PaginationResponse.from(properties, cards);
    }

    @Transactional(readOnly = true)
    public PublicPgDetailsResponse details(String slug) {
        PgProperty property = pgPropertyRepository
                .findBySlugAndStatusAndVerificationStatus(slug, PropertyStatus.ACTIVE, PropertyVerificationStatus.VERIFIED)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "PG not found", "PUBLIC_PG_NOT_FOUND"));
        PropertyRule rule = propertyRuleRepository.findByProperty(property).orElse(null);
        List<AmenityResponse> amenities = property.getAmenities().stream()
                .sorted((left, right) -> compareNullable(left.getName(), right.getName()))
                .map(mapper::toAmenityResponse)
                .toList();
        List<PgImageResponse> gallery = pgImageRepository.findAllByPropertyOrderBySortOrderAscCreatedAtAsc(property)
                .stream()
                .map(mapper::toImageResponse)
                .toList();
        List<Room> rooms = roomRepository.findPublicRooms(property, RoomStatus.ACTIVE, FloorStatus.ACTIVE);
        List<Long> roomIds = rooms.stream()
                .map(room -> Objects.requireNonNull(room.getId(), "room id must not be null"))
                .toList();
        Map<Long, Long> availableByRoomId = roomIds.isEmpty()
                ? Map.of()
                : countById(bedRepository.countByRoomIdsAndStatus(roomIds, BedStatus.AVAILABLE));
        List<PublicRoomAvailabilityResponse> availableRooms = rooms.stream()
                .map(room -> toRoomAvailability(room, availableByRoomId.getOrDefault(room.getId(), 0L)))
                .filter(room -> room.availableBeds() > 0)
                .toList();
        long totalBeds = bedRepository.countByPublicPropertyAndStatusNot(property, BedStatus.ARCHIVED, RoomStatus.ACTIVE, FloorStatus.ACTIVE);
        long availableBeds = bedRepository.countByPublicPropertyAndStatus(property, BedStatus.AVAILABLE, RoomStatus.ACTIVE, FloorStatus.ACTIVE);
        return new PublicPgDetailsResponse(
                property.getId(),
                property.getSlug(),
                property.getName(),
                property.getDescription(),
                property.getGenderType(),
                property.getPropertyType(),
                property.getAddressLine1(),
                property.getAddressLine2(),
                property.getArea(),
                property.getCity(),
                property.getState(),
                property.getPincode(),
                property.getLatitude(),
                property.getLongitude(),
                property.getStartingRent(),
                property.getSecurityDeposit(),
                property.getNoticePeriodDays(),
                property.getLockInMonths(),
                property.getEntryTime(),
                property.isFoodAvailable(),
                mapper.toRuleResponse(rule),
                amenities,
                gallery,
                availableRooms,
                availableBeds,
                totalBeds,
                new PublicOwnerSummaryResponse(property.getOwner().getBusinessName(), property.getOwner().getExperienceYears())
        );
    }

    @Transactional(readOnly = true)
    public List<AmenityResponse> amenities() {
        return amenityRepository.findAllByActiveTrueOrderByNameAsc().stream()
                .map(mapper::toAmenityResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PublicPgCardResponse> cardsForProperties(List<PgProperty> properties) {
        if (properties.isEmpty()) {
            return List.of();
        }
        List<Long> propertyIds = properties.stream()
                .map(property -> Objects.requireNonNull(property.getId(), "property id must not be null"))
                .toList();
        Map<Long, Long> totalBeds = countById(bedRepository.countByPublicPropertyIdsAndStatusNot(
                propertyIds, BedStatus.ARCHIVED, RoomStatus.ACTIVE, FloorStatus.ACTIVE));
        Map<Long, Long> availableBeds = countById(bedRepository.countByPublicPropertyIdsAndStatus(
                propertyIds, BedStatus.AVAILABLE, RoomStatus.ACTIVE, FloorStatus.ACTIVE));
        Map<Long, String> coverImages = coverImages(propertyIds);
        Map<Long, List<AmenityResponse>> amenitiesByProperty = amenitiesByProperty(propertyIds);
        return properties.stream()
                .map(property -> toCard(property, coverImages.get(property.getId()),
                        totalBeds.getOrDefault(property.getId(), 0L),
                        availableBeds.getOrDefault(property.getId(), 0L),
                        amenitiesByProperty.getOrDefault(property.getId(), List.of())))
                .toList();
    }

    public boolean isPubliclyVisible(PgProperty property) {
        return property.getStatus() == PropertyStatus.ACTIVE
                && property.getVerificationStatus() == PropertyVerificationStatus.VERIFIED;
    }

    private PublicPgCardResponse toCard(PgProperty property, String coverImage, long totalBeds,
                                        long availableBeds, List<AmenityResponse> amenities) {
        return new PublicPgCardResponse(
                property.getId(),
                property.getSlug(),
                property.getName(),
                coverImage,
                property.getArea(),
                property.getCity(),
                property.getGenderType(),
                property.getPropertyType(),
                property.getStartingRent(),
                property.getSecurityDeposit(),
                property.isFoodAvailable(),
                null,
                totalBeds,
                availableBeds,
                property.getVerificationStatus(),
                amenities.stream().limit(5).toList()
        );
    }

    private PublicRoomAvailabilityResponse toRoomAvailability(Room room, long availableBeds) {
        return new PublicRoomAvailabilityResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getSharingType(),
                room.getMonthlyRent(),
                room.getSecurityDeposit(),
                room.getCapacity(),
                availableBeds,
                room.isAcAvailable(),
                room.isAttachedBathroom(),
                room.getFurnishingType()
        );
    }

    private Pageable pageable(int page, int size, String sort) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        String selectedSort = sort == null || sort.isBlank() ? "latest" : sort.trim().toLowerCase(Locale.ROOT);
        return switch (selectedSort) {
            case "price_low_to_high" -> PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.ASC, "startingRent"));
            case "price_high_to_low" -> PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "startingRent"));
            case "availability" -> PageRequest.of(safePage, safeSize);
            case "latest" -> PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
            default -> throw new BusinessRuleException("Invalid sort option", "INVALID_SORT");
        };
    }

    private Specification<PgProperty> publicSpec(PublicPgSearchRequest request) {
        return (root, query, cb) -> {
            if (query == null || cb == null) {
                throw new IllegalArgumentException("Query and CriteriaBuilder cannot be null");
            }
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), PropertyStatus.ACTIVE));
            predicates.add(cb.equal(root.get("verificationStatus"), PropertyVerificationStatus.VERIFIED));
            if (hasText(request.search())) {
                String pattern = like(request.search());
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), pattern),
                        cb.like(cb.lower(root.get("city")), pattern),
                        cb.like(cb.lower(root.get("area")), pattern),
                        cb.like(cb.lower(root.get("addressLine1")), pattern),
                        cb.like(cb.lower(root.get("addressLine2")), pattern)
                ));
            }
            if (hasText(request.city())) {
                predicates.add(cb.equal(cb.lower(root.get("city")), request.city().trim().toLowerCase(Locale.ROOT)));
            }
            if (hasText(request.area())) {
                predicates.add(cb.equal(cb.lower(root.get("area")), request.area().trim().toLowerCase(Locale.ROOT)));
            }
            if (request.minRent() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startingRent"), request.minRent()));
            }
            if (request.maxRent() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startingRent"), request.maxRent()));
            }
            if (request.genderType() != null) {
                predicates.add(cb.equal(root.get("genderType"), request.genderType()));
            }
            if (request.propertyType() != null) {
                predicates.add(cb.equal(root.get("propertyType"), request.propertyType()));
            }
            if (request.foodAvailable() != null) {
                predicates.add(cb.equal(root.get("foodAvailable"), request.foodAvailable()));
            }
            if (request.sharingType() != null) {
                Subquery<Long> sharingSubquery = query.subquery(Long.class);
                Root<Room> room = sharingSubquery.from(Room.class);
                sharingSubquery.select(room.get("id")).where(
                        cb.equal(room.get("floor").get("property").get("id"), root.get("id")),
                        cb.equal(room.get("floor").get("status"), FloorStatus.ACTIVE),
                        cb.equal(room.get("status"), RoomStatus.ACTIVE),
                        cb.equal(room.get("sharingType"), request.sharingType())
                );
                predicates.add(cb.exists(sharingSubquery));
            }
            if (request.availableOnly()) {
                Subquery<Long> availabilitySubquery = query.subquery(Long.class);
                Root<Bed> bed = availabilitySubquery.from(Bed.class);
                availabilitySubquery.select(bed.get("id")).where(
                        cb.equal(bed.get("room").get("floor").get("property").get("id"), root.get("id")),
                        cb.equal(bed.get("room").get("floor").get("status"), FloorStatus.ACTIVE),
                        cb.equal(bed.get("room").get("status"), RoomStatus.ACTIVE),
                        cb.equal(bed.get("status"), BedStatus.AVAILABLE)
                );
                predicates.add(cb.exists(availabilitySubquery));
            }
            for (Long amenityId : sanitizedAmenityIds(request.amenityIds())) {
                Subquery<Long> amenitySubquery = query.subquery(Long.class);
                Root<PgProperty> property = amenitySubquery.from(PgProperty.class);
                var amenity = property.join("amenities");
                amenitySubquery.select(property.get("id")).where(
                        cb.equal(property.get("id"), root.get("id")),
                        cb.equal(amenity.get("id"), amenityId)
                );
                predicates.add(cb.exists(amenitySubquery));
            }
            if (query != null && !Long.class.equals(query.getResultType()) && "availability".equalsIgnoreCase(nullToBlank(request.sort()))) {
                Subquery<Long> countSubquery = query.subquery(Long.class);
                Root<Bed> bed = countSubquery.from(Bed.class);
                countSubquery.select(cb.count(bed)).where(
                        cb.equal(bed.get("room").get("floor").get("property").get("id"), root.get("id")),
                        cb.equal(bed.get("room").get("floor").get("status"), FloorStatus.ACTIVE),
                        cb.equal(bed.get("room").get("status"), RoomStatus.ACTIVE),
                        cb.equal(bed.get("status"), BedStatus.AVAILABLE)
                );
                query.orderBy(cb.desc(countSubquery), cb.desc(root.get("createdAt")));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Map<Long, Long> countById(List<Object[]> rows) {
        Map<Long, Long> counts = new HashMap<>();
        for (Object[] row : rows) {
            counts.put((Long) row[0], (Long) row[1]);
        }
        return counts;
    }

    private Map<Long, String> coverImages(Collection<Long> propertyIds) {
        Map<Long, String> images = new LinkedHashMap<>();
        for (PgImage image : pgImageRepository.findAllByPropertyIdsForCards(propertyIds)) {
            images.putIfAbsent(image.getProperty().getId(), image.getImageUrl());
        }
        return images;
    }

    private Map<Long, List<AmenityResponse>> amenitiesByProperty(Collection<Long> propertyIds) {
        Map<Long, List<AmenityResponse>> grouped = new HashMap<>();
        for (Object[] row : amenityRepository.findAmenitiesByPropertyIds(propertyIds)) {
            Long propertyId = (Long) row[0];
            Amenity amenity = (Amenity) row[1];
            grouped.computeIfAbsent(propertyId, ignored -> new ArrayList<>()).add(mapper.toAmenityResponse(amenity));
        }
        return grouped;
    }

    private void validateRentRange(BigDecimal minRent, BigDecimal maxRent) {
        if (minRent != null && maxRent != null && minRent.compareTo(maxRent) > 0) {
            throw new BusinessRuleException("Minimum rent cannot be greater than maximum rent", "INVALID_RENT_RANGE");
        }
    }

    private Set<Long> sanitizedAmenityIds(List<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return Set.of();
        }
        return amenityIds.stream().filter(id -> id != null && id > 0).collect(Collectors.toCollection(java.util.LinkedHashSet::new));
    }

    private String like(String value) {
        return "%" + value.trim().toLowerCase(Locale.ROOT) + "%";
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String nullToBlank(String value) {
        return value == null ? "" : value.trim();
    }

    private int compareNullable(String left, String right) {
        return Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER).compare(left, right);
    }
}
