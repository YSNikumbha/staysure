package com.staysure.property.repository;

import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.PropertyVerificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PropertyVerificationHistoryRepository extends JpaRepository<PropertyVerificationHistory, Long> {
    List<PropertyVerificationHistory> findAllByPropertyOrderByCreatedAtDesc(PgProperty property);
}
