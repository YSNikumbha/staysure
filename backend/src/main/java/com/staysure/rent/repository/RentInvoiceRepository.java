package com.staysure.rent.repository;

import com.staysure.booking.entity.TenantProfile;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import com.staysure.rent.entity.RentInvoice;
import com.staysure.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RentInvoiceRepository extends JpaRepository<RentInvoice, Long> {

    boolean existsByInvoiceNumber(String invoiceNumber);

    long countByBillingYearAndBillingMonth(Integer billingYear, Integer billingMonth);

    Optional<RentInvoice> findByTenantProfileAndBillingMonthAndBillingYear(TenantProfile tenantProfile,
                                                                           Integer billingMonth,
                                                                           Integer billingYear);

    @Query("select i from RentInvoice i where i.property.owner = :owner order by i.dueDate desc, i.id desc")
    List<RentInvoice> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select i from RentInvoice i where i.property = :property and i.property.owner = :owner order by i.dueDate desc, i.id desc")
    List<RentInvoice> findAllByPropertyAndOwner(@Param("property") PgProperty property, @Param("owner") OwnerProfile owner);

    @Query("select i from RentInvoice i where i.id = :id and i.property.owner = :owner")
    Optional<RentInvoice> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from RentInvoice i where i.id = :id and i.property.owner = :owner")
    Optional<RentInvoice> findLockedByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);

    @Query("select i from RentInvoice i where i.tenantProfile.user = :user order by i.dueDate desc, i.id desc")
    List<RentInvoice> findAllByTenantUser(@Param("user") User user);

    @Query("select i from RentInvoice i where i.id = :id and i.tenantProfile.user = :user")
    Optional<RentInvoice> findByIdAndTenantUser(@Param("id") Long id, @Param("user") User user);
}
