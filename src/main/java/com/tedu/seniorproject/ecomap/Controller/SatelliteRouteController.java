package com.tedu.seniorproject.ecomap.controller;

import com.tedu.seniorproject.ecomap.dto.RouteCalculationRequest;
import com.tedu.seniorproject.ecomap.dto.RouteCalculationResponse;
import com.tedu.seniorproject.ecomap.service.SatelliteRouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/satellite-route")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SatelliteRouteController {

    private final SatelliteRouteService satelliteRouteService;

    @Autowired
    public SatelliteRouteController(SatelliteRouteService satelliteRouteService) {
        this.satelliteRouteService = satelliteRouteService;
    }

    @PostMapping("/convert-tiff")
    public ResponseEntity<?> convertTiff(@RequestParam("image") MultipartFile file) {
        try {
            log.debug("Received file: {}, size: {}", file.getOriginalFilename(), file.getSize());
            String convertedImage = satelliteRouteService.convertTiffToPng(file);
            return ResponseEntity.ok(Map.of("convertedImage", convertedImage));
        } catch (Exception e) {
            log.error("Error converting TIFF: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/calculate")
    public ResponseEntity<RouteCalculationResponse> calculateRoute(@RequestBody RouteCalculationRequest request) {
        try {
            log.debug("Calculating route with request: {}", request);
            RouteCalculationResponse response = satelliteRouteService.calculateRoute(request);
            if (response.getError() != null) {
                return ResponseEntity.badRequest().body(response);
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error calculating route: " + e.getMessage(), e);
            return ResponseEntity.badRequest().body(
                new RouteCalculationResponse(null, null, "Error: " + e.getMessage())
            );
        }
    }
} 