package com.staysure.booking.repository;

import com.staysure.booking.entity.Booking;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.owner.entity.OwnerProfile;
import com.staysure.property.entity.Bed;
import com.staysure.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Optional<Booking> findByIdAndUser(Long id, User user);

    List<Booking> findAllByUserOrderByCreatedAtDesc(User user);

    @Query("select b from Booking b where b.property.owner = :owner order by b.createdAt desc")
    List<Booking> findAllByOwner(@Param("owner") OwnerProfile owner);

    @Query("select b from Booking b where b.id = :id and b.property.owner = :owner")
    Optional<Booking> findByIdAndOwner(@Param("id") Long id, @Param("owner") OwnerProfile owner);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.id = :id")
    Optional<Booking> findLockedById(@Param("id") Long id);

    List<Booking> findAllByUserAndStatusInOrderByCreatedAtDesc(User user, List<BookingStatus> statuses);

    boolean existsByBedAndStatusIn(Bed bed, List<BookingStatus> statuses);
}
