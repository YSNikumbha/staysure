package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.DepositStatus;
import com.staysure.booking.enums.PaymentMethod;
import com.staysure.booking.repository.SecurityDepositRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class SecurityDepositService {

    private final SecurityDepositRepository securityDepositRepository;
    private final BookingService bookingService;
    private final UserService userService;
    private final AuditService auditService;

    public SecurityDepositService(SecurityDepositRepository securityDepositRepository,
                                  BookingService bookingService,
                                  UserService userService,
                                  AuditService auditService) {
        this.securityDepositRepository = securityDepositRepository;
        this.bookingService = bookingService;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional
    public SecurityDeposit recordDeposit(Long ownerId, Long bookingId, BigDecimal amount, PaymentMethod paymentMethod, String paymentReference, String remarks, String ipAddress) {
        User owner = userService.getUser(ownerId);
        Booking booking = bookingService.getOwnerBooking(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.AWAITING_DEPOSIT) {
            throw new BusinessRuleException("Deposit not allowed in current booking status", "DEPOSIT_NOT_ALLOWED");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Invalid deposit amount", "INVALID_DEPOSIT_AMOUNT");
        }

        SecurityDeposit deposit = securityDepositRepository.findByBookingId(bookingId)
                .orElseGet(() -> {
                    SecurityDeposit newDeposit = new SecurityDeposit();
                    newDeposit.setBooking(booking);
                    newDeposit.setProperty(booking.getProperty());
                    newDeposit.setUser(booking.getUser());
                    newDeposit.setRequiredAmount(booking.getSecurityDepositAmount());
                    return newDeposit;
                });

        BigDecimal newPaidAmount = deposit.getPaidAmount().add(amount);
        if (newPaidAmount.compareTo(deposit.getRequiredAmount()) > 0) {
            throw new BusinessRuleException("Deposit amount exceeds required amount", "INVALID_DEPOSIT_AMOUNT");
        }

        deposit.setPaidAmount(newPaidAmount);
        deposit.setPaymentMethod(paymentMethod);
        deposit.setPaymentReference(paymentReference);
        deposit.setPaidAt(LocalDateTime.now());
        deposit.setRemarks(remarks);

        if (newPaidAmount.compareTo(deposit.getRequiredAmount()) >= 0) {
            deposit.setStatus(DepositStatus.PAID);
        } else {
            deposit.setStatus(DepositStatus.PARTIALLY_PAID);
        }

        SecurityDeposit saved = securityDepositRepository.save(deposit);
        auditService.log(owner, "DEPOSIT_RECORDED", "SECURITY_DEPOSIT", "SecurityDeposit", saved.getId(),
                "Security deposit recorded", null, saved.getStatus().name(), ipAddress);

        return saved;
    }

    @Transactional(readOnly = true)
    public SecurityDeposit getDeposit(Long ownerId, Long bookingId) {
        bookingService.getOwnerBooking(ownerId, bookingId);
        return securityDepositRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Deposit not found", "DEPOSIT_NOT_FOUND"));
    }
}