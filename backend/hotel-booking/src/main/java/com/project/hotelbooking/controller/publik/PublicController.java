package com.project.hotelbooking.controller.publik;

import com.project.hotelbooking.dto.publik.HotelDetailsDto;
import com.project.hotelbooking.dto.publik.PublicHotelDto;
import com.project.hotelbooking.dto.publik.PublicRoomDto;
import com.project.hotelbooking.dto.publik.ReviewResponseDto;
import com.project.hotelbooking.dto.publik.RoomInHotelDetailsDto;
import com.project.hotelbooking.model.Hotel;
import com.project.hotelbooking.model.Room;
import com.project.hotelbooking.service.HotelService;
import com.project.hotelbooking.service.ReviewService;
import com.project.hotelbooking.service.RoomService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final RoomService roomService;
    private final HotelService hotelService;
    private final ReviewService reviewService;

    public PublicController(RoomService roomService,HotelService hotelService,ReviewService reviewService) {
        this.roomService = roomService;
        this.hotelService = hotelService;
        this.reviewService = reviewService;
        
    }

    @GetMapping("/hotels") // Endpoint to get all hotels
    public ResponseEntity<List<PublicHotelDto>> getAllHotels(@RequestParam Map<String, String> filters) {
        List<PublicHotelDto> hotels = hotelService.getAllHotels(filters);
        return ResponseEntity.ok(hotels);
    }
    @GetMapping("/hotels/{hotelId}")
    public ResponseEntity<HotelDetailsDto> getHotelDetails(@PathVariable Long hotelId) {
        HotelDetailsDto hotel = hotelService.getHotelDetails(hotelId);
        return ResponseEntity.ok(hotel);
    }
    
    @GetMapping("/hotels/{hotelId}/available-rooms")
    public ResponseEntity<List<RoomInHotelDetailsDto>> getAvailableRooms(
            @PathVariable Long hotelId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate
    ) {
        List<RoomInHotelDetailsDto> availableRooms = hotelService.getAvailableRooms(hotelId, checkInDate, checkOutDate);
        return ResponseEntity.ok(availableRooms);
    }
    
    @GetMapping("/featured-hotels")
    public ResponseEntity<List<PublicHotelDto>> getFeaturedHotels() {
        List<PublicHotelDto> featuredHotels = hotelService.getFeaturedHotels();
        return ResponseEntity.ok(featuredHotels);
    }
    
    @GetMapping("/rooms")
    public ResponseEntity<List<PublicRoomDto>> getAllRooms(@RequestParam Map<String, String> filters) {
        List<PublicRoomDto> rooms = roomService.getPublicRooms(filters);
        return ResponseEntity.ok(rooms);
    }
    @GetMapping("/cities")
    public ResponseEntity<List<String>> getCities() {
        List<String> cities = hotelService.getDistinctCities();
        return ResponseEntity.ok(cities);
    }
    @GetMapping("/reviews")
    public ResponseEntity<List<ReviewResponseDto>> getFeaturedReviews() {
        List<ReviewResponseDto> reviews = reviewService.getFeaturedReviews();
        return ResponseEntity.ok(reviews);
    }
}
