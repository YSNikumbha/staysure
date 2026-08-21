package com.staysure.operations.repository;

import com.staysure.operations.entity.FoodMenu;
import com.staysure.operations.enums.MealType;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FoodMenuRepository extends JpaRepository<FoodMenu, Long> {

    Optional<FoodMenu> findByPropertyAndMenuDateAndMealType(PgProperty property, LocalDate menuDate, MealType mealType);

    List<FoodMenu> findAllByPropertyAndMenuDateOrderByMealTypeAsc(PgProperty property, LocalDate menuDate);

    @Query("select m from FoodMenu m where m.property.owner = :owner order by m.menuDate desc, m.mealType asc")
    List<FoodMenu> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select m from FoodMenu m where m.id = :id and m.property.owner = :owner")
    Optional<FoodMenu> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);
}
