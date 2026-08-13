package com.staysure.property.repository;

import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.PropertyRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyRuleRepository extends JpaRepository<PropertyRule, Long> {
    Optional<PropertyRule> findByProperty(PgProperty property);
}
