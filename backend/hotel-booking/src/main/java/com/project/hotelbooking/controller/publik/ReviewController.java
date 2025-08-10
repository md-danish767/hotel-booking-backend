package com.project.hotelbooking.controller.publik;

import com.project.hotelbooking.dto.publik.ReviewDto;
import com.project.hotelbooking.dto.publik.ReviewResponseDto;
import com.project.hotelbooking.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/hotels/{hotelId}/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<ReviewResponseDto> addReview(@PathVariable Long hotelId, @RequestBody ReviewDto reviewDto) {
        ReviewResponseDto newReview = reviewService.addReview(hotelId, reviewDto);
        return ResponseEntity.ok(newReview);
    }

    @GetMapping
    public ResponseEntity<List<ReviewResponseDto>> getReviews(@PathVariable Long hotelId) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByHotelId(hotelId);
        return ResponseEntity.ok(reviews);
    }
}
