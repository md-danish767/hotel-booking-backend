package com.project.hotelbooking.dto.publik;

import lombok.Data;

@Data
public class PublicHotelDto {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
    private String imageUrl;
    private Double averageRating;
    private long reviewCount;
}