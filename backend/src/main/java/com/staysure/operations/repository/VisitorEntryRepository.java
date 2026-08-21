package com.staysure.operations.repository;

import com.staysure.operations.entity.VisitorEntry;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VisitorEntryRepository extends JpaRepository<VisitorEntry, Long> {

    boolean existsByVisitorNumber(String visitorNumber);

    long countByVisitorNumberStartingWith(String prefix);

    @Query("select v from VisitorEntry v where v.tenantProfile.user = :user order by v.visitDate desc, v.createdAt desc")
    List<VisitorEntry> findAllByTenantUser(@Param("user") User user);

    @Query("select v from VisitorEntry v where v.id = :id and v.tenantProfile.user = :user")
    Optional<VisitorEntry> findByIdAndTenantUser(@Param("id") Long id, @Param("user") User user);

    @Query("select v from VisitorEntry v where v.property.owner = :owner order by v.visitDate desc, v.createdAt desc")
    List<VisitorEntry> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select v from VisitorEntry v where v.id = :id and v.property.owner = :owner")
    Optional<VisitorEntry> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);
}
