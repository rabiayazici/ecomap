package com.tedu.seniorproject.ecomap.Repository;

import com.tedu.seniorproject.ecomap.Model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CarRepository extends JpaRepository<Car, Long> {
    List<Car> findByUserId(Long userId);
}
