package com.staysure.property.repository;

import com.staysure.property.entity.PgImage;
import com.staysure.property.entity.PgProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PgImageRepository extends JpaRepository<PgImage, Long> {
    List<PgImage> findAllByPropertyOrderBySortOrderAscCreatedAtAsc(PgProperty property);

    List<PgImage> findAllByPropertyAndCoverImageTrue(PgProperty property);

    @Query("select i from PgImage i where i.property.id in :propertyIds order by i.property.id asc, i.coverImage desc, i.sortOrder asc, i.createdAt asc")
    List<PgImage> findAllByPropertyIdsForCards(@Param("propertyIds") Collection<Long> propertyIds);
}
