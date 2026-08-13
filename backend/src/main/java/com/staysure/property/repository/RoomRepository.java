package com.staysure.property.repository;

import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.Floor;
import com.staysure.property.entity.PgProperty;
import com.staysure.property.entity.Room;
import com.staysure.property.enums.FloorStatus;
import com.staysure.property.enums.PropertyStatus;
import com.staysure.property.enums.RoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findAllByFloorAndStatusNotOrderByRoomNumberAsc(Floor floor, RoomStatus status);

    boolean existsByFloorAndRoomNumber(Floor floor, String roomNumber);

    long countByFloorAndStatusNot(Floor floor, RoomStatus status);

    @Query("select count(r) from Room r where r.floor.property = :property and r.status <> :status")
    long countByPropertyAndStatusNot(@Param("property") PgProperty property, @Param("status") RoomStatus status);

    @Query("select r.floor.property.id, count(r) from Room r where r.floor.property.id in :propertyIds and r.status <> :status group by r.floor.property.id")
    List<Object[]> countByPropertyIdsAndStatusNot(@Param("propertyIds") Collection<Long> propertyIds, @Param("status") RoomStatus status);

    @Query("select r from Room r where r.floor.property = :property and r.status = :roomStatus and r.floor.status = :floorStatus order by r.monthlyRent asc, r.roomNumber asc")
    List<Room> findPublicRooms(@Param("property") PgProperty property,
                               @Param("roomStatus") RoomStatus roomStatus,
                               @Param("floorStatus") FloorStatus floorStatus);

    @Query("select count(r) from Room r where r.floor.property.owner = :owner and r.status <> :roomStatus and r.floor.status <> :floorStatus and r.floor.property.status <> :propertyStatus")
    long countByOwnerAndStatusesNot(@Param("owner") OwnerProfile owner,
                                    @Param("roomStatus") RoomStatus roomStatus,
                                    @Param("floorStatus") FloorStatus floorStatus,
                                    @Param("propertyStatus") PropertyStatus propertyStatus);
}
