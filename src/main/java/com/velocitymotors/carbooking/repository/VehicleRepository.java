package com.velocitymotors.carbooking.repository;

public interface VehicleRepository {

    boolean existsByVehicleId(String vehicleId);

    long count();

    void save(String vehicleId);
}
