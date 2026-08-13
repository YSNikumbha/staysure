package com.staysure.property.repository;

import com.staysure.property.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    List<Amenity> findAllByActiveTrueOrderByNameAsc();

    List<Amenity> findAllByIdInAndActiveTrue(Collection<Long> ids);

    @Query("select p.id, a from PgProperty p join p.amenities a where p.id in :propertyIds order by a.name asc")
    List<Object[]> findAmenitiesByPropertyIds(@Param("propertyIds") Collection<Long> propertyIds);
}
