package com.velocitymotors.carbooking.repository.impl;

import com.velocitymotors.carbooking.model.entity.Vehicle;
import com.velocitymotors.carbooking.repository.VehicleJpaRepository;
import com.velocitymotors.carbooking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class VehicleRepositoryImpl implements VehicleRepository {

    private final VehicleJpaRepository vehicleJpaRepository;

    @Override
    public boolean existsByVehicleId(String vehicleId) {
        return vehicleJpaRepository.existsById(vehicleId);
    }

    @Override
    public long count() {
        return vehicleJpaRepository.count();
    }

    @Override
    public void save(String vehicleId) {
        vehicleJpaRepository.save(new Vehicle(vehicleId));
    }
}
