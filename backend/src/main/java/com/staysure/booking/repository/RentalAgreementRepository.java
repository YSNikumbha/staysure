package com.staysure.booking.repository;

import com.staysure.booking.entity.RentalAgreement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RentalAgreementRepository extends JpaRepository<RentalAgreement, Long> {
    boolean existsByAgreementNumber(String agreementNumber);

    boolean existsByBookingId(Long bookingId);

    Optional<RentalAgreement> findByBookingId(Long bookingId);
}