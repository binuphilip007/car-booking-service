package com.velocitymotors.carbooking.validator;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
public class VehicleValidator {

    private static final Set<String> VALID_VEHICLE_IDS = Set.of("VH1001", "VH1002", "VH1003");

    public void validate(String vehicleId) {
        if (!VALID_VEHICLE_IDS.contains(vehicleId.trim())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid vehicleId");
        }
    }
}