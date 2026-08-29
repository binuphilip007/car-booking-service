package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.model.request.BookingRequest;
import com.velocitymotors.carbooking.model.response.BookingResponse;
import com.velocitymotors.carbooking.model.response.BookingDetailsResponse;
import com.velocitymotors.carbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        log.info("Received booking request paymentMode={} vehicleId={}",
            request.paymentMode(), request.vehicleId());
        BookingResponse response = bookingService.createBooking(request);
        log.info("Booking created bookingId={} status={}",
            response.bookingId(), response.bookingStatus());
        return response;
    }

    @GetMapping
    public List<BookingDetailsResponse> getAllBookings() {
        log.info("Received request to list bookings");
        return bookingService.getAllBookings();
    }
}