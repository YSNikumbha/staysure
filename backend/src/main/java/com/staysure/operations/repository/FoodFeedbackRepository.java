package com.staysure.operations.repository;

import com.staysure.booking.entity.TenantProfile;
import com.staysure.operations.entity.FoodFeedback;
import com.staysure.operations.enums.MealType;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FoodFeedbackRepository extends JpaRepository<FoodFeedback, Long> {

    Optional<FoodFeedback> findByTenantProfileAndMenuDateAndMealType(TenantProfile tenantProfile, LocalDate menuDate, MealType mealType);

    @Query("select f from FoodFeedback f where f.property = :property and f.property.owner = :owner order by f.menuDate desc, f.createdAt desc")
    List<FoodFeedback> findAllByPropertyAndOwner(@Param("property") PgProperty property, @Param("owner") OwnerProfile owner);

    @Query("select f from FoodFeedback f where f.property.owner = :owner order by f.menuDate desc, f.createdAt desc")
    List<FoodFeedback> findAllByOwner(@Param("owner") OwnerProfile owner);
}
