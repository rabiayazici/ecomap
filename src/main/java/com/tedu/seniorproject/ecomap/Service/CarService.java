package com.tedu.seniorproject.ecomap.Service;

import com.tedu.seniorproject.ecomap.Model.Car;
import com.tedu.seniorproject.ecomap.Model.User;
import com.tedu.seniorproject.ecomap.Repository.CarRepository;
import com.tedu.seniorproject.ecomap.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {
    
    @Autowired
    private CarRepository carRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public ResponseEntity<?> createCar(Car car) {
        User user = userRepository.findById(car.getUser().getId())
                .orElse(null);
                
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found");
        }
        
        car.setUser(user);
        Car savedCar = carRepository.save(car);
        return ResponseEntity.ok(savedCar);
    }
    
    public ResponseEntity<?> getCarById(Long id) {
        return carRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    public ResponseEntity<?> getCarsByUserId(Long userId) {
        List<Car> cars = carRepository.findByUserId(userId);
        return ResponseEntity.ok(cars);
    }
    
    public ResponseEntity<?> updateCar(Long id, Car car) {
        return carRepository.findById(id)
                .map(existingCar -> {
                    existingCar.setName(car.getName());
                    existingCar.setVehicle_type(car.getVehicle_type());
                    existingCar.setFuel_type(car.getFuel_type());
                    existingCar.setYear(car.getYear());
                    return ResponseEntity.ok(carRepository.save(existingCar));
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    public ResponseEntity<?> deleteCar(Long id) {
        return carRepository.findById(id)
                .map(car -> {
                    carRepository.delete(car);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
