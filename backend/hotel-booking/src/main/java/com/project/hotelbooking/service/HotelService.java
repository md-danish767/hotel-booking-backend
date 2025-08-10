package com.project.hotelbooking.service;

import com.project.hotelbooking.dto.publik.PublicHotelDto;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;


import com.project.hotelbooking.dto.publik.HotelDetailsDto;
import com.project.hotelbooking.dto.publik.PublicRoomDto;
import com.project.hotelbooking.dto.publik.RoomInHotelDetailsDto;
import com.project.hotelbooking.dto.vendor.RoomDto;
import com.project.hotelbooking.dto.vendor.RoomListingDto;
import com.project.hotelbooking.model.Booking;
import com.project.hotelbooking.model.BookingStatus;
import com.project.hotelbooking.model.Hotel;
import com.project.hotelbooking.model.Room;
import com.project.hotelbooking.model.User;
import com.project.hotelbooking.repository.BookingRepository;
import com.project.hotelbooking.repository.HotelRepository;
import com.project.hotelbooking.repository.ReviewRepository;
import com.project.hotelbooking.repository.RoomRepository;
import com.project.hotelbooking.repository.UserRepository;
import com.project.hotelbooking.service.util.ImageUploadService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class HotelService {

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final ImageUploadService imageUploadService;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final ReviewRepository reviewRepository;

    public HotelService(HotelRepository hotelRepository, UserRepository userRepository, ImageUploadService imageUploadService, RoomRepository roomRepository, BookingRepository bookingRepository, ReviewRepository reviewRepository) {
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.imageUploadService = imageUploadService;
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.reviewRepository = reviewRepository;
    }

    public Hotel addHotel(String name, String description, String address, String city, String country, MultipartFile image) throws IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String vendorEmail = authentication.getName();
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        String imageUrl = null;
        if (image != null && !image.isEmpty()) {
            imageUrl = imageUploadService.saveImage(image);
        }

        Hotel hotel = new Hotel();
        hotel.setName(name);
        hotel.setDescription(description);
        hotel.setAddress(address);
        hotel.setCity(city);
        hotel.setCountry(country);
        hotel.setImageUrl(imageUrl);
        hotel.setVendor(vendor);

        return hotelRepository.save(hotel);
    }
    
    public HotelDetailsDto getHotelDetails(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        HotelDetailsDto dto = new HotelDetailsDto();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setDescription(hotel.getDescription());
        dto.setAddress(hotel.getAddress());
        dto.setCity(hotel.getCity());
        dto.setCountry(hotel.getCountry());
        dto.setImageUrl(hotel.getImageUrl());
        
        dto.setAverageRating(reviewRepository.findAverageRatingByHotelId(hotelId));
        dto.setReviewCount(reviewRepository.countByHotelId(hotelId));
        
        List<RoomInHotelDetailsDto> roomDtos = roomRepository.findByHotelId(hotelId).stream()
                .map(room -> {
                    RoomInHotelDetailsDto roomDto = new RoomInHotelDetailsDto();
                    roomDto.setId(room.getId());
                    roomDto.setRoomType(room.getRoomType());
                    roomDto.setCapacity(room.getCapacity());
                    roomDto.setPricePerNight(room.getPricePerNight());
                    roomDto.setImages(room.getImages());
                    roomDto.setAmenities(room.getAmenities());
                    roomDto.setAvailable(room.isAvailable());
                    return roomDto;
                })
                .collect(Collectors.toList());
        
        dto.setRooms(roomDtos);
        
        return dto;
    }

    public List<RoomInHotelDetailsDto> getAvailableRooms(Long hotelId, LocalDate checkInDate, LocalDate checkOutDate) {
        if (checkInDate == null || checkOutDate == null) {
            return List.of();
        }

        List<Room> allRoomsInHotel = roomRepository.findByHotelId(hotelId);

        return allRoomsInHotel.stream()
                .filter(room -> room.isAvailable())
                .filter(room -> bookingRepository.findOverlappingBookings(room.getId(), checkInDate, checkOutDate).isEmpty())
                .map(room -> {
                    RoomInHotelDetailsDto dto = new RoomInHotelDetailsDto();
                    dto.setId(room.getId());
                    dto.setRoomType(room.getRoomType());
                    dto.setCapacity(room.getCapacity());
                    dto.setPricePerNight(room.getPricePerNight());
                    dto.setImages(room.getImages());
                    dto.setAmenities(room.getAmenities());
                    dto.setAvailable(room.isAvailable());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<PublicHotelDto> getAllHotels(Map<String, String> filters) {
        Specification<Hotel> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // Filter by City
            if (filters.containsKey("city") && !filters.get("city").isEmpty()) {
                String citiesParam = filters.get("city");
                List<String> cities = Arrays.asList(citiesParam.split(","));
                predicates.add(root.get("city").in(cities));
            }

            // Filter by Date Availability
            if (filters.containsKey("checkInDate") && filters.containsKey("checkOutDate")) {
                LocalDate checkInDate = LocalDate.parse(filters.get("checkInDate"));
                LocalDate checkOutDate = LocalDate.parse(filters.get("checkOutDate"));
                
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Booking> subRoot = subquery.from(Booking.class);
                Join<Booking, Room> roomJoin = subRoot.join("room");
                
                subquery.select(roomJoin.get("hotel").get("id"))
                        .where(
                            cb.equal(roomJoin.get("hotel").get("id"), root.get("id")),
                            cb.isTrue(roomJoin.get("isAvailable")),
                            cb.equal(subRoot.get("status"), BookingStatus.CONFIRMED),
                            cb.lessThan(subRoot.get("checkInDate"), checkOutDate),
                            cb.greaterThan(subRoot.get("checkOutDate"), checkInDate)
                        );
                
                predicates.add(cb.exists(subquery));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort sort = Sort.unsorted();
        if (filters.containsKey("sortBy")) {
            switch (filters.get("sortBy")) {
                case "Name A-Z": sort = Sort.by("name").ascending(); break;
                case "Name Z-A": sort = Sort.by("name").descending(); break;
                case "City A-Z": sort = Sort.by("city").ascending(); break;
                case "City Z-A": sort = Sort.by("city").descending(); break;
                default: sort = Sort.by("name").ascending();
            }
        } else {
            sort = Sort.by("name").ascending();
        }

        List<Hotel> hotels = hotelRepository.findAll(spec, sort);

        return hotels.stream()
                .map(hotel -> {
                    PublicHotelDto dto = new PublicHotelDto();
                    dto.setId(hotel.getId());
                    dto.setName(hotel.getName());
                    dto.setDescription(hotel.getDescription());
                    dto.setAddress(hotel.getAddress());
                    dto.setCity(hotel.getCity());
                    dto.setCountry(hotel.getCountry());
                    dto.setImageUrl(hotel.getImageUrl());
                    dto.setAverageRating(reviewRepository.findAverageRatingByHotelId(hotel.getId()));
                    dto.setReviewCount(reviewRepository.countByHotelId(hotel.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }
    public List<PublicHotelDto> getFeaturedHotels() {
        Pageable limit = PageRequest.of(0, 4);
        return hotelRepository.findAll(limit).stream()
                .map(hotel -> {
                    PublicHotelDto dto = new PublicHotelDto();
                    dto.setId(hotel.getId());
                    dto.setName(hotel.getName());
                    dto.setDescription(hotel.getDescription());
                    dto.setAddress(hotel.getAddress());
                    dto.setCity(hotel.getCity());
                    dto.setCountry(hotel.getCountry());
                    dto.setImageUrl(hotel.getImageUrl());
                    dto.setAverageRating(reviewRepository.findAverageRatingByHotelId(hotel.getId()));
                    dto.setReviewCount(reviewRepository.countByHotelId(hotel.getId()));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<Hotel> getHotelsByVendor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String vendorEmail = authentication.getName();
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        return hotelRepository.findByVendorId(vendor.getId());
    }

    public List<String> getDistinctCities() {
        return hotelRepository.findDistinctCities();
    }
}