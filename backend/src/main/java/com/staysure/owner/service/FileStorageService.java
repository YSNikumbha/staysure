package com.staysure.owner.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    StoredFile storeOwnerDocument(Long ownerId, MultipartFile file);

    StoredFile storePgImage(Long propertyId, MultipartFile file);

    StoredFile storeTenantDocument(Long bookingId, MultipartFile file);

    StoredFile storeRentalAgreement(Long bookingId, MultipartFile file);

    void deleteByPublicUrl(String publicUrl);
}
