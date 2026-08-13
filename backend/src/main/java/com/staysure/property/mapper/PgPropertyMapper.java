package com.staysure.property.mapper;

import com.staysure.property.dto.AmenityResponse;
import com.staysure.property.dto.BedResponse;
import com.staysure.property.dto.FloorResponse;
import com.staysure.property.dto.PgImageResponse;
import com.staysure.property.dto.PgPropertyResponse;
import com.staysure.property.dto.PropertyRuleResponse;
import com.staysure.property.dto.RoomResponse;
import com.staysure.property.dto.verification.VerificationHistoryResponse;
import com.staysure.property.entity.Amenity;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.Floor;
import com.staysure.property.entity.PgImage;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.PropertyRule;
import com.staysure.property.entity.PropertyVerificationHistory;
import com.staysure.property.entity.Room;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PgPropertyMapper {

    public PgPropertyResponse toPropertyResponse(PgProperty property) {
        return new PgPropertyResponse(
                property.getId(),
                property.getOwner().getId(),
                property.getName(),
                property.getSlug(),
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
                property.getStatus(),
                property.getVerificationStatus(),
                property.getSubmittedForVerificationAt(),
                property.getVerifiedAt(),
                property.getVerifiedBy() == null ? null : property.getVerifiedBy().getId(),
                property.getVerificationRemarks(),
                property.getRejectionReason(),
                property.getCreatedAt(),
                property.getUpdatedAt()
        );
    }

    public PropertyRuleResponse toRuleResponse(PropertyRule rule) {
        if (rule == null) {
            return null;
        }
        return new PropertyRuleResponse(
                rule.getId(),
                rule.getProperty().getId(),
                rule.isVisitorAllowed(),
                rule.isSmokingAllowed(),
                rule.isAlcoholAllowed(),
                rule.isCookingAllowed(),
                rule.getGateClosingTime(),
                rule.isLateEntryAllowed(),
                rule.getNoticePeriodDays(),
                rule.getAdditionalRules(),
                rule.getCreatedAt(),
                rule.getUpdatedAt()
        );
    }

    public FloorResponse toFloorResponse(Floor floor, long roomCount, long bedCount, List<RoomResponse> rooms) {
        return new FloorResponse(
                floor.getId(),
                floor.getProperty().getId(),
                floor.getName(),
                floor.getFloorNumber(),
                floor.getDescription(),
                floor.getStatus(),
                roomCount,
                bedCount,
                rooms,
                floor.getCreatedAt(),
                floor.getUpdatedAt()
        );
    }

    public RoomResponse toRoomResponse(Room room, long bedCount, List<BedResponse> beds) {
        return new RoomResponse(
                room.getId(),
                room.getFloor().getId(),
                room.getFloor().getProperty().getId(),
                room.getRoomNumber(),
                room.getRoomName(),
                room.getSharingType(),
                room.getCapacity(),
                room.getMonthlyRent(),
                room.getSecurityDeposit(),
                room.isAcAvailable(),
                room.isAttachedBathroom(),
                room.getFurnishingType(),
                room.getStatus(),
                room.getDescription(),
                bedCount,
                beds,
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }

    public BedResponse toBedResponse(Bed bed) {
        return new BedResponse(
                bed.getId(),
                bed.getRoom().getId(),
                bed.getRoom().getFloor().getProperty().getId(),
                bed.getBedNumber(),
                bed.getBedLabel(),
                bed.getStatus(),
                bed.getCreatedAt(),
                bed.getUpdatedAt()
        );
    }

    public AmenityResponse toAmenityResponse(Amenity amenity) {
        return new AmenityResponse(
                amenity.getId(),
                amenity.getName(),
                amenity.getCode(),
                amenity.getIcon(),
                amenity.getDescription(),
                amenity.isActive(),
                amenity.getCreatedAt(),
                amenity.getUpdatedAt()
        );
    }

    public PgImageResponse toImageResponse(PgImage image) {
        return new PgImageResponse(
                image.getId(),
                image.getProperty().getId(),
                image.getImageUrl(),
                image.getCategory(),
                image.isCoverImage(),
                image.getSortOrder(),
                image.getCreatedAt()
        );
    }

    public VerificationHistoryResponse toVerificationHistoryResponse(PropertyVerificationHistory history) {
        return new VerificationHistoryResponse(
                history.getId(),
                history.getProperty().getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getRemarks(),
                history.getActionBy() == null ? null : history.getActionBy().getId(),
                history.getCreatedAt()
        );
    }
}
