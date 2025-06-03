package com.tedu.seniorproject.ecomap.Service.openrouteservice;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OpenRouteService {

    // TODO: Move this to application.properties or environment variable
    private final String apiKey = "5b3ce3597851110001cf624893363b538f1041f1ad3b30ab1d2a5640";
    private final String baseUrl = "https://api.openrouteservice.org";
    private final RestTemplate restTemplate = new RestTemplate();

    public String geocodeSearch(String text) {
        String url = baseUrl + "/geocode/search?text=" + text;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setAccept(MediaType.parseMediaTypes("application/json"));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );

        return response.getBody();
    }

    public String calculateRoute(String jsonBody) {
        String url = baseUrl + "/v2/directions/driving-car";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> requestEntity = new HttpEntity<>(jsonBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url,
                requestEntity,
                String.class
        );

        return response.getBody();
    }
}