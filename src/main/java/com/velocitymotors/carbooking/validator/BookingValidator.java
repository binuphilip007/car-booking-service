package com.velocitymotors.carbooking.validator;

import com.velocitymotors.carbooking.model.api.request.BookingRequest;
import com.velocitymotors.carbooking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
@RequiredArgsConstructor
public class BookingValidator {

    private final VehicleRepository vehicleRepository;

    public void validate(BookingRequest request) {
        if (!vehicleRepository.existsByVehicleId(request.vehicleId().trim())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid vehicleId");
        }
    }
}