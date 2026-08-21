package com.staysure.operations.entity;

import com.staysure.common.entity.BaseEntity;
import com.staysure.operations.enums.MealType;
import com.staysure.property.entity.PgProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "food_menus")
public class FoodMenu extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "property_id", nullable = false)
    private PgProperty property;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_type", nullable = false, length = 30)
    private MealType mealType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String items;

    @Column(columnDefinition = "TEXT")
    private String notes;
}
