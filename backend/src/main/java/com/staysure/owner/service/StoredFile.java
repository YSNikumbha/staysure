package com.staysure.owner.service;

public record StoredFile(
        String publicUrl,
        String originalFileName,
        String contentType,
        long sizeBytes
) {
}
