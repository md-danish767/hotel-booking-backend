package com.project.hotelbooking.dto.publik;

import lombok.Data;
import java.time.LocalDate;

@Data
public class BookingDto {
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private int guestCount;
}