package com.velocitymotors.carbooking.validator;

import com.velocitymotors.carbooking.model.request.BookingRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Duration;
import java.time.LocalDateTime;

public class ValidRentalPeriodValidator implements ConstraintValidator<ValidRentalPeriod, BookingRequest> {

    private static final long MAX_RENTAL_DAYS = 21;

    @Override
    public boolean isValid(BookingRequest request, ConstraintValidatorContext context) {
        LocalDateTime rentalStartDate = request.rentalStartDate();
        LocalDateTime rentalEndDate = request.rentalEndDate();
        if (rentalStartDate == null || rentalEndDate == null) {
            // let @NotNull report missing dates
            return true;
        }

        if (!rentalEndDate.isAfter(rentalStartDate)) {
            addViolation(context, "rentalEndDate must be after rentalStartDate");
            return false;
        }

        Duration rentalDuration = Duration.between(rentalStartDate, rentalEndDate);
        if (rentalDuration.compareTo(Duration.ofDays(MAX_RENTAL_DAYS)) > 0) {
            addViolation(context, "A vehicle cannot be booked for more than 21 days");
            return false;
        }

        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode("rentalEndDate")
                .addConstraintViolation();
    }
}
