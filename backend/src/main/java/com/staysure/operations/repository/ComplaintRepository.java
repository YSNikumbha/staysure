package com.staysure.operations.repository;

import com.staysure.operations.entity.Complaint;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    boolean existsByComplaintNumber(String complaintNumber);

    long countByComplaintNumberStartingWith(String prefix);

    @Query("select c from Complaint c where c.tenantProfile.user = :user order by c.createdAt desc")
    List<Complaint> findAllByTenantUser(@Param("user") User user);

    @Query("select c from Complaint c where c.id = :id and c.tenantProfile.user = :user")
    Optional<Complaint> findByIdAndTenantUser(@Param("id") Long id, @Param("user") User user);

    @Query("select c from Complaint c where c.property.owner = :owner order by c.createdAt desc")
    List<Complaint> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select c from Complaint c where c.id = :id and c.property.owner = :owner")
    Optional<Complaint> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);
}
