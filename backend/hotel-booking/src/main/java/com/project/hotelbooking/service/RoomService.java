package com.project.hotelbooking.service;

import com.project.hotelbooking.dto.publik.PublicRoomDto;

import com.project.hotelbooking.dto.vendor.RoomDto;
import com.project.hotelbooking.dto.vendor.RoomListingDto;
import com.project.hotelbooking.model.Booking;
import com.project.hotelbooking.model.Hotel;
import com.project.hotelbooking.model.Room;
import com.project.hotelbooking.model.User;
import com.project.hotelbooking.repository.BookingRepository;
import com.project.hotelbooking.repository.HotelRepository;
import com.project.hotelbooking.repository.RoomRepository;
import com.project.hotelbooking.repository.UserRepository;
import com.project.hotelbooking.service.util.ImageUploadService;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.Predicate;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final BookingRepository bookingRepository;

    public RoomService(RoomRepository roomRepository, HotelRepository hotelRepository, UserRepository userRepository, ImageUploadService imageUploadService, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.imageUploadService = imageUploadService;
        this.bookingRepository = bookingRepository;
    }

    public Room addRoom(RoomDto roomDto, List<MultipartFile> images) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String vendorEmail = authentication.getName();
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        Hotel hotel = hotelRepository.findById(roomDto.getHotelId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));
        if (!hotel.getVendor().getId().equals(vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to add a room to this hotel");
        }
        
        List<String> imageUrls = images.stream()
                .map(file -> {
                    try {
                        return imageUploadService.saveImage(file);
                    } catch (IOException e) {
                        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Image upload failed", e);
                    }
                })
                .collect(Collectors.toList());

        Room room = new Room();
        room.setRoomType(roomDto.getRoomType());
        room.setCapacity(1);
        room.setPricePerNight(roomDto.getPricePerNight());
        room.setImages(imageUrls);
        room.setAmenities(roomDto.getAmenities());
        room.setHotel(hotel);
        room.setAvailable(true);

        return roomRepository.save(room);
    }
    
    public List<RoomListingDto> getRoomsByVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String vendorEmail = authentication.getName();
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        List<Hotel> hotels = hotelRepository.findByVendorId(vendor.getId());

        return hotels.stream()
                .flatMap(hotel -> roomRepository.findByHotelId(hotel.getId()).stream())
                .map(room -> {
                    RoomListingDto dto = new RoomListingDto();
                    dto.setId(room.getId());
                    dto.setRoomType(room.getRoomType());
                    dto.setPricePerNight(room.getPricePerNight());
                    dto.setAvailable(room.isAvailable());
                    dto.setHotelName(room.getHotel() != null ? room.getHotel().getName() : "Unknown Hotel");
                    
                    Optional<Booking> latestBooking = bookingRepository.findLatestBookingByRoomId(room.getId());
                    if (latestBooking.isPresent() && latestBooking.get().getCheckOutDate().isAfter(LocalDate.now())) {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                        dto.setAvailabilityStatusMessage("Booked till " + latestBooking.get().getCheckOutDate().format(formatter));
                        dto.setLatestBookingCheckInDate(latestBooking.get().getCheckInDate());
                        dto.setLatestBookingCheckOutDate(latestBooking.get().getCheckOutDate());
                    } else if (!room.isAvailable()) {
                        dto.setAvailabilityStatusMessage("Unavailable");
                    } else {
                        dto.setAvailabilityStatusMessage("Available");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }

    public RoomListingDto toggleRoomAvailability(Long roomId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String vendorEmail = authentication.getName();
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));
        
        if (!room.getHotel().getVendor().getId().equals(vendor.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to modify this room.");
        }
        
        if (room.isAvailable() == false) {
             Optional<Booking> latestBooking = bookingRepository.findLatestBookingByRoomId(room.getId());
             if (latestBooking.isPresent() && latestBooking.get().getCheckOutDate().isAfter(LocalDate.now())) {
                 throw new ResponseStatusException(HttpStatus.CONFLICT, "Cannot make room available while it has an active booking.");
             }
        }

        room.setAvailable(!room.isAvailable());
        Room updatedRoom = roomRepository.save(room);

        RoomListingDto dto = new RoomListingDto();
        dto.setId(updatedRoom.getId());
        dto.setRoomType(updatedRoom.getRoomType());
        dto.setPricePerNight(updatedRoom.getPricePerNight());
        dto.setAmenities(updatedRoom.getAmenities());
        dto.setAvailable(updatedRoom.isAvailable());
        dto.setHotelName(updatedRoom.getHotel().getName());
        
        if (!updatedRoom.isAvailable()) {
             dto.setAvailabilityStatusMessage("Unavailable");
        } else {
             dto.setAvailabilityStatusMessage("Available");
        }

        return dto;
    }

    public List<PublicRoomDto> getPublicRooms(Map<String, String> filters) {
        Specification<Room> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("isAvailable")));

            if (filters.containsKey("roomType") && !filters.get("roomType").isEmpty()) {
                predicates.add(cb.equal(root.get("roomType"), filters.get("roomType")));
            }
            if (filters.containsKey("priceRange")) {
                String priceRange = filters.get("priceRange");
                if (priceRange != null) {
                    String[] prices = priceRange.split(" to ");
                    if (prices.length == 2) {
                        BigDecimal minPrice = new BigDecimal(prices[0]);
                        BigDecimal maxPrice = new BigDecimal(prices[1]);
                        predicates.add(cb.between(root.get("pricePerNight"), minPrice, maxPrice));
                    }
                }
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (filters.containsKey("sortBy")) {
            switch (filters.get("sortBy")) {
                case "Price Low to High":
                    sort = Sort.by("pricePerNight").ascending();
                    break;
                case "Price High to Low":
                    sort = Sort.by("pricePerNight").descending();
                    break;
                case "Newest First":
                    sort = Sort.by("createdAt").descending();
                    break;
            }
        } else {
            sort = Sort.by("createdAt").descending();
        }

        List<Room> rooms = roomRepository.findAll(spec, sort);

        return rooms.stream()
                .map(room -> {
                    PublicRoomDto dto = new PublicRoomDto();
                    dto.setId(room.getId());
                    dto.setRoomType(room.getRoomType());
                    dto.setCapacity(room.getCapacity());
                    dto.setPricePerNight(room.getPricePerNight());
                    dto.setImages(room.getImages());
                    dto.setAmenities(room.getAmenities());
                    dto.setHotelName(room.getHotel().getName());
                    dto.setHotelAddress(room.getHotel().getAddress());
                    dto.setHotelCity(room.getHotel().getCity());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<Room> getAvailableRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return List.of();
        }

        List<Room> allRoomsInHotel = roomRepository.findByHotelId(hotelId);

        return allRoomsInHotel.stream()
                .filter(room -> room.isAvailable())
                .filter(room -> bookingRepository.findOverlappingBookings(room.getId(), checkInDate, checkOutDate).isEmpty())
                .collect(Collectors.toList());
    }
}