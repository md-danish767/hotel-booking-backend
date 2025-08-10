package com.project.hotelbooking.dto.dashboard;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardBookingDto {
	private Long id;
    private DashboardUserDto user;
    private DashboardRoomDto room; // Use the new DTO
    private BigDecimal totalPrice;
    private boolean isPaid;
    private String status;
}