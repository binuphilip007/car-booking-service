package com.velocitymotors.carbooking.model.response;

import com.velocitymotors.carbooking.model.entity.BookingStatus;
public record BookingResponse(String bookingId, BookingStatus bookingStatus) {
}