package com.staysure.rent.repository;

import com.staysure.owner.entity.OwnerProfile;
import com.staysure.rent.entity.RentInvoice;
import com.staysure.rent.entity.RentPayment;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentPaymentRepository extends JpaRepository<RentPayment, Long> {

    boolean existsByPaymentNumber(String paymentNumber);

    long countByPaymentNumberStartingWith(String prefix);

    List<RentPayment> findAllByRentInvoiceOrderByPaymentDateDescCreatedAtDesc(RentInvoice rentInvoice);

    @Query("select p from RentPayment p where p.id = :id and p.property.owner = :owner")
    Optional<RentPayment> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);

    @Query("select p from RentPayment p where p.id = :id and p.tenantProfile.user = :user")
    Optional<RentPayment> findByIdAndTenantUser(@Param("id") Long id, @Param("user") User user);
}
