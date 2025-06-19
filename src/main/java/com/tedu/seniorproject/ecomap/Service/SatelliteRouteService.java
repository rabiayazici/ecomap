package com.tedu.seniorproject.ecomap.Service;

import com.tedu.seniorproject.ecomap.dto.RouteCalculationRequest;
import com.tedu.seniorproject.ecomap.dto.RouteCalculationResponse;
import com.tedu.seniorproject.ecomap.dto.Point;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SatelliteRouteService {
    
    private final String PYTHON_API_URL = "http://localhost:5000/api";
    private final RestTemplate restTemplate;
    private final PythonService pythonService;

    public SatelliteRouteService(PythonService pythonService) {
        this.restTemplate = new RestTemplate();
        this.pythonService = pythonService;
    }

    public String convertTiffToPng(MultipartFile file) {
        try {
            log.debug("Converting TIFF to PNG, file size: {}", file.getSize());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            body.add("image", resource);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            
            log.debug("Sending request to Python API: {}", PYTHON_API_URL + "/convert-tiff");
            ResponseEntity<Map> response = restTemplate.exchange(
                PYTHON_API_URL + "/convert-tiff",
                HttpMethod.POST,
                requestEntity,
                Map.class
            );
            log.debug("Received response from Python API: {}", response.getStatusCode());

            if (response.getBody() != null && response.getBody().containsKey("convertedImage")) {
                return (String) response.getBody().get("convertedImage");
            }
            throw new RuntimeException("Failed to convert TIFF image: No converted image in response");
        } catch (Exception e) {
            log.error("Error converting TIFF: " + e.getMessage(), e);
            throw new RuntimeException("Failed to convert TIFF: " + e.getMessage(), e);
        }
    }

    public RouteCalculationResponse calculateRoute(RouteCalculationRequest request) {
        try {
            log.debug("Calculating route with request");
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                "image", request.getImage(),
                "start", Map.of("x", request.getStartPoint().getX(), "y", request.getStartPoint().getY()),
                "end", Map.of("x", request.getEndPoint().getX(), "y", request.getEndPoint().getY())
            );

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("Sending request to Python API: {}", PYTHON_API_URL + "/calculate-route");
            ResponseEntity<Map> response = restTemplate.exchange(
                PYTHON_API_URL + "/calculate-route",
                HttpMethod.POST,
                entity,
                Map.class
            );
            log.debug("Received response from Python API: {}", response.getStatusCode());

            if (response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                if (body.containsKey("error")) {
                    return new RouteCalculationResponse(null, null, (String) body.get("error"));
                }
                return new RouteCalculationResponse(
                    (java.util.List<Point>) body.get("path"),
                    (String) body.get("displayImage"),
                    null
                );
            }
            throw new RuntimeException("Empty response from Python service");
        } catch (Exception e) {
            log.error("Error calculating route: " + e.getMessage(), e);
            return new RouteCalculationResponse(null, null, "Failed to calculate route: " + e.getMessage());
        }
    }
} 