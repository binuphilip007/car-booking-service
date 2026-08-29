package com.velocitymotors.carbooking.validator;

import com.velocitymotors.carbooking.model.request.BookingRequest;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

public final class BookingValidator {

    private static final long MAX_RENTAL_DAYS = 21;
    private static final Set<String> VALID_VEHICLE_IDS =
            Set.of("VH1001", "VH1002", "VH1003");

    private BookingValidator() {
    }

    public static void validate(BookingRequest request) {
        if (!request.rentalStartDate().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "rentalStartDate must be in the future");
        }

        if (!request.rentalEndDate().isAfter(request.rentalStartDate())) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "rentalEndDate must be after rentalStartDate");
        }

        java.time.Duration rentalDuration = java.time.Duration.between(
                request.rentalStartDate(), request.rentalEndDate());
        if (rentalDuration.compareTo(java.time.Duration.ofDays(MAX_RENTAL_DAYS)) > 0) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A vehicle cannot be booked for more than 21 days");
        }

        if (!VALID_VEHICLE_IDS.contains(request.vehicleId().trim())) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid vehicleId");
        }
    }
}