package com.project.hotelbooking.service;

import com.project.hotelbooking.dto.dashboard.DashboardBookingDto;
import com.project.hotelbooking.dto.dashboard.DashboardDto;
import com.project.hotelbooking.dto.dashboard.DashboardUserDto;
import com.project.hotelbooking.dto.dashboard.DashboardRoomDto;
import com.project.hotelbooking.model.BookingStatus;
import com.project.hotelbooking.model.User;
import com.project.hotelbooking.repository.BookingRepository;
import com.project.hotelbooking.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;

    public DashboardService(BookingRepository bookingRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.userRepository = userRepository;
    }

    public DashboardDto getVendorDashboardData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String vendorEmail = authentication.getName();
        User vendor = userRepository.findByEmail(vendorEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vendor not found"));

        DashboardDto dashboardDto = new DashboardDto();
        dashboardDto.setTotalBookings(bookingRepository.countBookingsByVendorId(vendor.getId()));
       dashboardDto.setPendingBookings(bookingRepository.countByVendorIdAndStatus(vendor.getId(), BookingStatus.PENDING));
        dashboardDto.setTotalRevenue(bookingRepository.sumRevenueByVendorId(vendor.getId()));

        List<DashboardBookingDto> recentBookings = bookingRepository.findRecentBookingsByVendorId(vendor.getId()).stream()
                .limit(5)
                .map(booking -> {
                    DashboardBookingDto dto = new DashboardBookingDto();
                    dto.setId(booking.getId());
                    dto.setTotalPrice(booking.getTotalPrice());
                    dto.setPaid(booking.getStatus() == BookingStatus.CONFIRMED);

                    DashboardRoomDto roomDto = new DashboardRoomDto();
                    roomDto.setRoomType(booking.getRoom().getRoomType());
                    roomDto.setHotelName(booking.getRoom().getHotel().getName());
                    dto.setRoom(roomDto);

                    DashboardUserDto userDto = new DashboardUserDto();
                    userDto.setUsername(booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                    dto.setUser(userDto);
                    
                    dto.setStatus(booking.getStatus().name()); // Correctly populate the new status field
                    
                    return dto;
                }).collect(Collectors.toList());

        dashboardDto.setBookings(recentBookings);

        return dashboardDto;
    }
}