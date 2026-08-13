package com.staysure.owner.mapper;

import com.staysure.owner.dto.OwnerDocumentResponse;
import com.staysure.owner.dto.OwnerProfileResponse;
import com.staysure.owner.entity.OwnerDocument;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.user.mapper.UserMapper;
import org.springframework.stereotype.Component;

@Component
public class OwnerMapper {

    private final UserMapper userMapper;

    public OwnerMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public OwnerProfileResponse toResponse(OwnerProfile owner) {
        Long verifiedBy = owner.getVerifiedBy() == null ? null : owner.getVerifiedBy().getId();
        return new OwnerProfileResponse(
                owner.getId(),
                userMapper.toResponse(owner.getUser()),
                owner.getBusinessName(),
                owner.getAlternatePhone(),
                owner.getBusinessEmail(),
                owner.getExperienceYears(),
                owner.getDescription(),
                owner.getVerificationStatus(),
                owner.getVerificationRemarks(),
                owner.getVerifiedAt(),
                verifiedBy,
                owner.getCreatedAt(),
                owner.getUpdatedAt()
        );
    }

    public OwnerDocumentResponse toDocumentResponse(OwnerDocument document) {
        return new OwnerDocumentResponse(
                document.getId(),
                document.getDocumentType(),
                document.getDocumentNumber(),
                document.getDocumentUrl(),
                document.getOriginalFileName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getVerificationStatus(),
                document.getRejectionReason(),
                document.getCreatedAt()
        );
    }
}
