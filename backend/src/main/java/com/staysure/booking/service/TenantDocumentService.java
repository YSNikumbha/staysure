package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.booking.dto.TenantDocumentResponse;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.TenantDocument;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.mapper.BookingMapper;
import com.staysure.booking.repository.TenantDocumentRepository;
import com.staysure.common.enums.DocumentType;
import com.staysure.common.enums.DocumentVerificationStatus;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.owner.service.FileStorageService;
import com.staysure.owner.service.StoredFile;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class TenantDocumentService {

    private static final Set<BookingStatus> USER_UPLOAD_STATUSES = Set.of(
            BookingStatus.AWAITING_KYC,
            BookingStatus.KYC_VERIFICATION
    );

    private final TenantDocumentRepository tenantDocumentRepository;
    private final BookingService bookingService;
    private final FileStorageService fileStorageService;
    private final BookingMapper bookingMapper;
    private final UserService userService;
    private final AuditService auditService;

    public TenantDocumentService(TenantDocumentRepository tenantDocumentRepository,
                                 BookingService bookingService,
                                 FileStorageService fileStorageService,
                                 BookingMapper bookingMapper,
                                 UserService userService,
                                 AuditService auditService) {
        this.tenantDocumentRepository = tenantDocumentRepository;
        this.bookingService = bookingService;
        this.fileStorageService = fileStorageService;
        this.bookingMapper = bookingMapper;
        this.userService = userService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<TenantDocumentResponse> listForUser(Long userId, Long bookingId) {
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        return tenantDocumentRepository.findAllByBookingOrderByCreatedAtDesc(booking).stream()
                .map(bookingMapper::toDocumentResponse)
                .toList();
    }

    @Transactional
    public TenantDocumentResponse upload(Long userId, Long bookingId, DocumentType documentType,
                                         String documentNumber, MultipartFile file, String ipAddress) {
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        if (!USER_UPLOAD_STATUSES.contains(booking.getStatus())) {
            throw new BusinessRuleException("KYC documents cannot be uploaded for current booking status", "INVALID_BOOKING_TRANSITION");
        }
        StoredFile storedFile = fileStorageService.storeTenantDocument(booking.getId(), file);
        TenantDocument document = new TenantDocument();
        document.setBooking(booking);
        document.setUser(booking.getUser());
        document.setDocumentType(documentType);
        document.setDocumentNumber(blankToNull(documentNumber));
        document.setDocumentUrl(storedFile.publicUrl());
        document.setOriginalFileName(storedFile.originalFileName());
        document.setContentType(storedFile.contentType());
        document.setSizeBytes(storedFile.sizeBytes());
        document.setVerificationStatus(DocumentVerificationStatus.PENDING);
        TenantDocument saved = tenantDocumentRepository.save(document);
        bookingService.markKycInVerification(booking, booking.getUser(), ipAddress);
        auditService.log(booking.getUser(), "TENANT_DOCUMENT_UPLOADED", "BOOKING", "TenantDocument", saved.getId(),
                "Tenant KYC document uploaded", null, documentType.name(), ipAddress);
        return bookingMapper.toDocumentResponse(saved);
    }

    @Transactional
    public void delete(Long userId, Long bookingId, Long documentId) {
        Booking booking = bookingService.getUserBooking(userId, bookingId);
        TenantDocument document = tenantDocumentRepository.findByIdAndBooking(documentId, booking)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));
        if (document.getVerificationStatus() == DocumentVerificationStatus.VERIFIED) {
            throw new BusinessRuleException("Verified documents cannot be deleted", "DOCUMENT_DELETE_BLOCKED");
        }
        tenantDocumentRepository.delete(document);
        fileStorageService.deleteByPublicUrl(document.getDocumentUrl());
    }

    @Transactional(readOnly = true)
    public List<TenantDocumentResponse> listForOwner(Long ownerUserId, Long bookingId) {
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        return tenantDocumentRepository.findAllByBookingOrderByCreatedAtDesc(booking).stream()
                .map(bookingMapper::toDocumentResponse)
                .toList();
    }

    @Transactional
    public TenantDocumentResponse verify(Long ownerUserId, Long bookingId, Long documentId, String ipAddress) {
        User actor = userService.getUser(ownerUserId);
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        TenantDocument document = tenantDocumentRepository.findByIdAndBooking(documentId, booking)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));
        document.setVerificationStatus(DocumentVerificationStatus.VERIFIED);
        document.setRejectionReason(null);
        document.setVerifiedBy(actor);
        document.setVerifiedAt(LocalDateTime.now());
        TenantDocument saved = tenantDocumentRepository.save(document);
        auditService.log(actor, "TENANT_DOCUMENT_VERIFIED", "BOOKING", "TenantDocument", saved.getId(),
                "Tenant KYC document verified", null, document.getDocumentType().name(), ipAddress);
        bookingService.evaluateBookingReadiness(booking, actor, ipAddress);
        return bookingMapper.toDocumentResponse(saved);
    }

    @Transactional
    public TenantDocumentResponse reject(Long ownerUserId, Long bookingId, Long documentId, String reason, String ipAddress) {
        User actor = userService.getUser(ownerUserId);
        Booking booking = bookingService.getOwnerBooking(ownerUserId, bookingId);
        TenantDocument document = tenantDocumentRepository.findByIdAndBooking(documentId, booking)
                .orElseThrow(() -> new ResourceNotFoundException("KYC document not found"));
        String cleanedReason = requireRemarks(reason);
        document.setVerificationStatus(DocumentVerificationStatus.REJECTED);
        document.setRejectionReason(cleanedReason);
        document.setVerifiedBy(actor);
        document.setVerifiedAt(LocalDateTime.now());
        TenantDocument saved = tenantDocumentRepository.save(document);
        auditService.log(actor, "TENANT_DOCUMENT_REJECTED", "BOOKING", "TenantDocument", saved.getId(),
                "Tenant KYC document rejected", null, cleanedReason, ipAddress);
        return bookingMapper.toDocumentResponse(saved);
    }

    private String requireRemarks(String value) {
        String cleaned = blankToNull(value);
        if (cleaned == null) {
            throw new BusinessRuleException("Remarks are required", "REMARKS_REQUIRED");
        }
        return cleaned;
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
