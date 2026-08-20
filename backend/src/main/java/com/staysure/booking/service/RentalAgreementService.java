package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.enums.AgreementStatus;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.repository.RentalAgreementRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

@Service
public class RentalAgreementService {

    private final RentalAgreementRepository rentalAgreementRepository;
    private final BookingService bookingService;
    private final UserService userService;
    private final AuditService auditService;

    public RentalAgreementService(RentalAgreementRepository rentalAgreementRepository,
                                  BookingService bookingService,
                                  UserService userService,
                                  AuditService auditService) {
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.bookingService = bookingService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional
    public RentalAgreement createAgreement(Long ownerId, Long bookingId, String ipAddress) {
        User owner = userService.getUser(ownerId);
        Booking booking = bookingService.getOwnerBooking(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.AWAITING_AGREEMENT) {
            throw new BusinessRuleException("Agreement not allowed in current booking status", "AGREEMENT_NOT_READY");
        }

        if (rentalAgreementRepository.existsByBookingId(bookingId)) {
            throw new BusinessRuleException("Agreement already exists", "AGREEMENT_ALREADY_EXISTS");
        }

        RentalAgreement agreement = new RentalAgreement();
        agreement.setBooking(booking);
        agreement.setProperty(booking.getProperty());
        agreement.setUser(booking.getUser());
        agreement.setAgreementNumber(generateAgreementNumber());
        agreement.setStartDate(booking.getMoveInDate());
        agreement.setEndDate(booking.getExpectedMoveOutDate());
        agreement.setMonthlyRent(booking.getMonthlyRent());
        agreement.setSecurityDeposit(booking.getSecurityDepositAmount());
        agreement.setNoticePeriodDays(booking.getProperty().getNoticePeriodDays());
        agreement.setLockInMonths(booking.getProperty().getLockInMonths());
        agreement.setStatus(AgreementStatus.ISSUED);

        RentalAgreement saved = rentalAgreementRepository.save(agreement);
        auditService.log(owner, "AGREEMENT_CREATED", "RENTAL_AGREEMENT", "RentalAgreement", saved.getId(),
                "Rental agreement created", null, saved.getAgreementNumber(), ipAddress);

        return saved;
    }

    @Transactional(readOnly = true)
    public RentalAgreement getAgreement(Long userId, Long bookingId) {
        bookingService.getUserBooking(userId, bookingId);
        return rentalAgreementRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agreement not found", "AGREEMENT_NOT_FOUND"));
    }

    @Transactional
    public RentalAgreement acceptAgreement(Long userId, Long bookingId, String ipAddress) {
        User user = userService.getUser(userId);
        Booking booking = bookingService.getUserBooking(userId, bookingId);

        if (booking.getStatus() != BookingStatus.AWAITING_AGREEMENT) {
            throw new BusinessRuleException("Agreement acceptance not allowed in current booking status", "AGREEMENT_NOT_READY");
        }

        RentalAgreement agreement = rentalAgreementRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agreement not found", "AGREEMENT_NOT_FOUND"));

        if (!agreement.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Agreement access denied", "AGREEMENT_ACCESS_DENIED");
        }

        agreement.setStatus(AgreementStatus.ACCEPTED);
        RentalAgreement saved = rentalAgreementRepository.save(agreement);

        auditService.log(user, "AGREEMENT_ACCEPTED", "RENTAL_AGREEMENT", "RentalAgreement", saved.getId(),
                "Rental agreement accepted", null, saved.getAgreementNumber(), ipAddress);

        return saved;
    }

    private String generateAgreementNumber() {
        return "AGR-" + LocalDate.now().getYear() + "-" + String.format("%06d", UUID.randomUUID().getLeastSignificantBits() & 0xFFFFF).substring(0, 6);
    }
}