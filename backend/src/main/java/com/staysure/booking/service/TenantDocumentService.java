package com.staysure.booking.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.exception.ApiException;
import com.staysure.common.exception.BusinessRuleException;
import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.TenantDocument;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.booking.enums.DocumentType;
import com.staysure.booking.enums.DocumentVerificationStatus;
import com.staysure.booking.repository.TenantDocumentRepository;
import com.staysure.user.entity.User;
import com.staysure.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TenantDocumentService {

    private final TenantDocumentRepository tenantDocumentRepository;
    private final BookingService bookingService;
    private final UserService userService;
    private final AuditService auditService;

    public TenantDocumentService(TenantDocumentRepository tenantDocumentRepository,
                                 BookingService bookingService,
                                 UserService userService,
                                 AuditService auditService) {
        this.tenantDocumentRepository = tenantDocumentRepository;
        this.bookingService = bookingService;
        this.userService = userService;
        this.auditService = auditService;
    }

    private TenantDocument requireDocument(TenantDocument document, String errorCode) {
        if (document == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Document not found", errorCode);
        }
        return document;
    }

    @Transactional
    public TenantDocument uploadDocument(Long userId, Long bookingId, DocumentType documentType, String documentNumber, String documentUrl, String ipAddress) {
        User user = userService.getUser(userId);
        Booking booking = bookingService.getUserBooking(userId, bookingId);

        if (booking.getStatus() != BookingStatus.AWAITING_KYC && booking.getStatus() != BookingStatus.KYC_VERIFICATION) {
            throw new BusinessRuleException("KYC not allowed in current booking status", "KYC_NOT_ALLOWED");
        }

        TenantDocument document = new TenantDocument();
        document.setBooking(booking);
        document.setUser(user);
        document.setDocumentType(documentType);
        document.setDocumentNumber(documentNumber);
        document.setDocumentUrl(documentUrl);
        document.setVerificationStatus(DocumentVerificationStatus.PENDING);

        TenantDocument saved = tenantDocumentRepository.save(document);
        auditService.log(user, "TENANT_DOCUMENT_UPLOADED", "TENANT_DOCUMENT", "TenantDocument", saved.getId(),
                "Tenant document uploaded", null, documentType.name(), ipAddress);

        return saved;
    }

    @Transactional
    public TenantDocument verifyDocument(Long ownerId, Long bookingId, Long documentId, String ipAddress) {
        if (ownerId == null || bookingId == null || documentId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User owner = userService.getUser(ownerId);
        Booking booking = bookingService.getOwnerBooking(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.KYC_VERIFICATION) {
            throw new BusinessRuleException("KYC verification not allowed in current booking status", "KYC_NOT_ALLOWED");
        }

        TenantDocument document = requireDocument(
                tenantDocumentRepository.findById(documentId).orElse(null),
                "DOCUMENT_NOT_FOUND"
        );

        if (!document.getBooking().getId().equals(bookingId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Document not found", "DOCUMENT_NOT_FOUND");
        }

        document.setVerificationStatus(DocumentVerificationStatus.VERIFIED);
        document.setVerifiedBy(owner);
        document.setVerifiedAt(LocalDateTime.now());
        document.setRejectionReason(null);

        TenantDocument saved = tenantDocumentRepository.save(document);
        auditService.log(owner, "TENANT_DOCUMENT_VERIFIED", "TENANT_DOCUMENT", "TenantDocument", saved.getId(),
                "Tenant document verified", null, document.getDocumentType().name(), ipAddress);

        return saved;
    }

    @Transactional
    public TenantDocument rejectDocument(Long ownerId, Long bookingId, Long documentId, String reason, String ipAddress) {
        if (ownerId == null || bookingId == null || documentId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        User owner = userService.getUser(ownerId);
        Booking booking = bookingService.getOwnerBooking(ownerId, bookingId);

        if (booking.getStatus() != BookingStatus.KYC_VERIFICATION) {
            throw new BusinessRuleException("KYC verification not allowed in current booking status", "KYC_NOT_ALLOWED");
        }

        TenantDocument document = requireDocument(
                tenantDocumentRepository.findById(documentId).orElse(null),
                "DOCUMENT_NOT_FOUND"
        );

        if (!document.getBooking().getId().equals(bookingId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Document not found", "DOCUMENT_NOT_FOUND");
        }

        document.setVerificationStatus(DocumentVerificationStatus.REJECTED);
        document.setVerifiedBy(owner);
        document.setVerifiedAt(LocalDateTime.now());
        document.setRejectionReason(reason);

        TenantDocument saved = tenantDocumentRepository.save(document);
        auditService.log(owner, "TENANT_DOCUMENT_REJECTED", "TENANT_DOCUMENT", "TenantDocument", saved.getId(),
                "Tenant document rejected", null, document.getDocumentType().name(), ipAddress);

        return saved;
    }

    @Transactional(readOnly = true)
    public List<TenantDocument> getBookingDocuments(Long userId, Long bookingId) {
        bookingService.getUserBooking(userId, bookingId);
        return tenantDocumentRepository.findByBookingIdOrderByCreatedAtDesc(bookingId);
    }

    @Transactional
    public void deleteDocument(Long userId, Long bookingId, Long documentId, String ipAddress) {
        if (userId == null || bookingId == null || documentId == null) {
            throw new BusinessRuleException("Invalid request parameters", "INVALID_REQUEST");
        }
        bookingService.getUserBooking(userId, bookingId);
        TenantDocument document = requireDocument(
                tenantDocumentRepository.findById(documentId).orElse(null),
                "DOCUMENT_NOT_FOUND"
        );

        if (!document.getBooking().getId().equals(bookingId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Document not found", "DOCUMENT_NOT_FOUND");
        }

        tenantDocumentRepository.delete(document);
        auditService.log(userService.getUser(userId), "TENANT_DOCUMENT_DELETED", "TENANT_DOCUMENT", "TenantDocument", documentId,
                "Tenant document deleted", null, null, ipAddress);
    }
}
