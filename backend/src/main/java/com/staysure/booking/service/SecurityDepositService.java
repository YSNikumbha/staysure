package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.dto.RecordDepositRequest;
import com.staysure.booking.dto.SecurityDepositResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.DepositStatus;
import com.staysure.booking.mapper.BookingMapper;
import com.staysure.booking.repository.SecurityDepositRepository;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SecurityDepositService {

    private final SecurityDepositRepository securityDepositRepository;
    private final BookingService bookingService;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final AuditService auditService;

    public SecurityDepositService(SecurityDepositRepository securityDepositRepository,
                                  BookingService bookingService,
                                  BookingMapper bookingMapper,
                                  UserService userService,
                                  AuditService auditService) {
        this.securityDepositRepository = securityDepositRepository;
        this.bookingService = bookingService;
        this.bookingMapper = bookingMapper;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public SecurityDepositResponse getForUser(Long userId, Long bookingId) {
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        return bookingMapper.toDepositResponse(securityDepositRepository.findByBooking(booking).orElse(null));
    }

    @Transactional(readOnly = true)
    public SecurityDepositResponse getForOwner(Long ownerUserId, Long bookingId) {
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        return bookingMapper.toDepositResponse(securityDepositRepository.findByBooking(booking).orElse(null));
    }

    @Transactional
    public SecurityDepositResponse record(Long ownerUserId, Long bookingId, RecordDepositRequest request, String ipAddress) {
        User actor = userService.getUser(ownerUserId);
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        if (booking.getStatus() != BookingStatus.AWAITING_DEPOSIT) {
            throw new BusinessRuleException("Deposit can be recorded only when booking awaits deposit", "INVALID_BOOKING_TRANSITION");
        }
        SecurityDeposit deposit = bookingService.ensureDeposit(booking);
        if (deposit.getStatus() == DepositStatus.PAID) {
            throw new BusinessRuleException("Security deposit is already paid", "DEPOSIT_ALREADY_PAID");
        }
        BigDecimal nextPaidAmount = deposit.getPaidAmount().add(request.amount());
        deposit.setPaidAmount(nextPaidAmount);
        deposit.setLastPaymentMethod(request.paymentMethod());
        deposit.setLastPaymentReference(blankToNull(request.paymentReference()));
        deposit.setRemarks(blankToNull(request.remarks()));
        if (nextPaidAmount.compareTo(deposit.getRequiredAmount()) >= 0) {
            deposit.setStatus(DepositStatus.PAID);
            deposit.setPaidAt(LocalDateTime.now());
        } else {
            deposit.setStatus(DepositStatus.PARTIALLY_PAID);
        }
        SecurityDeposit saved = securityDepositRepository.save(deposit);
        auditService.log(actor, "DEPOSIT_RECORDED", "BOOKING", "SecurityDeposit", saved.getId(),
                "Security deposit payment recorded", null, saved.getStatus().name(), ipAddress);
        bookingService.evaluateBookingReadiness(booking, actor, ipAddress);
        return bookingMapper.toDepositResponse(saved);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
