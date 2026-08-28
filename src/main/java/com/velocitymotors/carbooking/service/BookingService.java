package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.model.request.BookingRequest;
import com.velocitymotors.carbooking.model.response.BookingResponse;
import com.velocitymotors.carbooking.model.response.BookingDetailsResponse;

import java.util.List;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    List<BookingDetailsResponse> getAllBookings();
}