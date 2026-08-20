package com.staysure.booking.mapper;

import com.staysure.booking.dto.BookingBedSummary;
import com.staysure.booking.dto.BookingPropertySummary;
import com.staysure.booking.dto.BookingResponse;
import com.staysure.booking.dto.BookingRoomSummary;
import com.staysure.booking.dto.BookingStatusHistoryResponse;
import com.staysure.booking.dto.BookingUserSummary;
import com.staysure.booking.dto.RentalAgreementResponse;
import com.staysure.booking.dto.SecurityDepositResponse;
import com.staysure.booking.dto.TenantDocumentResponse;
import com.staysure.booking.dto.TenantProfileResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.BookingStatusHistory;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.entity.TenantDocument;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.user.entity.User;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BookingMapper {

    public BookingResponse toResponse(Booking booking,
                                      List<TenantDocument> documents,
                                      SecurityDeposit deposit,
                                      RentalAgreement agreement,
                                      TenantProfile tenant,
                                      List<BookingStatusHistory> history) {
        return new BookingResponse(
                booking.getId(),
                booking.getBookingNumber(),
                booking.getStatus(),
                toUserSummary(booking.getUser()),
                toPropertySummary(booking.getProperty()),
                toRoomSummary(booking.getRoom()),
                toBedSummary(booking.getBed()),
                booking.getMoveInDate(),
                booking.getExpectedMoveOutDate(),
                booking.getMonthlyRent(),
                booking.getSecurityDeposit(),
                booking.getRequestedAt(),
                booking.getApprovedAt(),
                booking.getRejectedAt(),
                booking.getCancelledAt(),
                booking.getConfirmedAt(),
                booking.getCheckedInAt(),
                booking.getRejectionReason(),
                booking.getCancellationReason(),
                booking.getRemarks(),
                documents.stream().map(this::toDocumentResponse).toList(),
                toDepositResponse(deposit),
                toAgreementResponse(agreement),
                toTenantResponse(tenant),
                history.stream().map(this::toHistoryResponse).toList()
        );
    }

    public TenantDocumentResponse toDocumentResponse(TenantDocument document) {
        return new TenantDocumentResponse(
                document.getId(),
                document.getBooking().getId(),
                document.getDocumentType(),
                document.getDocumentNumber(),
                document.getDocumentUrl(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getVerificationStatus(),
                document.getRejectionReason(),
                document.getVerifiedBy() == null ? null : document.getVerifiedBy().getId(),
                document.getVerifiedAt(),
                document.getCreatedAt()
        );
    }

    public SecurityDepositResponse toDepositResponse(SecurityDeposit deposit) {
        if (deposit == null) {
            return null;
        }
        BigDecimal remaining = deposit.getRequiredAmount().subtract(deposit.getPaidAmount()).max(BigDecimal.ZERO);
        return new SecurityDepositResponse(
                deposit.getId(),
                deposit.getBooking().getId(),
                deposit.getRequiredAmount(),
                deposit.getPaidAmount(),
                remaining,
                deposit.getStatus(),
                deposit.getLastPaymentMethod(),
                deposit.getLastPaymentReference(),
                deposit.getRemarks(),
                deposit.getPaidAt()
        );
    }

    public RentalAgreementResponse toAgreementResponse(RentalAgreement agreement) {
        if (agreement == null) {
            return null;
        }
        return new RentalAgreementResponse(
                agreement.getId(),
                agreement.getBooking().getId(),
                agreement.getAgreementNumber(),
                agreement.getStatus(),
                agreement.getDocumentUrl(),
                agreement.getOriginalFileName(),
                agreement.getTerms(),
                agreement.getStartDate(),
                agreement.getEndDate(),
                agreement.getMonthlyRent(),
                agreement.getSecurityDeposit(),
                agreement.getNoticePeriodDays(),
                agreement.getLockInMonths(),
                agreement.getIssuedAt(),
                agreement.getAcceptedAt()
        );
    }

    public TenantProfileResponse toTenantResponse(TenantProfile tenant) {
        if (tenant == null) {
            return null;
        }
        return new TenantProfileResponse(
                tenant.getId(),
                tenant.getBooking().getId(),
                toUserSummary(tenant.getUser()),
                toPropertySummary(tenant.getProperty()),
                toRoomSummary(tenant.getRoom()),
                toBedSummary(tenant.getBed()),
                tenant.getStatus(),
                tenant.getJoiningDate(),
                tenant.getExpectedCheckoutDate(),
                tenant.getCreatedAt()
        );
    }

    public BookingStatusHistoryResponse toHistoryResponse(BookingStatusHistory history) {
        return new BookingStatusHistoryResponse(
                history.getId(),
                history.getPreviousStatus(),
                history.getNewStatus(),
                history.getRemarks(),
                history.getActionBy() == null ? null : history.getActionBy().getId(),
                history.getCreatedAt()
        );
    }

    public BookingUserSummary toUserSummary(User user) {
        return new BookingUserSummary(user.getId(), user.getFirstName(), user.getLastName(), user.getPhone(), user.getEmail());
    }

    public BookingPropertySummary toPropertySummary(PgProperty property) {
        return new BookingPropertySummary(
                property.getId(),
                property.getSlug(),
                property.getName(),
                property.getArea(),
                property.getCity(),
                property.getAddressLine1()
        );
    }

    public BookingRoomSummary toRoomSummary(Room room) {
        return new BookingRoomSummary(
                room.getId(),
                room.getRoomNumber(),
                room.getSharingType(),
                room.getMonthlyRent(),
                room.getSecurityDeposit(),
                room.getCapacity(),
                room.isAcAvailable(),
                room.isAttachedBathroom(),
                room.getFurnishingType()
        );
    }

    public BookingBedSummary toBedSummary(Bed bed) {
        return new BookingBedSummary(bed.getId(), bed.getBedNumber(), bed.getBedLabel(), bed.getStatus());
    }
}
