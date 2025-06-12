package com.tedu.seniorproject.ecomap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteCalculationRequest {
    private String image;  // Base64 encoded image
    private Point startPoint;
    private Point endPoint;
} 