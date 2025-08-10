package com.project.hotelbooking.dto.publik;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReviewResponseDto {
    private Long id;
    private int rating;
    private String comment;
    private String userName;
    private String hotelName;
    private LocalDateTime createdAt;
}
