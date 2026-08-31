package com.velocitymotors.carbooking.validator;

import com.velocitymotors.carbooking.model.api.request.BookingRequest;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public final class BookingValidator {

    private static final Set<String> VALID_VEHICLE_IDS =
            Set.of("VH1001", "VH1002", "VH1003");

    private BookingValidator() {
    }

    public static void validate(BookingRequest request) {
        if (!VALID_VEHICLE_IDS.contains(request.vehicleId().trim())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid vehicleId");
        }
    }
}