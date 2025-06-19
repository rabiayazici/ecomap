package com.tedu.seniorproject.ecomap.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import java.time.Year;
import lombok.Data;

@Data
public class CarRequest {
    @NotBlank(message = "Car name is required")
    private String name;

    @NotBlank(message = "Vehicle type is required")
    private String vehicle_type; // A, B, C, D, E, F, S, J, M

    @NotBlank(message = "Fuel type is required")
    private String fuel_type; // diesel, electric, hybrid, petrol, plugin hybrid

    @NotNull(message = "Year is required")
    @Max(value = Year.MAX_VALUE, message = "Year cannot be in the future") // Placeholder, will set dynamically
    private Integer year;

    // Custom setter to enforce max year at runtime
    public void setYear(Integer year) {
        int currentYear = Year.now().getValue();
        if (year != null && year > currentYear) {
            throw new IllegalArgumentException("Year cannot be greater than the current year: " + currentYear);
        }
        this.year = year;
    }
} 