package com.project.hotelbooking.repository;

import com.project.hotelbooking.model.Room;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> ,JpaSpecificationExecutor<Room>{
    List<Room> findByHotelId(Long hotelId);
}
