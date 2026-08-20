package com.staysure.booking.repository;

import com.staysure.booking.entity.SecurityDeposit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityDepositRepository extends JpaRepository<SecurityDeposit, Long> {
    Optional<SecurityDeposit> findByBookingId(Long bookingId);
}