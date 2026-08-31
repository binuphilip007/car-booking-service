package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.model.api.request.BookingRequest;
import com.velocitymotors.carbooking.model.api.response.BookingResponse;
import com.velocitymotors.carbooking.model.api.response.BookingDetailsResponse;
import com.velocitymotors.carbooking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingController {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

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
    public Page<BookingDetailsResponse> getAllBookings(Pageable pageable) {
        log.info("Received request to list bookings");
        return bookingService.getAllBookings(boundedPageable(pageable));
    }

    private Pageable boundedPageable(Pageable pageable) {
        int page = pageable.isPaged() ? pageable.getPageNumber() : 0;
        int size = pageable.isPaged() ? pageable.getPageSize() : DEFAULT_PAGE_SIZE;
        return PageRequest.of(page, Math.min(size, MAX_PAGE_SIZE), pageable.getSort());
    }
}