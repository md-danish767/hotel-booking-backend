// BookingController.java
package com.project.hotelbooking.controller;

import com.project.hotelbooking.dto.publik.BookingDto;
import com.project.hotelbooking.dto.publik.BookingResponseDto;
import com.project.hotelbooking.dto.publik.MyBookingsDto;
import com.project.hotelbooking.dto.publik.PaymentDto;
import com.project.hotelbooking.service.BookingService;
import com.stripe.exception.StripeException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<BookingResponseDto> createBooking(@RequestBody BookingDto bookingDto) {
        BookingResponseDto newBooking = bookingService.createBooking(bookingDto);
        return ResponseEntity.ok(newBooking);
    }
    
    @PatchMapping("/vendor/{bookingId}/approve")
    @PreAuthorize("hasRole('ROLE_VENDOR')")
    public ResponseEntity<Void> approveBooking(@PathVariable Long bookingId) {
        bookingService.approveBooking(bookingId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/me")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<MyBookingsDto>> getMyBookings() {
        List<MyBookingsDto> myBookings = bookingService.getMyBookings();
        return ResponseEntity.ok(myBookings);
    }
    @PostMapping("/create-payment-intent")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<String> createPaymentIntent(@RequestBody PaymentDto paymentDto) throws StripeException {
        String clientSecret = bookingService.createPaymentIntent(paymentDto);
        return ResponseEntity.ok(clientSecret);
    }
}