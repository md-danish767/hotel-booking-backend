package com.project.hotelbooking.dto.vendor;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RoomListingDto {
    private Long id;
    private String roomType;
    private BigDecimal pricePerNight;
    private List<String> amenities;
    private boolean isAvailable;
    private String hotelName;
    private String availabilityStatusMessage;
    private LocalDate latestBookingCheckInDate; // New field
    private LocalDate latestBookingCheckOutDate; // New field
}