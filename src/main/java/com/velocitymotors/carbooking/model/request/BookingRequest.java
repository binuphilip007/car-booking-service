package com.velocitymotors.carbooking.model.request;

import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record BookingRequest(
        @NotBlank(message = "customerName is required") String customerName,
        @NotBlank(message = "vehicleId is required") String vehicleId,
        @NotNull(message = "rentalStartDate is required") LocalDateTime rentalStartDate,
        @NotNull(message = "rentalEndDate is required") LocalDateTime rentalEndDate,
        @NotNull(message = "vehicleCategory is required") VehicleCategory vehicleCategory,
        @NotNull(message = "paymentMode is required") PaymentMode paymentMode,
        @NotBlank(message = "paymentReference is required") String paymentReference) {
}