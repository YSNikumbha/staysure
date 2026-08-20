package com.staysure.booking.repository;

import com.staysure.booking.entity.TenantProfile;
import com.staysure.booking.enums.TenantStatus;
import com.staysure.property.entity.PgProperty;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TenantProfileRepository extends JpaRepository<TenantProfile, Long> {
    boolean existsByBookingId(Long bookingId);

    List<TenantProfile> findByUserOrderByCreatedAtDesc(User user);

    List<TenantProfile> findByPropertyAndStatusOrderByCreatedAtDesc(PgProperty property, TenantStatus status);

    Optional<TenantProfile> findByBookingId(Long bookingId);
}