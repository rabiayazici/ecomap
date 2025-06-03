package com.tedu.seniorproject.ecomap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CarRequest {
    @NotBlank(message = "Car name is required")
    private String name;

    @NotBlank(message = "Car model is required")
    private String model;

    @NotNull(message = "Fuel consumption is required")
    @Positive(message = "Fuel consumption must be positive")
    private Double fuelConsumption;
} 