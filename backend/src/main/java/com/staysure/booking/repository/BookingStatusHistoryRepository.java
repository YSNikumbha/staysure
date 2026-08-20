package com.staysure.booking.repository;

import com.staysure.booking.entity.Booking;
import com.staysure.booking.entity.BookingStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingStatusHistoryRepository extends JpaRepository<BookingStatusHistory, Long> {
    List<BookingStatusHistory> findByBookingOrderByCreatedAtDesc(Booking booking);
}