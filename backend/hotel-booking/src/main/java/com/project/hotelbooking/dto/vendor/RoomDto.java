package com.project.hotelbooking.dto.vendor;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomDto {
    private String roomType;
    private int capacity;
    private BigDecimal pricePerNight;
    private List<String> images;
    private List<String> amenities;
    private Long hotelId; // To link the room to a specific hotel
}