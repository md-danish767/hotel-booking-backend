package com.project.hotelbooking.service;

import com.project.hotelbooking.dto.publik.BookingDto;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import com.project.hotelbooking.dto.publik.BookingResponseDto;
import com.project.hotelbooking.dto.publik.MyBookingsDto;
import com.project.hotelbooking.dto.publik.PaymentDto;
import com.project.hotelbooking.model.Booking;
import com.project.hotelbooking.model.BookingStatus;
import com.project.hotelbooking.model.Room;
import com.project.hotelbooking.model.User;
import com.project.hotelbooking.repository.BookingRepository;
import com.project.hotelbooking.repository.RoomRepository;
import com.project.hotelbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    public BookingResponseDto createBooking(BookingDto bookingDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Room room = roomRepository.findById(bookingDto.getRoomId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found"));

        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(room.getId(), bookingDto.getCheckInDate(), bookingDto.getCheckOutDate());
        if (!overlappingBookings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Room is not available for the selected dates.");
        }

        if (bookingDto.getGuestCount() > room.getCapacity()) {
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Guest count exceeds room capacity.");
        }

        long nights = ChronoUnit.DAYS.between(bookingDto.getCheckInDate(), bookingDto.getCheckOutDate());
        BigDecimal totalPrice = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(bookingDto.getCheckInDate());
        booking.setCheckOutDate(bookingDto.getCheckOutDate());
        booking.setTotalPrice(totalPrice);
        booking.setStatus(BookingStatus.PENDING);
        booking.setGuestCount(bookingDto.getGuestCount());

        Booking savedBooking = bookingRepository.save(booking);
        
        BookingResponseDto responseDto = new BookingResponseDto();
        responseDto.setId(savedBooking.getId());
        responseDto.setRoomId(savedBooking.getRoom().getId());
        responseDto.setCheckInDate(savedBooking.getCheckInDate());
        responseDto.setCheckOutDate(savedBooking.getCheckOutDate());
        responseDto.setTotalPrice(savedBooking.getTotalPrice());
        responseDto.setStatus(savedBooking.getStatus());
        responseDto.setGuestCount(savedBooking.getGuestCount());

        return responseDto;
    }

    public void approveBooking(Long bookingId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String vendorEmail = authentication.getName();

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));

        if (!booking.getRoom().getHotel().getVendor().getEmail().equals(vendorEmail)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not authorized to approve this booking.");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only pending bookings can be approved.");
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }
    
    public List<MyBookingsDto> getMyBookings() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        List<Booking> myBookings = bookingRepository.findByUserId(user.getId());

        return myBookings.stream().map(booking -> {
            MyBookingsDto dto = new MyBookingsDto();
            dto.setId(booking.getId());
            dto.setCheckInDate(booking.getCheckInDate());
            dto.setCheckOutDate(booking.getCheckOutDate());
            dto.setTotalPrice(booking.getTotalPrice());
            dto.setStatus(booking.getStatus());
            dto.setPaid(booking.getStatus() == BookingStatus.CONFIRMED);
            dto.setHotelName(booking.getRoom().getHotel().getName());
            dto.setRoomType(booking.getRoom().getRoomType());
            dto.setHotelAddress(booking.getRoom().getHotel().getAddress());
            dto.setGuestCount(booking.getGuestCount());
            dto.setRoomImages(booking.getRoom().getImages());
            return dto;
        }).collect(Collectors.toList());
    }
    
    @Value("${stripe.secret.key}")
    private String stripeSecretKey;

    public String createPaymentIntent(PaymentDto paymentDto) throws StripeException {
        Stripe.apiKey = stripeSecretKey;

        Booking booking = bookingRepository.findById(paymentDto.getBookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found."));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Booking is already paid.");
        }

        PaymentIntentCreateParams params =
            PaymentIntentCreateParams.builder()
                .setAmount(booking.getTotalPrice().longValue() * 100L) // Stripe requires amount in cents
                .setCurrency("usd")
                .addPaymentMethodType("card") // Use this to add a single payment method
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.getClientSecret();
    }
}