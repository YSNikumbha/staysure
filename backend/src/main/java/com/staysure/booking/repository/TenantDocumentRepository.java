package com.staysure.booking.repository;

import com.staysure.booking.entity.TenantDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TenantDocumentRepository extends JpaRepository<TenantDocument, Long> {
    List<TenantDocument> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
}