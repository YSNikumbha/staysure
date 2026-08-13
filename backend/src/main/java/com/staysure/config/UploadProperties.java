package com.staysure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "app.upload")
public record UploadProperties(
        String directory,
        long maxFileSizeBytes,
        List<String> allowedContentTypes
) {
}
