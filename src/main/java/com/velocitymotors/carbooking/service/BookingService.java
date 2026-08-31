package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.model.api.request.BookingRequest;
import com.velocitymotors.carbooking.model.api.response.BookingResponse;
import com.velocitymotors.carbooking.model.api.response.BookingDetailsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    Page<BookingDetailsResponse> getAllBookings(Pageable pageable);
}