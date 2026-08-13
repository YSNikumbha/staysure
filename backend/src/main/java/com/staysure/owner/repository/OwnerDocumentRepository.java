package com.staysure.owner.repository;

import com.staysure.owner.entity.OwnerDocument;
import com.staysure.owner.entity.OwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerDocumentRepository extends JpaRepository<OwnerDocument, Long> {
    List<OwnerDocument> findAllByOwnerOrderByCreatedAtDesc(OwnerProfile owner);

    Optional<OwnerDocument> findByIdAndOwner(Long id, OwnerProfile owner);
}
