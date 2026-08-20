package com.staysure.booking.repository;

import com.staysure.booking.entity.Booking;
import com.staysure.booking.enums.BookingStatus;
import com.staysure.property.entity.PgProperty;
import com.staysure.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByBookingNumber(String bookingNumber);

    List<Booking> findByUserOrderByCreatedAtDesc(User user);

    List<Booking> findByPropertyOrderByCreatedAtDesc(PgProperty property);

    Optional<Booking> findByBedIdAndStatusIn(Long bedId, List<BookingStatus> statuses);

    List<Booking> findByStatusInOrderByCreatedAtDesc(List<BookingStatus> statuses);
}