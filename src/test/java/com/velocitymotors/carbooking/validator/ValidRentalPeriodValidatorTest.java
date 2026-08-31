package com.velocitymotors.carbooking.validator;

import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import com.velocitymotors.carbooking.model.api.request.BookingRequest;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ValidRentalPeriodValidatorTest {

    private static final LocalDateTime START = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConstraintValidatorContext context;

    private ValidRentalPeriodValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ValidRentalPeriodValidator();
    }

    @Test
    void acceptsRentalPeriodWithinTwentyOneDays() {
        assertTrue(validator.isValid(request(START, START.plusDays(4)), context));
        verifyNoInteractions(context);
    }

    @Test
    void acceptsRentalPeriodOfExactlyTwentyOneDays() {
        assertTrue(validator.isValid(request(START, START.plusDays(21)), context));
        verifyNoInteractions(context);
    }

    @Test
    void skipsValidationWhenStartDateIsMissing() {
        assertTrue(validator.isValid(request(null, START.plusDays(4)), context));
        verifyNoInteractions(context);
    }

    @Test
    void skipsValidationWhenEndDateIsMissing() {
        assertTrue(validator.isValid(request(START, null), context));
        verifyNoInteractions(context);
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        assertFalse(validator.isValid(request(START, START.minusDays(1)), context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("rentalEndDate must be after rentalStartDate");
    }

    @Test
    void rejectsEndDateEqualToStartDate() {
        assertFalse(validator.isValid(request(START, START), context));

        verify(context).buildConstraintViolationWithTemplate("rentalEndDate must be after rentalStartDate");
    }

    @Test
    void rejectsRentalPeriodLongerThanTwentyOneDays() {
        assertFalse(validator.isValid(request(START, START.plusDays(21).plusMinutes(1)), context));

        verify(context).disableDefaultConstraintViolation();
        verify(context).buildConstraintViolationWithTemplate("A vehicle cannot be booked for more than 21 days");
    }

    private BookingRequest request(LocalDateTime rentalStartDate, LocalDateTime rentalEndDate) {
        return new BookingRequest(
                "Binu Philip",
                "VH1001",
                rentalStartDate,
                rentalEndDate,
                VehicleCategory.SUV,
                new BigDecimal("500.00"),
                PaymentMode.DIGITAL_WALLET,
                "WALLET123");
    }
}
