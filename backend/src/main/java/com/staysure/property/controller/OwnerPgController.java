package com.staysure.property.controller;

import com.staysure.auth.security.SecurityUtils;
import com.staysure.common.response.ApiResponse;
import com.staysure.common.util.RequestUtils;
import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.dto.BedRequest;
import com.staysure.property.dto.BedResponse;
import com.staysure.property.dto.FloorRequest;
import com.staysure.property.dto.FloorResponse;
import com.staysure.property.dto.PgImageOrderRequest;
import com.staysure.property.dto.PgImageResponse;
import com.staysure.property.dto.PgPropertyRequest;
import com.staysure.property.dto.PropertyAmenityUpdateRequest;
import com.staysure.property.dto.PropertyDetailsResponse;
import com.staysure.property.dto.PropertyStatusUpdateRequest;
import com.staysure.property.dto.PropertySummaryResponse;
import com.staysure.property.dto.RoomRequest;
import com.staysure.property.dto.RoomResponse;
import com.staysure.property.dto.verification.SubmitVerificationResponse;
import com.staysure.property.enums.ImageCategory;
import com.staysure.property.service.PgPropertyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/owner")
@PreAuthorize("hasRole('PG_OWNER')")
public class OwnerPgController {

    private final PgPropertyService pgPropertyService;

    public OwnerPgController(PgPropertyService pgPropertyService) {
        this.pgPropertyService = pgPropertyService;
    }

    @PostMapping("/pgs")
    public ResponseEntity<ApiResponse<PropertyDetailsResponse>> createPg(@Valid @RequestBody PgPropertyRequest request,
                                                                         HttpServletRequest servletRequest) {
        PropertyDetailsResponse response = pgPropertyService.createProperty(
                SecurityUtils.currentUserId(),
                request,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG created successfully", response));
    }

    @GetMapping("/pgs")
    public ResponseEntity<ApiResponse<List<PropertySummaryResponse>>> listPgs() {
        return ResponseEntity.ok(ApiResponse.success("PGs loaded",
                pgPropertyService.listProperties(SecurityUtils.currentUserId())));
    }

    @GetMapping("/pgs/{pgId}")
    public ResponseEntity<ApiResponse<PropertyDetailsResponse>> getPg(@PathVariable Long pgId) {
        return ResponseEntity.ok(ApiResponse.success("PG loaded",
                pgPropertyService.getProperty(SecurityUtils.currentUserId(), pgId)));
    }

    @PutMapping("/pgs/{pgId}")
    public ResponseEntity<ApiResponse<PropertyDetailsResponse>> updatePg(@PathVariable Long pgId,
                                                                         @Valid @RequestBody PgPropertyRequest request,
                                                                         HttpServletRequest servletRequest) {
        PropertyDetailsResponse response = pgPropertyService.updateProperty(
                SecurityUtils.currentUserId(),
                pgId,
                request,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG updated successfully", response));
    }

    @PatchMapping("/pgs/{pgId}/status")
    public ResponseEntity<ApiResponse<PropertyDetailsResponse>> updatePgStatus(@PathVariable Long pgId,
                                                                               @Valid @RequestBody PropertyStatusUpdateRequest request,
                                                                               HttpServletRequest servletRequest) {
        PropertyDetailsResponse response = pgPropertyService.updatePropertyStatus(
                SecurityUtils.currentUserId(),
                pgId,
                request.status(),
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG status updated", response));
    }

    @DeleteMapping("/pgs/{pgId}")
    public ResponseEntity<ApiResponse<Void>> archivePg(@PathVariable Long pgId, HttpServletRequest servletRequest) {
        pgPropertyService.archiveProperty(SecurityUtils.currentUserId(), pgId, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("PG archived successfully"));
    }

    @PostMapping("/pgs/{pgId}/submit-verification")
    public ResponseEntity<ApiResponse<SubmitVerificationResponse>> submitVerification(@PathVariable Long pgId,
                                                                                     HttpServletRequest servletRequest) {
        SubmitVerificationResponse response = pgPropertyService.submitForVerification(
                SecurityUtils.currentUserId(),
                pgId,
                RequestUtils.getClientIp(servletRequest)
        );
        return ResponseEntity.ok(ApiResponse.success("PG submitted for verification", response));
    }

    @PostMapping("/pgs/{pgId}/floors")
    public ResponseEntity<ApiResponse<FloorResponse>> createFloor(@PathVariable Long pgId,
                                                                  @Valid @RequestBody FloorRequest request,
                                                                  HttpServletRequest servletRequest) {
        FloorResponse response = pgPropertyService.createFloor(SecurityUtils.currentUserId(), pgId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Floor created successfully", response));
    }

    @GetMapping("/pgs/{pgId}/floors")
    public ResponseEntity<ApiResponse<List<FloorResponse>>> listFloors(@PathVariable Long pgId) {
        return ResponseEntity.ok(ApiResponse.success("Floors loaded",
                pgPropertyService.listFloors(SecurityUtils.currentUserId(), pgId)));
    }

    @GetMapping("/pgs/{pgId}/floors/{floorId}")
    public ResponseEntity<ApiResponse<FloorResponse>> getFloor(@PathVariable Long pgId,
                                                               @PathVariable Long floorId) {
        return ResponseEntity.ok(ApiResponse.success("Floor loaded",
                pgPropertyService.getFloor(SecurityUtils.currentUserId(), pgId, floorId)));
    }

    @PutMapping("/pgs/{pgId}/floors/{floorId}")
    public ResponseEntity<ApiResponse<FloorResponse>> updateFloor(@PathVariable Long pgId,
                                                                  @PathVariable Long floorId,
                                                                  @Valid @RequestBody FloorRequest request,
                                                                  HttpServletRequest servletRequest) {
        FloorResponse response = pgPropertyService.updateFloor(SecurityUtils.currentUserId(), pgId, floorId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Floor updated successfully", response));
    }

    @DeleteMapping("/pgs/{pgId}/floors/{floorId}")
    public ResponseEntity<ApiResponse<Void>> archiveFloor(@PathVariable Long pgId,
                                                         @PathVariable Long floorId,
                                                         HttpServletRequest servletRequest) {
        pgPropertyService.archiveFloor(SecurityUtils.currentUserId(), pgId, floorId, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Floor archived successfully"));
    }

    @PostMapping("/pgs/{pgId}/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@PathVariable Long pgId,
                                                                @PathVariable Long floorId,
                                                                @Valid @RequestBody RoomRequest request,
                                                                HttpServletRequest servletRequest) {
        RoomResponse response = pgPropertyService.createRoom(SecurityUtils.currentUserId(), pgId, floorId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Room created successfully", response));
    }

    @GetMapping("/pgs/{pgId}/floors/{floorId}/rooms")
    public ResponseEntity<ApiResponse<List<RoomResponse>>> listRooms(@PathVariable Long pgId,
                                                                     @PathVariable Long floorId) {
        return ResponseEntity.ok(ApiResponse.success("Rooms loaded",
                pgPropertyService.listRooms(SecurityUtils.currentUserId(), pgId, floorId)));
    }

    @GetMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable Long pgId,
                                                             @PathVariable Long floorId,
                                                             @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success("Room loaded",
                pgPropertyService.getRoom(SecurityUtils.currentUserId(), pgId, floorId, roomId)));
    }

    @PutMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(@PathVariable Long pgId,
                                                                @PathVariable Long floorId,
                                                                @PathVariable Long roomId,
                                                                @Valid @RequestBody RoomRequest request,
                                                                HttpServletRequest servletRequest) {
        RoomResponse response = pgPropertyService.updateRoom(SecurityUtils.currentUserId(), pgId, floorId, roomId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Room updated successfully", response));
    }

    @DeleteMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}")
    public ResponseEntity<ApiResponse<Void>> archiveRoom(@PathVariable Long pgId,
                                                        @PathVariable Long floorId,
                                                        @PathVariable Long roomId,
                                                        HttpServletRequest servletRequest) {
        pgPropertyService.archiveRoom(SecurityUtils.currentUserId(), pgId, floorId, roomId, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Room archived successfully"));
    }

    @PostMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}/beds")
    public ResponseEntity<ApiResponse<BedResponse>> createBed(@PathVariable Long pgId,
                                                              @PathVariable Long floorId,
                                                              @PathVariable Long roomId,
                                                              @Valid @RequestBody BedRequest request,
                                                              HttpServletRequest servletRequest) {
        BedResponse response = pgPropertyService.createBed(SecurityUtils.currentUserId(), pgId, floorId, roomId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Bed created successfully", response));
    }

    @GetMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}/beds")
    public ResponseEntity<ApiResponse<List<BedResponse>>> listBeds(@PathVariable Long pgId,
                                                                   @PathVariable Long floorId,
                                                                   @PathVariable Long roomId) {
        return ResponseEntity.ok(ApiResponse.success("Beds loaded",
                pgPropertyService.listBeds(SecurityUtils.currentUserId(), pgId, floorId, roomId)));
    }

    @GetMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}/beds/{bedId}")
    public ResponseEntity<ApiResponse<BedResponse>> getBed(@PathVariable Long pgId,
                                                           @PathVariable Long floorId,
                                                           @PathVariable Long roomId,
                                                           @PathVariable Long bedId) {
        return ResponseEntity.ok(ApiResponse.success("Bed loaded",
                pgPropertyService.getBed(SecurityUtils.currentUserId(), pgId, floorId, roomId, bedId)));
    }

    @PutMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}/beds/{bedId}")
    public ResponseEntity<ApiResponse<BedResponse>> updateBed(@PathVariable Long pgId,
                                                              @PathVariable Long floorId,
                                                              @PathVariable Long roomId,
                                                              @PathVariable Long bedId,
                                                              @Valid @RequestBody BedRequest request,
                                                              HttpServletRequest servletRequest) {
        BedResponse response = pgPropertyService.updateBed(SecurityUtils.currentUserId(), pgId, floorId, roomId, bedId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Bed updated successfully", response));
    }

    @DeleteMapping("/pgs/{pgId}/floors/{floorId}/rooms/{roomId}/beds/{bedId}")
    public ResponseEntity<ApiResponse<Void>> archiveBed(@PathVariable Long pgId,
                                                       @PathVariable Long floorId,
                                                       @PathVariable Long roomId,
                                                       @PathVariable Long bedId,
                                                       HttpServletRequest servletRequest) {
        pgPropertyService.archiveBed(SecurityUtils.currentUserId(), pgId, floorId, roomId, bedId, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Bed archived successfully"));
    }

    @GetMapping("/amenities")
    public ResponseEntity<ApiResponse<List<AmenityResponse>>> amenities() {
        return ResponseEntity.ok(ApiResponse.success("Amenities loaded",
                pgPropertyService.listAmenities(SecurityUtils.currentUserId())));
    }

    @PutMapping("/pgs/{pgId}/amenities")
    public ResponseEntity<ApiResponse<List<AmenityResponse>>> updateAmenities(@PathVariable Long pgId,
                                                                              @Valid @RequestBody PropertyAmenityUpdateRequest request,
                                                                              HttpServletRequest servletRequest) {
        List<AmenityResponse> response = pgPropertyService.updateAmenities(SecurityUtils.currentUserId(), pgId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Amenities updated successfully", response));
    }

    @PostMapping(value = "/pgs/{pgId}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<PgImageResponse>> uploadImage(@PathVariable Long pgId,
                                                                    @RequestParam(defaultValue = "OTHER") ImageCategory category,
                                                                    @RequestParam(defaultValue = "false") boolean coverImage,
                                                                    @RequestParam(required = false) Integer sortOrder,
                                                                    @RequestPart("file") MultipartFile file,
                                                                    HttpServletRequest servletRequest) {
        PgImageResponse response = pgPropertyService.uploadImage(SecurityUtils.currentUserId(), pgId, file, category,
                coverImage, sortOrder, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("PG image uploaded successfully", response));
    }

    @GetMapping("/pgs/{pgId}/images")
    public ResponseEntity<ApiResponse<List<PgImageResponse>>> listImages(@PathVariable Long pgId) {
        return ResponseEntity.ok(ApiResponse.success("PG images loaded",
                pgPropertyService.listImages(SecurityUtils.currentUserId(), pgId)));
    }

    @PatchMapping("/pgs/{pgId}/images/{imageId}/cover")
    public ResponseEntity<ApiResponse<PgImageResponse>> setCoverImage(@PathVariable Long pgId,
                                                                      @PathVariable Long imageId,
                                                                      HttpServletRequest servletRequest) {
        PgImageResponse response = pgPropertyService.setCoverImage(SecurityUtils.currentUserId(), pgId, imageId,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("Cover image updated successfully", response));
    }

    @PutMapping("/pgs/{pgId}/images/reorder")
    public ResponseEntity<ApiResponse<List<PgImageResponse>>> reorderImages(@PathVariable Long pgId,
                                                                            @Valid @RequestBody List<PgImageOrderRequest> request,
                                                                            HttpServletRequest servletRequest) {
        List<PgImageResponse> response = pgPropertyService.reorderImages(SecurityUtils.currentUserId(), pgId, request,
                RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("PG images reordered successfully", response));
    }

    @DeleteMapping("/pgs/{pgId}/images/{imageId}")
    public ResponseEntity<ApiResponse<Void>> deleteImage(@PathVariable Long pgId,
                                                        @PathVariable Long imageId,
                                                        HttpServletRequest servletRequest) {
        pgPropertyService.deleteImage(SecurityUtils.currentUserId(), pgId, imageId, RequestUtils.getClientIp(servletRequest));
        return ResponseEntity.ok(ApiResponse.success("PG image deleted successfully"));
    }
}
