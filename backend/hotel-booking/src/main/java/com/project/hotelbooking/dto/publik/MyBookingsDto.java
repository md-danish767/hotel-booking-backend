package com.project.hotelbooking.dto.publik;

import com.project.hotelbooking.model.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class MyBookingsDto {
    private Long id;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private boolean isPaid;
    private String hotelName;
    private String roomType;
    private String hotelAddress;
    private int guestCount;
    private List<String> roomImages;
}
