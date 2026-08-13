package com.staysure.property.repository;

import com.staysure.property.entity.Floor;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.enums.FloorStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FloorRepository extends JpaRepository<Floor, Long> {
    List<Floor> findAllByPropertyAndStatusNotOrderByFloorNumberAsc(PgProperty property, FloorStatus status);

    boolean existsByPropertyAndFloorNumber(PgProperty property, Integer floorNumber);

    long countByPropertyAndStatusNot(PgProperty property, FloorStatus status);
}
