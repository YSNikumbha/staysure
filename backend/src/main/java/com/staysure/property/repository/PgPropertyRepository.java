package com.staysure.property.repository;

import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.PropertyVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface PgPropertyRepository extends JpaRepository<PgProperty, Long>, JpaSpecificationExecutor<PgProperty> {
    boolean existsBySlug(String slug);

    Optional<PgProperty> findBySlugAndStatusAndVerificationStatus(String slug, PropertyStatus status, PropertyVerificationStatus verificationStatus);

    List<PgProperty> findAllByOwnerAndStatusNotOrderByCreatedAtDesc(OwnerProfile owner, PropertyStatus status);

    List<PgProperty> findAllByVerificationStatusOrderBySubmittedForVerificationAtDesc(PropertyVerificationStatus status);

    List<PgProperty> findAllByVerificationStatusInOrderBySubmittedForVerificationAtDesc(List<PropertyVerificationStatus> statuses);

    List<PgProperty> findAllByOrderByCreatedAtDesc();

    long countByOwnerAndStatusNot(OwnerProfile owner, PropertyStatus status);

    long countByOwnerAndStatus(OwnerProfile owner, PropertyStatus status);
}
