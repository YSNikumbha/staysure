package com.staysure.property.dto.admin;

import com.staysure.owner.dto.OwnerProfileResponse;
import com.staysure.property.dto.PropertyDetailsResponse;
import com.staysure.property.dto.verification.VerificationHistoryResponse;

import java.util.List;

public record AdminPropertyDetailsResponse(
        OwnerProfileResponse owner,
        PropertyDetailsResponse propertyDetails,
        List<VerificationHistoryResponse> verificationHistory
) {
}
