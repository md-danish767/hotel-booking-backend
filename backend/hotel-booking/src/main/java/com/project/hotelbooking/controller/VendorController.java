package com.project.hotelbooking.controller;

import com.project.hotelbooking.dto.vendor.HotelDto;
import com.project.hotelbooking.dto.dashboard.DashboardDto;
import com.project.hotelbooking.service.DashboardService;
import com.project.hotelbooking.dto.vendor.RoomDto;
import com.project.hotelbooking.dto.vendor.RoomListingDto;
import com.project.hotelbooking.model.Hotel;
import com.project.hotelbooking.model.Room;
import com.project.hotelbooking.service.HotelService;
import com.project.hotelbooking.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/vendor")
@PreAuthorize("hasRole('ROLE_VENDOR')")
public class VendorController {

    private final RoomService roomService;
    private final HotelService hotelService;
    private final DashboardService dashboardService;

    public VendorController(RoomService roomService,HotelService hotelService,DashboardService dashboardService) {
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.dashboardService = dashboardService;
    }
    @PostMapping(value = "/hotels", consumes = {"multipart/form-data"})
    public ResponseEntity<Hotel> addHotel(
        @RequestPart("name") String name,
        @RequestPart("description") String description,
        @RequestPart("address") String address,
        @RequestPart("city") String city,
        @RequestPart("country") String country,
        @RequestPart(value = "image", required = false) MultipartFile image
    ) throws IOException {
        Hotel newHotel = hotelService.addHotel(name, description, address, city, country, image);
        return ResponseEntity.ok(newHotel);
    }
    
    @GetMapping("/hotels") // New endpoint for listing hotels
    public ResponseEntity<List<Hotel>> getHotels() {
        List<Hotel> hotels = hotelService.getHotelsByVendor();
        return ResponseEntity.ok(hotels);
    }

    @PostMapping(value = "/rooms", consumes = {"multipart/form-data"})
    public ResponseEntity<Room> addRoom(
            @RequestPart("roomDto") RoomDto roomDto,
            @RequestPart("images") List<MultipartFile> images) throws IOException {
        Room newRoom = roomService.addRoom(roomDto, images);
        return ResponseEntity.ok(newRoom);
    }

    @GetMapping("/rooms")
    public ResponseEntity<List<RoomListingDto>> getMyRooms() {
        List<RoomListingDto> rooms = roomService.getRoomsByVendor();
        return ResponseEntity.ok(rooms);
    }

    @PatchMapping("/rooms/{roomId}/toggle-availability")
    public ResponseEntity<RoomListingDto> toggleRoomAvailability(@PathVariable Long roomId) {
        RoomListingDto updatedRoom = roomService.toggleRoomAvailability(roomId);
        return ResponseEntity.ok(updatedRoom);
    }
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardDto> getDashboardData() {
        DashboardDto dashboardData = dashboardService.getVendorDashboardData();
        return ResponseEntity.ok(dashboardData);
    }
}
