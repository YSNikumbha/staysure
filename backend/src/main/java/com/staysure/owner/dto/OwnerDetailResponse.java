package com.staysure.owner.dto;

import java.util.List;

public record OwnerDetailResponse(
        OwnerProfileResponse profile,
        List<OwnerDocumentResponse> documents
) {
}
