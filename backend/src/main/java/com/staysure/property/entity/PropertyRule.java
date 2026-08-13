package com.staysure.property.entity;

import com.staysure.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(name = "property_rules")
public class PropertyRule extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    private PgProperty property;

    @Column(name = "visitor_allowed", nullable = false)
    private boolean visitorAllowed;

    @Column(name = "smoking_allowed", nullable = false)
    private boolean smokingAllowed;

    @Column(name = "alcohol_allowed", nullable = false)
    private boolean alcoholAllowed;

    @Column(name = "cooking_allowed", nullable = false)
    private boolean cookingAllowed;

    @Column(name = "gate_closing_time")
    private LocalTime gateClosingTime;

    @Column(name = "late_entry_allowed", nullable = false)
    private boolean lateEntryAllowed;

    @Column(name = "notice_period_days", nullable = false)
    private Integer noticePeriodDays = 0;

    @Column(name = "additional_rules", columnDefinition = "TEXT")
    private String additionalRules;
}
