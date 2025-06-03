package com.tedu.seniorproject.ecomap.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RouteRequest {
    @NotNull(message = "Start coordinate is required")
    private Double startCoordinate;

    @NotNull(message = "End coordinate is required")
    private Double endCoordinate;
} 