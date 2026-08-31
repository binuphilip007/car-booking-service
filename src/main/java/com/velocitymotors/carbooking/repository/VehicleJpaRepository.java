package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.model.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleJpaRepository extends JpaRepository<Vehicle, String> {
}
