package com.tedu.seniorproject.ecomap.Controller;

import com.tedu.seniorproject.ecomap.Model.User;
import com.tedu.seniorproject.ecomap.Service.UserService;
import com.tedu.seniorproject.ecomap.config.JwtConfig;
import com.tedu.seniorproject.ecomap.dto.LoginRequest;
import com.tedu.seniorproject.ecomap.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {
    
    private final UserService userService;
    private final JwtConfig jwtConfig;
    
    public UserController(UserService userService, JwtConfig jwtConfig) {
        this.userService = userService;
        this.jwtConfig = jwtConfig;
    }
    
    @Operation(
        summary = "Register a new user",
        description = "Creates a new user account with the provided details"
    )
    @ApiResponse(responseCode = "200", description = "User registered successfully")
    @ApiResponse(responseCode = "400", description = "Email already exists or invalid input")
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        
        ResponseEntity<?> response = userService.registerUser(user);
        if (response.getStatusCode().is2xxSuccessful()) {
            User registeredUser = (User) response.getBody();
            String token = jwtConfig.generateToken(registeredUser);
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", registeredUser);
            responseBody.put("token", token);
            
            return ResponseEntity.ok(responseBody);
        }
        return response;
    }
    
    @Operation(
        summary = "User login",
        description = "Authenticates a user with email and password"
    )
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@Valid @RequestBody LoginRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        
        ResponseEntity<?> response = userService.loginUser(user);
        if (response.getStatusCode().is2xxSuccessful()) {
            User loggedInUser = (User) response.getBody();
            String token = jwtConfig.generateToken(loggedInUser);
            
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("user", loggedInUser);
            responseBody.put("token", token);
            
            return ResponseEntity.ok(responseBody);
        }
        return response;
    }
    
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves user details by their ID"
    )
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(
        @Parameter(description = "User ID", required = true)
        @PathVariable Long id
    ) {
        return userService.getUserById(id);
    }
    
    @Operation(
        summary = "Update user",
        description = "Updates user details by their ID"
    )
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable Long id,
        @Valid @RequestBody User user
    ) {
        return userService.updateUser(id, user);
    }
    
    @Operation(
        summary = "Delete user",
        description = "Deletes a user by their ID"
    )
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
        @Parameter(description = "User ID", required = true)
        @PathVariable Long id
    ) {
        return userService.deleteUser(id);
    }
}
