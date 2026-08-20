package com.staysure.property.repository;

import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.Bed;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.property.enums.BedStatus;
import com.staysure.property.enums.FloorStatus;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.RoomStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BedRepository extends JpaRepository<Bed, Long> {
    List<Bed> findAllByRoomAndStatusNotOrderByBedNumberAsc(Room room, BedStatus status);

    boolean existsByRoomAndBedNumber(Room room, String bedNumber);

    long countByRoomAndStatusNot(Room room, BedStatus status);

    @Query("select count(b) from Bed b where b.room.floor.property = :property and b.status <> :status")
    long countByPropertyAndStatusNot(@Param("property") PgProperty property, @Param("status") BedStatus status);

    @Query("select count(b) from Bed b where b.room.floor.property = :property and b.status = :status")
    long countByPropertyAndStatus(@Param("property") PgProperty property, @Param("status") BedStatus status);

    @Query("select b.room.floor.property.id, count(b) from Bed b where b.room.floor.property.id in :propertyIds and b.status <> :status group by b.room.floor.property.id")
    List<Object[]> countByPropertyIdsAndStatusNot(@Param("propertyIds") Collection<Long> propertyIds, @Param("status") BedStatus status);

    @Query("select b.room.floor.property.id, count(b) from Bed b where b.room.floor.property.id in :propertyIds and b.status = :status group by b.room.floor.property.id")
    List<Object[]> countByPropertyIdsAndStatus(@Param("propertyIds") Collection<Long> propertyIds, @Param("status") BedStatus status);

    @Query("select b.room.floor.property.id, count(b) from Bed b where b.room.floor.property.id in :propertyIds and b.status <> :bedStatus and b.room.status = :roomStatus and b.room.floor.status = :floorStatus group by b.room.floor.property.id")
    List<Object[]> countByPublicPropertyIdsAndStatusNot(@Param("propertyIds") Collection<Long> propertyIds,
                                                        @Param("bedStatus") BedStatus bedStatus,
                                                        @Param("roomStatus") RoomStatus roomStatus,
                                                        @Param("floorStatus") FloorStatus floorStatus);

    @Query("select b.room.floor.property.id, count(b) from Bed b where b.room.floor.property.id in :propertyIds and b.status = :bedStatus and b.room.status = :roomStatus and b.room.floor.status = :floorStatus group by b.room.floor.property.id")
    List<Object[]> countByPublicPropertyIdsAndStatus(@Param("propertyIds") Collection<Long> propertyIds,
                                                     @Param("bedStatus") BedStatus bedStatus,
                                                     @Param("roomStatus") RoomStatus roomStatus,
                                                     @Param("floorStatus") FloorStatus floorStatus);

    @Query("select count(b) from Bed b where b.room.floor.property = :property and b.status <> :bedStatus and b.room.status = :roomStatus and b.room.floor.status = :floorStatus")
    long countByPublicPropertyAndStatusNot(@Param("property") PgProperty property,
                                           @Param("bedStatus") BedStatus bedStatus,
                                           @Param("roomStatus") RoomStatus roomStatus,
                                           @Param("floorStatus") FloorStatus floorStatus);

    @Query("select count(b) from Bed b where b.room.floor.property = :property and b.status = :bedStatus and b.room.status = :roomStatus and b.room.floor.status = :floorStatus")
    long countByPublicPropertyAndStatus(@Param("property") PgProperty property,
                                        @Param("bedStatus") BedStatus bedStatus,
                                        @Param("roomStatus") RoomStatus roomStatus,
                                        @Param("floorStatus") FloorStatus floorStatus);

    @Query("select b.room.id, count(b) from Bed b where b.room.id in :roomIds and b.status = :status group by b.room.id")
    List<Object[]> countByRoomIdsAndStatus(@Param("roomIds") Collection<Long> roomIds, @Param("status") BedStatus status);

    @Query("select b from Bed b where b.room.id in :roomIds and b.status = :status order by b.room.id asc, b.bedNumber asc")
    List<Bed> findByRoomIdsAndStatus(@Param("roomIds") Collection<Long> roomIds, @Param("status") BedStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bed b where b.id = :id")
    Optional<Bed> findLockedById(@Param("id") Long id);

    @Query("select count(b) from Bed b where b.room.floor.property.owner = :owner and b.status <> :bedStatus and b.room.status <> :roomStatus and b.room.floor.status <> :floorStatus and b.room.floor.property.status <> :propertyStatus")
    long countByOwnerAndStatusesNot(@Param("owner") OwnerProfile owner,
                                    @Param("bedStatus") BedStatus bedStatus,
                                    @Param("roomStatus") RoomStatus roomStatus,
                                    @Param("floorStatus") FloorStatus floorStatus,
                                    @Param("propertyStatus") PropertyStatus propertyStatus);

    @Query("select count(b) from Bed b where b.room.floor.property.owner = :owner and b.status = :bedStatus and b.room.status <> :roomStatus and b.room.floor.status <> :floorStatus and b.room.floor.property.status <> :propertyStatus")
    long countByOwnerAndStatusWithParentsNot(@Param("owner") OwnerProfile owner,
                                             @Param("bedStatus") BedStatus bedStatus,
                                             @Param("roomStatus") RoomStatus roomStatus,
                                             @Param("floorStatus") FloorStatus floorStatus,
                                             @Param("propertyStatus") PropertyStatus propertyStatus);
}
