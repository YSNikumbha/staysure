package com.staysure.owner.service;

import com.staysure.audit.service.AuditService;
import com.staysure.common.enums.DocumentType;
import com.staysure.common.enums.DocumentVerificationStatus;
import com.staysure.common.exception.ResourceNotFoundException;
import com.staysure.owner.dto.OwnerDocumentResponse;
import com.staysure.owner.entity.OwnerDocument;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.owner.mapper.OwnerMapper;
import com.staysure.owner.repository.OwnerDocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class OwnerDocumentService {

    private final OwnerService ownerService;
    private final OwnerDocumentRepository ownerDocumentRepository;
    private final FileStorageService fileStorageService;
    private final OwnerMapper ownerMapper;
    private final AuditService auditService;

    public OwnerDocumentService(OwnerService ownerService,
                                OwnerDocumentRepository ownerDocumentRepository,
                                FileStorageService fileStorageService,
                                OwnerMapper ownerMapper,
                                AuditService auditService) {
        this.ownerService = ownerService;
        this.ownerDocumentRepository = ownerDocumentRepository;
        this.fileStorageService = fileStorageService;
        this.ownerMapper = ownerMapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<OwnerDocumentResponse> listMine(Long userId) {
        OwnerProfile owner = ownerService.getCurrentOwner(userId);
        return ownerDocumentRepository.findAllByOwnerOrderByCreatedAtDesc(owner).stream()
                .map(ownerMapper::toDocumentResponse)
                .toList();
    }

    @Transactional
    public OwnerDocumentResponse upload(Long userId, DocumentType documentType, String documentNumber,
                                        MultipartFile file, String ipAddress) {
        OwnerProfile owner = ownerService.getCurrentOwner(userId);
        StoredFile storedFile = fileStorageService.storeOwnerDocument(owner.getId(), file);
        OwnerDocument document = new OwnerDocument();
        document.setOwner(owner);
        document.setDocumentType(documentType);
        document.setDocumentNumber(blankToNull(documentNumber));
        document.setDocumentUrl(storedFile.publicUrl());
        document.setOriginalFileName(storedFile.originalFileName());
        document.setContentType(storedFile.contentType());
        document.setSizeBytes(storedFile.sizeBytes());
        document.setVerificationStatus(DocumentVerificationStatus.PENDING);
        OwnerDocument saved = ownerDocumentRepository.save(document);
        auditService.log(owner.getUser(), "OWNER_DOCUMENT_UPLOADED", "OWNER", "OwnerDocument", saved.getId(),
                "Owner document metadata uploaded", null, documentType.name(), ipAddress);
        return ownerMapper.toDocumentResponse(saved);
    }

    @Transactional
    public void delete(Long userId, Long documentId) {
        OwnerProfile owner = ownerService.getCurrentOwner(userId);
        OwnerDocument document = ownerDocumentRepository.findByIdAndOwner(documentId, owner)
                .orElseThrow(() -> new ResourceNotFoundException("Owner document not found"));
        ownerDocumentRepository.delete(Objects.requireNonNull(document, "owner document must not be null"));
        fileStorageService.deleteByPublicUrl(document.getDocumentUrl());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
