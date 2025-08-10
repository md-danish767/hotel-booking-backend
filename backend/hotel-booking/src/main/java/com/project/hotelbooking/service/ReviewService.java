package com.project.hotelbooking.service;

import com.project.hotelbooking.dto.publik.ReviewDto;
import com.project.hotelbooking.dto.publik.ReviewResponseDto;
import com.project.hotelbooking.model.Hotel;
import com.project.hotelbooking.model.Review;
import com.project.hotelbooking.model.User;
import com.project.hotelbooking.repository.HotelRepository;
import com.project.hotelbooking.repository.ReviewRepository;
import com.project.hotelbooking.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, HotelRepository hotelRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
    }

    public ReviewResponseDto addReview(Long hotelId, ReviewDto reviewDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Hotel not found"));

        Review review = new Review();
        review.setRating(reviewDto.getRating());
        review.setComment(reviewDto.getComment());
        review.setUser(user);
        review.setHotel(hotel);

        Review savedReview = reviewRepository.save(review);

        ReviewResponseDto responseDto = new ReviewResponseDto();
        responseDto.setId(savedReview.getId());
        responseDto.setRating(savedReview.getRating());
        responseDto.setComment(savedReview.getComment());
        responseDto.setUserName(savedReview.getUser().getFirstName() + " " + savedReview.getUser().getLastName());
        responseDto.setCreatedAt(savedReview.getCreatedAt());

        return responseDto;
    }

    public List<ReviewResponseDto> getReviewsByHotelId(Long hotelId) {
        List<Review> reviews = reviewRepository.findByHotelId(hotelId);
        return reviews.stream()
                .map(review -> {
                    ReviewResponseDto dto = new ReviewResponseDto();
                    dto.setId(review.getId());
                    dto.setRating(review.getRating());
                    dto.setComment(review.getComment());
                    dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
                    dto.setCreatedAt(review.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());
    }
    
    public List<ReviewResponseDto> getFeaturedReviews() {
        Pageable limit = PageRequest.of(0, 4);
        List<Review> reviews = reviewRepository.findAllByOrderByCreatedAtDesc(limit);
        return reviews.stream()
                .map(review -> {
                    ReviewResponseDto dto = new ReviewResponseDto();
                    dto.setId(review.getId());
                    dto.setRating(review.getRating());
                    dto.setComment(review.getComment());
                    dto.setUserName(review.getUser().getFirstName() + " " + review.getUser().getLastName());
                    dto.setHotelName(review.getHotel().getName());
                    dto.setCreatedAt(review.getCreatedAt());
                    return dto;
                }).collect(Collectors.toList());
    }
}