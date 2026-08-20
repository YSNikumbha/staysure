package com.staysure.property.controller;

import com.staysure.common.response.ApiResponse;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.dto.PaginationResponse;
import com.staysure.property.dto.discovery.PublicPgCardResponse;
import com.staysure.property.dto.discovery.PublicPgDetailsResponse;
import com.staysure.property.dto.discovery.PublicPgSearchRequest;
import com.staysure.property.enums.GenderType;
import com.staysure.property.enums.PropertyType;
import com.staysure.property.enums.SharingType;
import com.staysure.property.service.PublicPgDiscoveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/public")
public class PublicPgController {

    private final PublicPgDiscoveryService publicPgDiscoveryService;

    public PublicPgController(PublicPgDiscoveryService publicPgDiscoveryService) {
        this.publicPgDiscoveryService = publicPgDiscoveryService;
    }

    @GetMapping("/pgs")
    public ResponseEntity<ApiResponse<PaginationResponse<PublicPgCardResponse>>> search(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String area,
            @RequestParam(required = false) BigDecimal minRent,
            @RequestParam(required = false) BigDecimal maxRent,
            @RequestParam(required = false) GenderType genderType,
            @RequestParam(required = false) PropertyType propertyType,
            @RequestParam(required = false) SharingType sharingType,
            @RequestParam(required = false) Boolean foodAvailable,
            @RequestParam(required = false) String amenityIds,
            @RequestParam(defaultValue = "false") boolean availableOnly,
            @RequestParam(defaultValue = "latest") String sort
    ) {
        PublicPgSearchRequest request = new PublicPgSearchRequest(
                search,
                city,
                area,
                minRent,
                maxRent,
                genderType,
                propertyType,
                sharingType,
                foodAvailable,
                parseAmenityIds(amenityIds),
                availableOnly,
                sort
        );
        return ResponseEntity.ok(ApiResponse.success("PGs loaded",
                publicPgDiscoveryService.search(request, page, size)));
    }

    @GetMapping("/pgs/{slug}")
    public ResponseEntity<ApiResponse<PublicPgDetailsResponse>> details(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success("PG loaded", publicPgDiscoveryService.details(slug)));
    }

    @GetMapping("/amenities")
    public ResponseEntity<ApiResponse<List<AmenityResponse>>> amenities() {
        return ResponseEntity.ok(ApiResponse.success("Amenities loaded", publicPgDiscoveryService.amenities()));
    }

    private List<Long> parseAmenityIds(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(value.split(","))
                .map(item -> item.trim())
                .filter(item -> !item.isBlank())
                .map(this::parseAmenityId)
                .toList();
    }

    private Long parseAmenityId(String value) {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException ex) {
            throw new BusinessRuleException("Invalid amenity filter", "INVALID_AMENITY_FILTER");
        }
    }
}
