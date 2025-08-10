package com.project.hotelbooking.dto;

import lombok.Data;

@Data
public class AuthResponseDto {
    private String accessToken;
    private String role;
}