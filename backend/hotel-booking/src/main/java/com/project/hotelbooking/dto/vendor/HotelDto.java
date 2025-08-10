package com.project.hotelbooking.dto.vendor;

import lombok.Data;
import java.util.List;

@Data
public class HotelDto {
    private String name;
    private String description;
    private String address;
    private String city;
    private String country;
}
