package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.model.request.BookingRequest;
import com.velocitymotors.carbooking.model.response.BookingResponse;
import com.velocitymotors.carbooking.model.response.BookingDetailsResponse;
import com.velocitymotors.carbooking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private static final Logger logger = LoggerFactory.getLogger(BookingController.class);

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody BookingRequest request) {
        logger.info("Received booking request paymentMode={} vehicleId={}",
            request.paymentMode(), request.vehicleId());
        BookingResponse response = bookingService.createBooking(request);
        logger.info("Booking created bookingId={} status={}",
            response.bookingId(), response.bookingStatus());
        return response;
    }

    @GetMapping
    public List<BookingDetailsResponse> getAllBookings() {
        logger.info("Received request to list bookings");
        return bookingService.getAllBookings();
    }
}