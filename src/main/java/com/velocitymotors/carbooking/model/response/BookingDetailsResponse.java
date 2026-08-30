package com.velocitymotors.carbooking.model.response;

import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BookingDetailsResponse(
        String customerName,
        String vehicleId,
        LocalDateTime rentalStartDate,
        LocalDateTime rentalEndDate,
        VehicleCategory vehicleCategory,
        PaymentMode paymentMode,
        String paymentReference,
        String bookingId,
        BookingStatus bookingStatus,
        BigDecimal totalAmount,
        BigDecimal amountPaid) {
}