package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.model.request.BookingRequest;
import com.velocitymotors.carbooking.model.response.BookingResponse;
import com.velocitymotors.carbooking.model.response.BookingDetailsResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

    BookingResponse createBooking(BookingRequest request);

    Page<BookingDetailsResponse> getAllBookings(Pageable pageable);
}