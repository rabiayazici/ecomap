package com.tedu.seniorproject.ecomap.Service;

import com.tedu.seniorproject.ecomap.Model.Route;
import com.tedu.seniorproject.ecomap.Model.User;
import com.tedu.seniorproject.ecomap.Repository.RouteRepository;
import com.tedu.seniorproject.ecomap.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class RouteService {
    
    @Autowired
    private RouteRepository routeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public ResponseEntity<?> createRoute(Route route) {
        User user = userRepository.findById(route.getUser().getId())
                .orElse(null);
                
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        route.setUser(user);
        route.setDate(LocalDateTime.now());
        Route savedRoute = routeRepository.save(route);
        return ResponseEntity.ok(savedRoute);
    }
    
    public ResponseEntity<?> getRouteById(Long id) {
        return routeRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    public ResponseEntity<?> getRoutesByUserId(Long userId) {
        List<Route> routes = routeRepository.findByUserId(userId);
        return ResponseEntity.ok(routes);
    }
    
    public ResponseEntity<?> updateRoute(Long id, Route route) {
        return routeRepository.findById(id)
                .map(existingRoute -> {
                    existingRoute.setStartCoordinate(route.getStartCoordinate());
                    existingRoute.setEndCoordinate(route.getEndCoordinate());
                    return ResponseEntity.ok(routeRepository.save(existingRoute));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    public ResponseEntity<?> deleteRoute(Long id) {
        return routeRepository.findById(id)
                .map(route -> {
                    routeRepository.delete(route);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    public ResponseEntity<?> calculateRoute(Route route) {
        // Here you would implement the route calculation logic
        // For now, we'll just return the route with the current timestamp
        route.setDate(LocalDateTime.now());
        return ResponseEntity.ok(route);
    }
}
