package com.staysure.owner.repository;

import com.staysure.common.enums.OwnerVerificationStatus;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OwnerProfileRepository extends JpaRepository<OwnerProfile, Long> {
    Optional<OwnerProfile> findByUser(User user);

    boolean existsByUser(User user);

    List<OwnerProfile> findAllByVerificationStatus(OwnerVerificationStatus status);

    List<OwnerProfile> findAllByVerificationStatusIn(List<OwnerVerificationStatus> statuses);
}
