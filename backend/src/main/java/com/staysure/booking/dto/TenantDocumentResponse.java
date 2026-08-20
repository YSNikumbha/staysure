package com.staysure.booking.dto;

import com.staysure.common.enums.DocumentType;
import com.staysure.common.enums.DocumentVerificationStatus;

import java.time.LocalDateTime;

public record TenantDocumentResponse(
        Long id,
        Long bookingId,
        DocumentType documentType,
        String documentNumber,
        String documentUrl,
        String originalFileName,
        String contentType,
        Long sizeBytes,
        DocumentVerificationStatus verificationStatus,
        String rejectionReason,
        Long verifiedBy,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
) {
}
