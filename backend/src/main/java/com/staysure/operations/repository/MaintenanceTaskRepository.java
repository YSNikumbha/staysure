package com.staysure.operations.repository;

import com.staysure.operations.entity.MaintenanceTask;
import com.staysure.owner.entity.OwnerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MaintenanceTaskRepository extends JpaRepository<MaintenanceTask, Long> {

    boolean existsByTaskNumber(String taskNumber);

    long countByTaskNumberStartingWith(String prefix);

    @Query("select t from MaintenanceTask t where t.property.owner = :owner order by t.createdAt desc")
    List<MaintenanceTask> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select t from MaintenanceTask t where t.id = :id and t.property.owner = :owner")
    Optional<MaintenanceTask> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);
}
