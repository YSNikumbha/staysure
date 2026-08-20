package com.staysure.booking.repository;

import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantProfileRepository extends JpaRepository<TenantProfile, Long> {
    Optional<TenantProfile> findByBooking(Booking booking);

    List<TenantProfile> findAllByUserOrderByCreatedAtDesc(User user);

    @Query("select t from TenantProfile t where t.property.owner = :owner order by t.createdAt desc")
    List<TenantProfile> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select t from TenantProfile t where t.id = :id and t.property.owner = :owner")
    Optional<TenantProfile> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);

    @Query("select t from TenantProfile t where t.property = :property and t.property.owner = :owner and t.status = :status order by t.id asc")
    List<TenantProfile> findAllByPropertyAndOwnerAndStatus(@Param("property") PgProperty property,
                                                           @Param("owner") OwnerProfile owner,
                                                           @Param("status") TenantStatus status);

    Optional<TenantProfile> findFirstByUserAndStatusOrderByCreatedAtDesc(User user, TenantStatus status);
}
