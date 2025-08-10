package com.project.hotelbooking.dto.publik;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomInHotelDetailsDto {
    private Long id;
    private String roomType;
    private int capacity;
    private BigDecimal pricePerNight;
    private List<String> images;
    private List<String> amenities;
    private boolean isAvailable;
}
