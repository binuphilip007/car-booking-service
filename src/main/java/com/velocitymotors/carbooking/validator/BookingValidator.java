package com.velocitymotors.carbooking.validator;

import com.velocitymotors.carbooking.model.request.BookingRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Component
public class BookingValidator {

    private static final long MAX_RENTAL_DAYS = 21;

    private final VehicleValidator vehicleValidator;

    public BookingValidator(VehicleValidator vehicleValidator) {
        this.vehicleValidator = vehicleValidator;
    }

    public void validate(BookingRequest request) {
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

        vehicleValidator.validate(request.vehicleId());
    }
}