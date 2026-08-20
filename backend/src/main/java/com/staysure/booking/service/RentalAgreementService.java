package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.dto.RentalAgreementResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.enums.AgreementStatus;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.mapper.BookingMapper;
import com.staysure.booking.repository.RentalAgreementRepository;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.owner.service.FileStorageService;
import com.staysure.owner.service.StoredFile;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class RentalAgreementService {

    private final RentalAgreementRepository rentalAgreementRepository;
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final AuditService auditService;

    public RentalAgreementService(RentalAgreementRepository rentalAgreementRepository,
                                  BookingService bookingService,
                                  BookingMapper bookingMapper,
                                  FileStorageService fileStorageService,
                                  UserService userService,
                                  AuditService auditService) {
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
        this.fileStorageService = fileStorageService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public RentalAgreementResponse getForUser(Long userId, Long bookingId) {
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        return bookingMapper.toAgreementResponse(findAgreement(booking));
    }

    @Transactional(readOnly = true)
    public RentalAgreementResponse getForOwner(Long ownerUserId, Long bookingId) {
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        return bookingMapper.toAgreementResponse(findAgreement(booking));
    }

    @Transactional
    public RentalAgreementResponse issue(Long ownerUserId, Long bookingId, LocalDate endDate,
                                         String terms, MultipartFile file, String ipAddress) {
        User actor = userService.getUser(ownerUserId);
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        if (booking.getStatus() != BookingStatus.AWAITING_AGREEMENT) {
            throw new BusinessRuleException("Agreement can be issued only when booking awaits agreement", "INVALID_BOOKING_TRANSITION");
        }
        RentalAgreement agreement = rentalAgreementRepository.findByBooking(booking).orElseGet(RentalAgreement::new);
        if (agreement.getId() != null && agreement.getStatus() == AgreementStatus.ACCEPTED) {
            throw new BusinessRuleException("Accepted agreement cannot be changed", "AGREEMENT_ALREADY_ACCEPTED");
        }
        agreement.setBooking(booking);
        if (agreement.getAgreementNumber() == null) {
            agreement.setAgreementNumber(nextAgreementNumber());
        }
        agreement.setStatus(AgreementStatus.ISSUED);
        agreement.setTerms(blankToNull(terms));
        agreement.setStartDate(booking.getMoveInDate());
        agreement.setEndDate(endDate);
        agreement.setMonthlyRent(booking.getMonthlyRent());
        agreement.setSecurityDeposit(booking.getSecurityDeposit());
        agreement.setNoticePeriodDays(booking.getProperty().getNoticePeriodDays());
        agreement.setLockInMonths(booking.getProperty().getLockInMonths());
        agreement.setIssuedAt(LocalDateTime.now());
        agreement.setAcceptedAt(null);
        agreement.setCreatedBy(actor);
        if (file != null && !file.isEmpty()) {
            StoredFile storedFile = fileStorageService.storeRentalAgreement(booking.getId(), file);
            agreement.setDocumentUrl(storedFile.publicUrl());
            agreement.setOriginalFileName(storedFile.originalFileName());
            agreement.setContentType(storedFile.contentType());
            agreement.setSizeBytes(storedFile.sizeBytes());
        }
        RentalAgreement saved = rentalAgreementRepository.save(agreement);
        auditService.log(actor, "AGREEMENT_CREATED", "BOOKING", "RentalAgreement", saved.getId(),
                "Rental agreement issued", null, saved.getStatus().name(), ipAddress);
        return bookingMapper.toAgreementResponse(saved);
    }

    @Transactional
    public RentalAgreementResponse accept(Long userId, Long bookingId, String ipAddress) {
        User actor = userService.getUser(userId);
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        RentalAgreement agreement = findAgreement(booking);
        if (agreement.getStatus() != AgreementStatus.ISSUED) {
            throw new BusinessRuleException("Agreement cannot be accepted from current status", "INVALID_AGREEMENT_TRANSITION");
        }
        agreement.setStatus(AgreementStatus.ACCEPTED);
        agreement.setAcceptedAt(LocalDateTime.now());
        RentalAgreement saved = rentalAgreementRepository.save(agreement);
        auditService.log(actor, "AGREEMENT_ACCEPTED", "BOOKING", "RentalAgreement", saved.getId(),
                "Rental agreement accepted", AgreementStatus.ISSUED.name(), AgreementStatus.ACCEPTED.name(), ipAddress);
        bookingService.evaluateBookingReadiness(booking, actor, ipAddress);
        return bookingMapper.toAgreementResponse(saved);
    }

    private RentalAgreement findAgreement(Booking booking) {
        return rentalAgreementRepository.findByBooking(booking)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Agreement not found", "AGREEMENT_NOT_FOUND"));
    }

    private String nextAgreementNumber() {
        return "AG-" + LocalDate.now().toString().replace("-", "") + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
