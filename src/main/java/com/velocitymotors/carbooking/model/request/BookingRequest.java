package com.velocitymotors.carbooking.model.request;

import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import com.velocitymotors.carbooking.validator.ValidRentalPeriod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@ValidRentalPeriod
public record BookingRequest(
        @NotBlank(message = "customerName is required")
        @Size(min = 2, max = 100, message = "customerName must be between 2 and 100 characters")
        String customerName,

        @NotBlank(message = "vehicleId is required")
        @Pattern(regexp = "^VH\\d{4}$", message = "vehicleId must match the format VHnnnn")
        String vehicleId,

        @NotNull(message = "rentalStartDate is required")
        @Future(message = "rentalStartDate must be in the future")
        LocalDateTime rentalStartDate,

        @NotNull(message = "rentalEndDate is required") LocalDateTime rentalEndDate,
        @NotNull(message = "vehicleCategory is required") VehicleCategory vehicleCategory,

        @NotNull(message = "totalAmount is required")
        @DecimalMin(value = "0.01", message = "totalAmount must be greater than zero")
        @Digits(integer = 10, fraction = 2, message = "totalAmount must have at most 2 decimal places")
        BigDecimal totalAmount,

        @NotNull(message = "paymentMode is required") PaymentMode paymentMode,

        @NotBlank(message = "paymentReference is required")
        @Size(min = 4, max = 30, message = "paymentReference must be between 4 and 30 characters")
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "paymentReference must be alphanumeric")
        String paymentReference) {
}