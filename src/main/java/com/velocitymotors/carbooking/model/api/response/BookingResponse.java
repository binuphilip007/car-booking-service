package com.velocitymotors.carbooking.model.api.response;

import com.velocitymotors.carbooking.model.entity.BookingStatus;
public record BookingResponse(String bookingId, BookingStatus bookingStatus) {
}