package com.tedu.seniorproject.ecomap.Model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cars")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Car {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank
    @Column(nullable = false)
    private String name;
    
    @NotBlank
    @Column(nullable = false)
    private String model;
    
    @NotBlank
    @Column(nullable = false)
    private String engine_type;
    
    @NotNull
    @Column(nullable = false)
    private int year;
    
    @NotBlank
    @Column(nullable = false)
    private String fuel_type;
    
    @NotNull
    @Column(nullable = false)
    private double engine_displacement;
    
    @NotBlank
    @Column(nullable = false)
    private String transmission;
    
    @NotBlank
    @Column(nullable = false)
    private String drive_type;
    
    @NotNull
    @Positive
    @Column(nullable = false)
    private double fuelConsumption;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;
}
