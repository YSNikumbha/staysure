package com.staysure.operations.repository;

import com.staysure.operations.entity.Notice;
import com.staysure.operations.enums.NoticeStatus;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.PgProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("select n from Notice n where n.property.owner = :owner order by n.createdAt desc")
    List<Notice> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select n from Notice n where n.id = :id and n.property.owner = :owner")
    Optional<Notice> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);

    @Query("select n from Notice n where n.property = :property and n.status = :status and (n.expiresAt is null or n.expiresAt >= :today) order by n.publishedAt desc")
    List<Notice> findActiveForProperty(@Param("property") PgProperty property,
                                       @Param("status") NoticeStatus status,
                                       @Param("today") LocalDate today);

    @Query("select n from Notice n where n.id = :id and n.property = :property and n.status = :status and (n.expiresAt is null or n.expiresAt >= :today)")
    Optional<Notice> findActiveByIdForProperty(@Param("id") Long id,
                                               @Param("property") PgProperty property,
                                               @Param("status") NoticeStatus status,
                                               @Param("today") LocalDate today);
}
