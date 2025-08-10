package com.project.hotelbooking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class UserController {

    @GetMapping("/me")
    public ResponseEntity<String> getMyDetails() {
        return ResponseEntity.ok("This is a protected endpoint! You are authenticated.");
    }
}