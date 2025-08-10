package com.project.hotelbooking.dto.publik;

import lombok.Data;
import java.util.List;

@Data
public class HotelDetailsDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private String imageUrl;
    private Double averageRating;
    private long reviewCount;
    private List<RoomInHotelDetailsDto> rooms; 
}