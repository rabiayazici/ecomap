package com.tedu.seniorproject.ecomap.Controller;

import com.tedu.seniorproject.ecomap.Model.Car;
import com.tedu.seniorproject.ecomap.Model.User;
import com.tedu.seniorproject.ecomap.Service.CarService;
import com.tedu.seniorproject.ecomap.Service.UserService;
import com.tedu.seniorproject.ecomap.dto.CarRequest;
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
@RequestMapping("/api/cars")
@Tag(name = "Car Management", description = "APIs for managing cars")
public class CarController {
    
    private final CarService carService;
    private final UserService userService;
    
    public CarController(CarService carService, UserService userService) {
        this.carService = carService;
        this.userService = userService;
    }
    
    @Operation(
        summary = "Create a new car",
        description = "Creates a new car for the authenticated user"
    )
    @ApiResponse(responseCode = "200", description = "Car created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping
    public ResponseEntity<?> createCar(@Valid @RequestBody CarRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        
        User user = userService.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Car car = new Car();
        car.setName(request.getName());
        car.setModel(request.getModel());
        car.setFuelConsumption(request.getFuelConsumption());
        car.setUser(user);
        
        return carService.createCar(car);
    }
    
    @Operation(
        summary = "Get car by ID",
        description = "Retrieves car details by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Car found")
    @ApiResponse(responseCode = "404", description = "Car not found")
    @GetMapping("/{id}")
    public ResponseEntity<?> getCarById(
        @Parameter(description = "Car ID", required = true)
        @PathVariable Long id
    ) {
        return carService.getCarById(id);
    }
    
    @Operation(
        summary = "Get cars by user ID",
        description = "Retrieves all cars belonging to a specific user"
    )
    @ApiResponse(responseCode = "200", description = "Cars found")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCarsByUserId(
        @Parameter(description = "User ID", required = true)
        @PathVariable Long userId
    ) {
        return carService.getCarsByUserId(userId);
    }
    
    @Operation(
        summary = "Update car",
        description = "Updates car details by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Car updated successfully")
    @ApiResponse(responseCode = "404", description = "Car not found")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateCar(
        @Parameter(description = "Car ID", required = true)
        @PathVariable Long id,
        @Valid @RequestBody CarRequest request
    ) {
        Car car = new Car();
        car.setName(request.getName());
        car.setModel(request.getModel());
        car.setFuelConsumption(request.getFuelConsumption());
        return carService.updateCar(id, car);
    }
    
    @Operation(
        summary = "Delete car",
        description = "Deletes a car by its ID"
    )
    @ApiResponse(responseCode = "200", description = "Car deleted successfully")
    @ApiResponse(responseCode = "404", description = "Car not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCar(
        @Parameter(description = "Car ID", required = true)
        @PathVariable Long id
    ) {
        return carService.deleteCar(id);
    }
}
