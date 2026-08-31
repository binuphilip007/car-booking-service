package com.velocitymotors.carbooking.model.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "vehicles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Vehicle {

    @Id
    private String vehicleId;

    public Vehicle(String vehicleId) {
        this.vehicleId = vehicleId;
    }
}
