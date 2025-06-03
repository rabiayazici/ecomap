package com.tedu.seniorproject.ecomap.Repository;

import com.tedu.seniorproject.ecomap.Model.Route;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RouteRepository extends JpaRepository<Route, Long> {
    List<Route> findByUserId(Long userId);
}
