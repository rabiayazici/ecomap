package com.tedu.seniorproject.ecomap.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteCalculationResponse {
    private List<Point> path;
    private String displayImage;
    private String error;
} 