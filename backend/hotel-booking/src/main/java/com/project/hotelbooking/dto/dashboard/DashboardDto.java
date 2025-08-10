package com.project.hotelbooking.dto.dashboard;

import lombok.Data;
import java.util.List;

@Data
public class DashboardDto {
    private long totalBookings;
    private long pendingBookings;
    private Double totalRevenue;
    private List<DashboardBookingDto> bookings;
}