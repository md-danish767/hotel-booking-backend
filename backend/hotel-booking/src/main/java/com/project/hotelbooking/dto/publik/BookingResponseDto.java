package com.project.hotelbooking.dto.publik;

import com.project.hotelbooking.model.BookingStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BookingResponseDto {
    private Long id;
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private int guestCount;
}
