package com.staysure.booking.repository;

import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.TenantDocument;
import com.staysure.common.enums.DocumentType;
import com.staysure.common.enums.DocumentVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TenantDocumentRepository extends JpaRepository<TenantDocument, Long> {
    List<TenantDocument> findAllByBookingOrderByCreatedAtDesc(Booking booking);

    Optional<TenantDocument> findByIdAndBooking(Long id, Booking booking);

    @Query("select count(d) > 0 from TenantDocument d where d.booking = :booking and d.verificationStatus = :status and d.documentType in :types")
    boolean existsVerifiedByTypeIn(@Param("booking") Booking booking,
                                   @Param("status") DocumentVerificationStatus status,
                                   @Param("types") Collection<DocumentType> types);
}
