package com.project.hotelbooking.repository;

import com.project.hotelbooking.model.Booking;
import com.project.hotelbooking.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("SELECT b FROM Booking b WHERE b.room.hotel.vendor.id = :vendorId ORDER BY b.createdAt DESC")
    List<Booking> findRecentBookingsByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.vendor.id = :vendorId")
    long countBookingsByVendorId(@Param("vendorId") Long vendorId);

    @Query("SELECT SUM(b.totalPrice) FROM Booking b WHERE b.room.hotel.vendor.id = :vendorId")
    Double sumRevenueByVendorId(@Param("vendorId") Long vendorId);

    List<Booking> findByUserId(Long userId);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.room.id = :roomId " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND (b.checkInDate < :checkOutDate AND b.checkOutDate > :checkInDate)")
     List<Booking> findOverlappingBookings(@Param("roomId") Long roomId,
                                          @Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate);

    
 // New method to count bookings by status for a vendor
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.room.hotel.vendor.id = :vendorId AND b.status = :status")
    long countByVendorIdAndStatus(@Param("vendorId") Long vendorId, @Param("status") BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.room.id = :roomId AND b.status IN ('PENDING', 'CONFIRMED') ORDER BY b.checkOutDate DESC")
    Optional<Booking> findLatestBookingByRoomId(@Param("roomId") Long roomId);
}