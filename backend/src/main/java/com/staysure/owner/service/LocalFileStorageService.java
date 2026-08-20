package com.staysure.owner.service;

import com.staysure.common.exception.BusinessRuleException;
import com.staysure.config.UploadProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final UploadProperties uploadProperties;

    public LocalFileStorageService(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public StoredFile storeOwnerDocument(Long ownerId, MultipartFile file) {
        validate(file, uploadProperties.allowedContentTypes(), "Document file is required",
                "DOCUMENT_FILE_REQUIRED", "Document exceeds allowed file size", "DOCUMENT_FILE_TOO_LARGE",
                "Unsupported document file type", "UNSUPPORTED_DOCUMENT_TYPE");
        return store(file, "owner-documents", ownerId);
    }

    @Override
    public StoredFile storePgImage(Long propertyId, MultipartFile file) {
        validate(file, IMAGE_CONTENT_TYPES, "Image file is required", "IMAGE_FILE_REQUIRED",
                "File exceeds allowed size", "FILE_TOO_LARGE", "Invalid image file type", "INVALID_FILE_TYPE");
        return store(file, "pg-images", propertyId);
    }

    @Override
    public StoredFile storeTenantDocument(Long bookingId, MultipartFile file) {
        validate(file, uploadProperties.allowedContentTypes(), "Document file is required",
                "DOCUMENT_FILE_REQUIRED", "Document exceeds allowed file size", "DOCUMENT_FILE_TOO_LARGE",
                "Unsupported document file type", "UNSUPPORTED_DOCUMENT_TYPE");
        return store(file, "tenant-documents", bookingId);
    }

    @Override
    public StoredFile storeRentalAgreement(Long bookingId, MultipartFile file) {
        validate(file, uploadProperties.allowedContentTypes(), "Agreement file is required",
                "AGREEMENT_FILE_REQUIRED", "Agreement exceeds allowed file size", "AGREEMENT_FILE_TOO_LARGE",
                "Unsupported agreement file type", "UNSUPPORTED_AGREEMENT_TYPE");
        return store(file, "rental-agreements", bookingId);
    }

    private StoredFile store(MultipartFile file, String folder, Long ownerId) {
        String originalFileName = cleanFileName(file.getOriginalFilename());
        String extension = extensionFor(originalFileName, file.getContentType());
        String storedFileName = UUID.randomUUID() + extension;
        Path root = rootPath();
        Path directory = root.resolve(folder).resolve(ownerId.toString()).normalize();
        Path target = directory.resolve(storedFileName).normalize();
        if (!target.startsWith(directory)) {
            throw new BusinessRuleException("Invalid upload path", "INVALID_UPLOAD_PATH");
        }
        try {
            Files.createDirectories(directory);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            String publicUrl = "/uploads/" + folder + "/" + ownerId + "/" + storedFileName;
            return new StoredFile(publicUrl, originalFileName, file.getContentType(), file.getSize());
        } catch (IOException ex) {
            throw new BusinessRuleException("Unable to store uploaded file", "UPLOAD_STORAGE_FAILED");
        }
    }

    @Override
    public void deleteByPublicUrl(String publicUrl) {
        if (publicUrl == null || !publicUrl.startsWith("/uploads/")) {
            return;
        }
        Path target = rootPath().resolve(publicUrl.substring("/uploads/".length())).normalize();
        if (!target.startsWith(rootPath())) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // Metadata deletion should not fail just because the local file is already gone.
        }
    }

    private void validate(MultipartFile file, Iterable<String> allowedContentTypes, String requiredMessage,
                          String requiredCode, String sizeMessage, String sizeCode, String typeMessage,
                          String typeCode) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException(requiredMessage, requiredCode);
        }
        if (file.getSize() > uploadProperties.maxFileSizeBytes()) {
            throw new BusinessRuleException(sizeMessage, sizeCode);
        }
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new BusinessRuleException(typeMessage, typeCode);
        }
        boolean allowed = false;
        for (String allowedContentType : allowedContentTypes) {
            if (allowedContentType.equals(contentType)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            throw new BusinessRuleException(typeMessage, typeCode);
        }
    }

    private Path rootPath() {
        return Path.of(uploadProperties.directory()).toAbsolutePath().normalize();
    }

    private String cleanFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "document";
        }
        String filename = Path.of(originalFilename).getFileName().toString();
        return filename.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String extensionFor(String originalFileName, String contentType) {
        int dot = originalFileName.lastIndexOf('.');
        if (dot >= 0 && dot < originalFileName.length() - 1) {
            String extension = originalFileName.substring(dot).toLowerCase(Locale.ROOT);
            if (Set.of(".jpg", ".jpeg", ".png", ".pdf").contains(extension)) {
                return extension;
            }
        }
        return switch (contentType) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "application/pdf" -> ".pdf";
            default -> "";
        };
    }
}
