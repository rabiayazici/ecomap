package com.tedu.seniorproject.ecomap.Controller;

import com.tedu.seniorproject.ecomap.Model.Route;
import com.tedu.seniorproject.ecomap.Model.User;
import com.tedu.seniorproject.ecomap.Service.RouteService;
import com.tedu.seniorproject.ecomap.Service.UserService;
import com.tedu.seniorproject.ecomap.Service.openrouteservice.OpenRouteService;
import com.tedu.seniorproject.ecomap.dto.RouteRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/routes")
@Tag(name = "Route Management", description = "APIs for managing routes and integrating with OpenRouteService")
public class RouteController {
    
    private final RouteService routeService;
    private final UserService userService;
    private final OpenRouteService openRouteService;

    public RouteController(RouteService routeService, UserService userService, OpenRouteService openRouteService) {
        this.routeService = routeService;
        this.userService = userService;
        this.openRouteService = openRouteService;
    }
    
    @Operation(
        summary = "Create a new route",
        description = "Creates a new route for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Route created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping
    public ResponseEntity<?> createRoute(@Valid @RequestBody RouteRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Route route = new Route();
        route.setStartCoordinate(request.getStartCoordinate());
        route.setEndCoordinate(request.getEndCoordinate());
        route.setUser(user);
        
        // Note: This currently saves the route with just coordinates and a timestamp.
        // You might want to update this after getting actual route data from OpenRouteService.
        return routeService.createRoute(route);
    }
    
    @Operation(
        summary = "Get route by ID",
        description = "Retrieves route details by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Route found")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @GetMapping("/{id}")
    public ResponseEntity<?> getRouteById(
        @Parameter(description = "Route ID", required = true)
        @PathVariable Long id
    ) {
        return routeService.getRouteById(id);
    }
    
    @Operation(
        summary = "Get routes by user ID",
        description = "Retrieves all routes belonging to a specific user"
    )
    @ApiResponse(responseCode = "200", description = "Routes found")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRoutesByUserId(
        @Parameter(description = "User ID", required = true)
        @PathVariable Long userId
    ) {
        return routeService.getRoutesByUserId(userId);
    }
    
    @Operation(
        summary = "Update route",
        description = "Updates route details by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Route updated successfully")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateRoute(
        @Parameter(description = "Route ID", required = true)
        @PathVariable Long id,
        @Valid @RequestBody RouteRequest request
    ) {
        Route route = new Route();
        route.setStartCoordinate(request.getStartCoordinate());
        route.setEndCoordinate(request.getEndCoordinate());
        return routeService.updateRoute(id, route);
    }
    
    @Operation(
        summary = "Delete route",
        description = "Deletes a route by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Route deleted successfully")
    @ApiResponse(responseCode = "404", description = "Route not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoute(
        @Parameter(description = "Route ID", required = true)
        @PathVariable Long id
    ) {
        return routeService.deleteRoute(id);
    }
    
    @Operation(
        summary = "Search for locations using geocoding",
        description = "Uses OpenRouteService Geocoding API to search for locations based on text input."
    )
    @ApiResponse(responseCode = "200", description = "Geocoding search successful")
    @GetMapping("/geocode/search")
    public ResponseEntity<String> geocodeSearch(@RequestParam String text) {
        String result = openRouteService.geocodeSearch(text);
        return ResponseEntity.ok(result);
    }

    @Operation(
        summary = "Calculate route between coordinates",
        description = "Uses OpenRouteService Routing API to calculate a route based on start and end coordinates."
    )
    @ApiResponse(responseCode = "200", description = "Route calculation successful")
    @PostMapping("/calculate-route") // Changed from /calculate to avoid conflict with existing POST /routes/calculate (which just adds to DB)
    public ResponseEntity<String> calculateRoute(@RequestBody String requestBody) {
        String result = openRouteService.calculateRoute(requestBody);
        return ResponseEntity.ok(result);
    }

    // Keeping the existing calculate endpoint for now, but it might be redundant
    @Operation(
        summary = "Calculate route (placeholder)",
        description = "Calculates a route between start and end coordinates (placeholder implementation)."
    )
    @ApiResponse(responseCode = "200", description = "Route calculated successfully")
    @PostMapping("/calculate")
    public ResponseEntity<?> calculateRoute(@Valid @RequestBody RouteRequest request) {
         Route route = new Route();
        route.setStartCoordinate(request.getStartCoordinate());
        route.setEndCoordinate(request.getEndCoordinate());
        return routeService.calculateRoute(route);
    }
}
