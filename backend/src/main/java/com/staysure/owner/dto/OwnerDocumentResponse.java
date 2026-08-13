package com.staysure.owner.dto;

import com.staysure.common.enums.DocumentType;
import com.staysure.common.enums.DocumentVerificationStatus;

import java.time.LocalDateTime;

public record OwnerDocumentResponse(
        Long id,
        DocumentType documentType,
        String documentNumber,
        String documentUrl,
        String originalFileName,
        String contentType,
        Long sizeBytes,
        DocumentVerificationStatus verificationStatus,
        String rejectionReason,
        LocalDateTime createdAt
) {
}
