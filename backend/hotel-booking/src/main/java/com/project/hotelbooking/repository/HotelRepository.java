package com.project.hotelbooking.repository;

import com.project.hotelbooking.model.Hotel;
import com.project.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {
    
    List<Hotel> findByVendorId(Long vendorId);

    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId AND r.images IS NOT EMPTY ORDER BY r.createdAt ASC LIMIT 1")
    Optional<Room> findFirstRoomWithImageByHotelId(@Param("hotelId") Long hotelId);

    @Query("SELECT DISTINCT h.city FROM Hotel h")
    List<String> findDistinctCities();

    @Query("SELECT h FROM Hotel h WHERE h.id IN ( " +
            "SELECT DISTINCT r.hotel.id FROM Room r WHERE r.isAvailable = true " +
            "AND NOT EXISTS (SELECT b FROM Booking b WHERE b.room.id = r.id " +
            "AND b.status IN ('PENDING', 'CONFIRMED') " +
            "AND (:checkInDate < b.checkOutDate AND :checkOutDate > b.checkInDate))" +
            ")")
     List<Hotel> findByAvailableRoomDates(@Param("checkInDate") LocalDate checkInDate,
                                          @Param("checkOutDate") LocalDate checkOutDate);
}