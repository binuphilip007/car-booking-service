package com.velocitymotors.carbooking.service.impl;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.request.BookingRequest;
import com.velocitymotors.carbooking.model.response.BookingResponse;
import com.velocitymotors.carbooking.model.response.BookingDetailsResponse;
import com.velocitymotors.carbooking.service.adapter.outbound.http.CreditCardPaymentClient;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.velocitymotors.carbooking.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import com.velocitymotors.carbooking.validator.BookingValidator;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final CreditCardPaymentClient creditCardPaymentClient;
    private final AtomicLong bookingSequence = new AtomicLong(1);

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        BookingValidator.validate(request);

        String paymentReference = request.paymentReference().trim();
        if (bookingRepository.existsByPaymentReference(paymentReference)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "paymentReference is already in use");
        }

        if (request.paymentMode() == PaymentMode.CREDIT_CARD) {
            log.debug("Validating credit-card payment reference");
            CreditCardPaymentClient.PaymentStatus paymentStatus =
                    creditCardPaymentClient.retrievePaymentStatus(paymentReference);
            if (paymentStatus != CreditCardPaymentClient.PaymentStatus.APPROVED) {
            log.warn("Credit-card payment was not approved");
                throw new ResponseStatusException(
                        HttpStatus.PAYMENT_REQUIRED,
                        "Credit-card payment was not approved");
            }
        }

        String bookingId = "BKG%07d".formatted(bookingSequence.getAndIncrement());
        
        BookingStatus bookingStatus = request.paymentMode() == PaymentMode.BANK_TRANSFER
            ? BookingStatus.PENDING_PAYMENT
            : BookingStatus.CONFIRMED;

        Booking booking = Booking.builder()
            .bookingId(bookingId)
            .customerName(request.customerName().trim())
            .vehicleId(request.vehicleId().trim())
            .rentalStartDate(request.rentalStartDate())
            .rentalEndDate(request.rentalEndDate())
            .vehicleCategory(request.vehicleCategory())
            .paymentMode(request.paymentMode())
            .paymentReference(paymentReference)
            .bookingStatus(bookingStatus)
            .build();

        bookingRepository.save(booking);
        log.debug("Persisted booking bookingId={} paymentMode={} status={}",
            bookingId, request.paymentMode(), bookingStatus);
        return toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> getAllBookings() {
        log.debug("Retrieving all bookings");
        return bookingRepository.findAll().stream()
            .map(this::toBookingDetailsResponse)
                .toList();
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return new BookingResponse(booking.getBookingId(), booking.getBookingStatus());
    }

    private BookingDetailsResponse toBookingDetailsResponse(Booking booking) {
        return new BookingDetailsResponse(
            booking.getCustomerName(),
            booking.getVehicleId(),
            booking.getRentalStartDate(),
            booking.getRentalEndDate(),
            booking.getVehicleCategory(),
            booking.getPaymentMode(),
            booking.getPaymentReference(),
            booking.getBookingId(),
            booking.getBookingStatus());
    }
}