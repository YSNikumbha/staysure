package com.staysure.property.dto.discovery;

import com.staysure.property.enums.BedStatus;

public record PublicAvailableBedResponse(
        Long id,
        String bedNumber,
        String bedLabel,
        BedStatus status
) {
}
