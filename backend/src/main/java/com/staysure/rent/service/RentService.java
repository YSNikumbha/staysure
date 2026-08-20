package com.staysure.rent.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.entity.RentalAgreement;
import com.staysure.booking.entity.SecurityDeposit;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.AgreementStatus;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.booking.repository.RentalAgreementRepository;
import com.staysure.booking.repository.SecurityDepositRepository;
import com.staysure.booking.repository.TenantProfileRepository;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.service.OwnerService;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.repository.PgPropertyRepository;
import com.staysure.rent.dto.GenerateRentRequest;
import com.staysure.rent.dto.GenerateRentResponse;
import com.staysure.rent.dto.RecordRentPaymentRequest;
import com.staysure.rent.dto.RentDashboardResponse;
import com.staysure.rent.dto.RentInvoiceDetailResponse;
import com.staysure.rent.dto.RentPaymentResponse;
import com.staysure.rent.dto.UpdateRentChargesRequest;
import com.staysure.rent.entity.RentInvoice;
import com.staysure.rent.entity.RentPayment;
import com.staysure.rent.enums.RentInvoiceStatus;
import com.staysure.rent.mapper.RentMapper;
import com.staysure.rent.repository.RentInvoiceRepository;
import com.staysure.rent.repository.RentPaymentRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class RentService {

    private static final BigDecimal ZERO = BigDecimal.ZERO;

    private final RentInvoiceRepository rentInvoiceRepository;
    private final RentPaymentRepository rentPaymentRepository;
    private final TenantProfileRepository tenantProfileRepository;
    private final RentalAgreementRepository rentalAgreementRepository;
    private final SecurityDepositRepository securityDepositRepository;
    private final PgPropertyRepository propertyRepository;
    private final OwnerService ownerService;
    private final UserService userService;
    private final AuditService auditService;
    private final RentMapper rentMapper;
    private final int defaultDueDay;

    public RentService(RentInvoiceRepository rentInvoiceRepository,
                       RentPaymentRepository rentPaymentRepository,
                       TenantProfileRepository tenantProfileRepository,
                       RentalAgreementRepository rentalAgreementRepository,
                       SecurityDepositRepository securityDepositRepository,
                       PgPropertyRepository propertyRepository,
                       OwnerService ownerService,
                       UserService userService,
                       AuditService auditService,
                       RentMapper rentMapper,
                       @Value("${app.rent.default-due-day:5}") int defaultDueDay) {
        this.rentInvoiceRepository = rentInvoiceRepository;
        this.rentPaymentRepository = rentPaymentRepository;
        this.tenantProfileRepository = tenantProfileRepository;
        this.rentalAgreementRepository = rentalAgreementRepository;
        this.securityDepositRepository = securityDepositRepository;
        this.propertyRepository = propertyRepository;
        this.ownerService = ownerService;
        this.userService = userService;
        this.auditService = auditService;
        this.rentMapper = rentMapper;
        this.defaultDueDay = Math.max(1, Math.min(28, defaultDueDay));
    }

    @Transactional(readOnly = true)
    public RentDashboardResponse listForOwner(Long ownerUserId, Long propertyId) {
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        List<RentInvoice> invoices = propertyId == null
                ? rentInvoiceRepository.findAllByOwner(owner)
                : rentInvoiceRepository.findAllByPropertyAndOwner(getOwnedProperty(propertyId, owner), owner);
        return rentMapper.toDashboard(invoices, null);
    }

    @Transactional(readOnly = true)
    public RentInvoiceDetailResponse getForOwner(Long ownerUserId, Long invoiceId) {
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        RentInvoice invoice = ownerInvoice(owner, invoiceId);
        return rentMapper.toDetail(invoice, payments(invoice), deposit(invoice));
    }

    @Transactional(readOnly = true)
    public List<RentPaymentResponse> paymentsForOwner(Long ownerUserId, Long invoiceId) {
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        return payments(ownerInvoice(owner, invoiceId)).stream().map(rentMapper::toPayment).toList();
    }

    @Transactional(readOnly = true)
    public RentDashboardResponse listForUser(Long userId) {
        User user = userService.getUser(userId);
        List<RentInvoice> invoices = rentInvoiceRepository.findAllByTenantUser(user);
        SecurityDeposit deposit = tenantProfileRepository.findFirstByUserAndStatusOrderByCreatedAtDesc(user, TenantStatus.ACTIVE)
                .flatMap(tenant -> securityDepositRepository.findByBooking(tenant.getBooking()))
                .orElse(null);
        return rentMapper.toDashboard(invoices, deposit);
    }

    @Transactional(readOnly = true)
    public RentInvoiceDetailResponse getForUser(Long userId, Long invoiceId) {
        User user = userService.getUser(userId);
        RentInvoice invoice = userInvoice(user, invoiceId);
        return rentMapper.toDetail(invoice, payments(invoice), deposit(invoice));
    }

    @Transactional(readOnly = true)
    public List<RentPaymentResponse> paymentsForUser(Long userId, Long invoiceId) {
        User user = userService.getUser(userId);
        return payments(userInvoice(user, invoiceId)).stream().map(rentMapper::toPayment).toList();
    }

    @Transactional
    public GenerateRentResponse generate(Long ownerUserId, GenerateRentRequest request, String ipAddress) {
        validateBillingPeriod(request.billingMonth(), request.billingYear());
        User actor = userService.getUser(ownerUserId);
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        PgProperty property = getOwnedProperty(request.propertyId(), owner);
        List<TenantProfile> tenants = tenantProfileRepository.findAllByPropertyAndOwnerAndStatus(property, owner, TenantStatus.ACTIVE);

        int generated = 0;
        int alreadyGenerated = 0;
        int skipped = 0;
        List<RentInvoice> generatedInvoices = new ArrayList<>();
        long sequence = rentInvoiceRepository.countByBillingYearAndBillingMonth(request.billingYear(), request.billingMonth()) + 1;

        for (TenantProfile tenant : tenants) {
            if (rentInvoiceRepository.findByTenantProfileAndBillingMonthAndBillingYear(
                    tenant, request.billingMonth(), request.billingYear()).isPresent()) {
                alreadyGenerated++;
                continue;
            }
            BigDecimal baseRent = trustedRent(tenant);
            if (baseRent.compareTo(ZERO) <= 0) {
                skipped++;
                continue;
            }
            RentInvoice invoice = new RentInvoice();
            invoice.setInvoiceNumber(nextInvoiceNumber(request.billingYear(), request.billingMonth(), sequence++));
            invoice.setTenantProfile(tenant);
            invoice.setProperty(tenant.getProperty());
            invoice.setRoom(tenant.getRoom());
            invoice.setBed(tenant.getBed());
            invoice.setBillingMonth(request.billingMonth());
            invoice.setBillingYear(request.billingYear());
            invoice.setBaseRent(baseRent);
            invoice.setDueDate(dueDate(request.billingYear(), request.billingMonth()));
            invoice.setGeneratedAt(LocalDateTime.now());
            recalculate(invoice);
            RentInvoice saved = rentInvoiceRepository.save(invoice);
            auditService.log(actor, "RENT_INVOICE_GENERATED", "RENT", "RentInvoice", saved.getId(),
                    "Monthly rent invoice generated", null, saved.getInvoiceNumber(), ipAddress);
            generated++;
            generatedInvoices.add(saved);
        }

        return new GenerateRentResponse(
                property.getId(),
                request.billingMonth(),
                request.billingYear(),
                generated,
                alreadyGenerated,
                skipped,
                generatedInvoices.stream().map(rentMapper::toSummary).toList()
        );
    }

    @Transactional
    public RentInvoiceDetailResponse updateCharges(Long ownerUserId, Long invoiceId, UpdateRentChargesRequest request, String ipAddress) {
        User actor = userService.getUser(ownerUserId);
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        RentInvoice invoice = rentInvoiceRepository.findLockedByIdAndOwner(invoiceId, owner)
                .orElseThrow(() -> rentAccessError(invoiceId));
        if (invoice.getStatus() == RentInvoiceStatus.PAID) {
            throw new BusinessRuleException("Paid invoices cannot be edited", "INVALID_RENT_STATUS");
        }
        if (invoice.getStatus() == RentInvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled invoices cannot be edited", "INVALID_RENT_STATUS");
        }
        invoice.setMaintenanceCharge(nonNegative(request.maintenanceCharge()));
        invoice.setElectricityCharge(nonNegative(request.electricityCharge()));
        invoice.setOtherCharge(nonNegative(request.otherCharge()));
        invoice.setLateFee(nonNegative(request.lateFee()));
        invoice.setNotes(blankToNull(request.notes()));
        BigDecimal newTotal = total(invoice);
        if (newTotal.compareTo(invoice.getPaidAmount()) < 0) {
            throw new BusinessRuleException("Charges cannot reduce total below paid amount", "INVALID_RENT_AMOUNT");
        }
        recalculate(invoice);
        RentInvoice saved = rentInvoiceRepository.save(invoice);
        auditService.log(actor, "RENT_CHARGES_UPDATED", "RENT", "RentInvoice", saved.getId(),
                "Rent invoice charges updated", null, saved.getStatus().name(), ipAddress);
        return rentMapper.toDetail(saved, payments(saved), deposit(saved));
    }

    @Transactional
    public RentInvoiceDetailResponse recordPayment(Long ownerUserId, Long invoiceId, RecordRentPaymentRequest request, String ipAddress) {
        User actor = userService.getUser(ownerUserId);
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        RentInvoice invoice = rentInvoiceRepository.findLockedByIdAndOwner(invoiceId, owner)
                .orElseThrow(() -> rentAccessError(invoiceId));
        recalculate(invoice);
        validatePayment(invoice, request.amount());

        RentPayment payment = new RentPayment();
        payment.setPaymentNumber(nextPaymentNumber());
        payment.setRentInvoice(invoice);
        payment.setTenantProfile(invoice.getTenantProfile());
        payment.setProperty(invoice.getProperty());
        payment.setAmount(request.amount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentReference(blankToNull(request.paymentReference()));
        payment.setPaymentDate(request.paymentDate());
        payment.setRemarks(blankToNull(request.remarks()));
        payment.setRecordedBy(actor);
        RentPayment savedPayment = rentPaymentRepository.save(payment);

        RentInvoiceStatus oldStatus = invoice.getStatus();
        invoice.setPaidAmount(invoice.getPaidAmount().add(request.amount()));
        recalculate(invoice);
        RentInvoice savedInvoice = rentInvoiceRepository.save(invoice);
        auditService.log(actor, "RENT_PAYMENT_RECORDED", "RENT", "RentPayment", savedPayment.getId(),
                "Rent payment recorded", null, savedPayment.getPaymentNumber(), ipAddress);
        if (oldStatus != RentInvoiceStatus.PAID && savedInvoice.getStatus() == RentInvoiceStatus.PAID) {
            auditService.log(actor, "RENT_INVOICE_PAID", "RENT", "RentInvoice", savedInvoice.getId(),
                    "Rent invoice fully paid", oldStatus.name(), RentInvoiceStatus.PAID.name(), ipAddress);
        }
        return rentMapper.toDetail(savedInvoice, payments(savedInvoice), deposit(savedInvoice));
    }

    @Transactional(readOnly = true)
    public String receiptForOwner(Long ownerUserId, Long paymentId) {
        OwnerProfile owner = ownerService.getCurrentOwner(ownerUserId);
        RentPayment payment = rentPaymentRepository.findByIdAndOwner(paymentId, owner)
                .orElseThrow(() -> rentPaymentAccessError(paymentId));
        return receipt(payment);
    }

    @Transactional(readOnly = true)
    public String receiptForUser(Long userId, Long paymentId) {
        User user = userService.getUser(userId);
        RentPayment payment = rentPaymentRepository.findByIdAndTenantUser(paymentId, user)
                .orElseThrow(() -> rentPaymentAccessError(paymentId));
        return receipt(payment);
    }

    private List<RentPayment> payments(RentInvoice invoice) {
        return rentPaymentRepository.findAllByRentInvoiceOrderByPaymentDateDescCreatedAtDesc(invoice);
    }

    private SecurityDeposit deposit(RentInvoice invoice) {
        return securityDepositRepository.findByBooking(invoice.getTenantProfile().getBooking()).orElse(null);
    }

    private PgProperty getOwnedProperty(Long propertyId, OwnerProfile owner) {
        return propertyRepository.findByIdAndOwner(propertyId, owner)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "Rent access denied", "RENT_ACCESS_DENIED"));
    }

    private RentInvoice ownerInvoice(OwnerProfile owner, Long invoiceId) {
        return rentInvoiceRepository.findByIdAndOwner(invoiceId, owner)
                .orElseThrow(() -> rentAccessError(invoiceId));
    }

    private RentInvoice userInvoice(User user, Long invoiceId) {
        return rentInvoiceRepository.findByIdAndTenantUser(invoiceId, user)
                .orElseThrow(() -> rentAccessError(invoiceId));
    }

    private ApiException rentAccessError(Long invoiceId) {
        if (invoiceId != null && rentInvoiceRepository.existsById(invoiceId)) {
            return new ApiException(HttpStatus.FORBIDDEN, "Rent access denied", "RENT_ACCESS_DENIED");
        }
        return new ApiException(HttpStatus.NOT_FOUND, "Rent invoice not found", "RENT_INVOICE_NOT_FOUND");
    }

    private ApiException rentPaymentAccessError(Long paymentId) {
        if (paymentId != null && rentPaymentRepository.existsById(paymentId)) {
            return new ApiException(HttpStatus.FORBIDDEN, "Rent access denied", "RENT_ACCESS_DENIED");
        }
        return new ApiException(HttpStatus.NOT_FOUND, "Rent payment not found", "PAYMENT_NOT_FOUND");
    }

    private void validateBillingPeriod(Integer month, Integer year) {
        if (month == null || month < 1 || month > 12 || year == null || year < 2000 || year > 2100) {
            throw new BusinessRuleException("Invalid billing period", "INVALID_BILLING_PERIOD");
        }
    }

    private BigDecimal trustedRent(TenantProfile tenant) {
        return rentalAgreementRepository.findByBooking(tenant.getBooking())
                .filter(agreement -> agreement.getStatus() == AgreementStatus.ACCEPTED)
                .map(RentalAgreement::getMonthlyRent)
                .filter(amount -> amount != null && amount.compareTo(ZERO) > 0)
                .orElseGet(() -> {
                    BigDecimal bookingRent = tenant.getBooking().getMonthlyRent();
                    if (bookingRent != null && bookingRent.compareTo(ZERO) > 0) {
                        return bookingRent;
                    }
                    return tenant.getRoom().getMonthlyRent();
                });
    }

    private void validatePayment(RentInvoice invoice, BigDecimal amount) {
        if (invoice.getStatus() == RentInvoiceStatus.CANCELLED) {
            throw new BusinessRuleException("Payment is not allowed for cancelled invoice", "PAYMENT_NOT_ALLOWED");
        }
        if (invoice.getStatus() == RentInvoiceStatus.PAID || invoice.getBalanceAmount().compareTo(ZERO) <= 0) {
            throw new BusinessRuleException("This invoice has already been fully paid", "RENT_ALREADY_PAID");
        }
        if (amount == null || amount.compareTo(ZERO) <= 0) {
            throw new BusinessRuleException("Payment amount must be greater than zero", "INVALID_PAYMENT_AMOUNT");
        }
        if (amount.compareTo(invoice.getBalanceAmount()) > 0) {
            throw new BusinessRuleException("Payment amount cannot exceed the outstanding balance", "PAYMENT_EXCEEDS_BALANCE");
        }
    }

    private void recalculate(RentInvoice invoice) {
        BigDecimal totalAmount = total(invoice);
        BigDecimal paidAmount = nonNegative(invoice.getPaidAmount());
        BigDecimal balanceAmount = totalAmount.subtract(paidAmount).max(ZERO);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(paidAmount);
        invoice.setBalanceAmount(balanceAmount);
        if (invoice.getStatus() == RentInvoiceStatus.CANCELLED) {
            return;
        }
        invoice.setStatus(statusFor(invoice));
    }

    private RentInvoiceStatus statusFor(RentInvoice invoice) {
        if (invoice.getBalanceAmount().compareTo(ZERO) <= 0) {
            return RentInvoiceStatus.PAID;
        }
        if (invoice.getDueDate() != null && LocalDate.now().isAfter(invoice.getDueDate())) {
            return RentInvoiceStatus.OVERDUE;
        }
        if (invoice.getPaidAmount().compareTo(ZERO) > 0) {
            return RentInvoiceStatus.PARTIALLY_PAID;
        }
        return RentInvoiceStatus.PENDING;
    }

    private BigDecimal total(RentInvoice invoice) {
        return nonNegative(invoice.getBaseRent())
                .add(nonNegative(invoice.getMaintenanceCharge()))
                .add(nonNegative(invoice.getElectricityCharge()))
                .add(nonNegative(invoice.getOtherCharge()))
                .add(nonNegative(invoice.getLateFee()));
    }

    private BigDecimal nonNegative(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        if (value.compareTo(ZERO) < 0) {
            throw new BusinessRuleException("Rent amount cannot be negative", "INVALID_RENT_AMOUNT");
        }
        return value;
    }

    private String nextInvoiceNumber(Integer year, Integer month, long sequence) {
        String invoiceNumber;
        long next = sequence;
        do {
            invoiceNumber = String.format("RENT-%04d-%02d-%06d", year, month, next++);
        } while (rentInvoiceRepository.existsByInvoiceNumber(invoiceNumber));
        return invoiceNumber;
    }

    private String nextPaymentNumber() {
        int year = LocalDate.now().getYear();
        String prefix = "PAY-" + year + "-";
        long sequence = rentPaymentRepository.countByPaymentNumberStartingWith(prefix) + 1;
        String paymentNumber;
        do {
            paymentNumber = prefix + String.format("%06d", sequence++);
        } while (rentPaymentRepository.existsByPaymentNumber(paymentNumber));
        return paymentNumber;
    }

    private LocalDate dueDate(Integer year, Integer month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        int day = Math.min(defaultDueDay, yearMonth.lengthOfMonth());
        return yearMonth.atDay(day);
    }

    private String receipt(RentPayment payment) {
        RentInvoice invoice = payment.getRentInvoice();
        String tenant = invoice.getTenantProfile().getUser().getFirstName() + " " + invoice.getTenantProfile().getUser().getLastName();
        String bed = invoice.getBed().getBedLabel() == null || invoice.getBed().getBedLabel().isBlank()
                ? invoice.getBed().getBedNumber()
                : invoice.getBed().getBedLabel();
        return """
                StaySure Rent Receipt
                Receipt Number: %s
                Tenant: %s
                PG: %s
                Room: %s
                Bed: %s
                Billing Month: %s %d
                Invoice Number: %s
                Payment Amount: Rs %s
                Payment Method: %s
                Payment Reference: %s
                Payment Date: %s
                Remaining Balance: Rs %s
                """.formatted(
                payment.getPaymentNumber(),
                tenant,
                invoice.getProperty().getName(),
                invoice.getRoom().getRoomNumber(),
                bed,
                Month.of(invoice.getBillingMonth()).name(),
                invoice.getBillingYear(),
                invoice.getInvoiceNumber(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentReference() == null ? "-" : payment.getPaymentReference(),
                payment.getPaymentDate(),
                invoice.getBalanceAmount()
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
