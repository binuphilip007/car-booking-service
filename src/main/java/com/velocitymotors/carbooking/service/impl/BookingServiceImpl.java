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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;
import java.util.List;
import com.velocitymotors.carbooking.validator.BookingValidator;

@Service
public class BookingServiceImpl implements BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingServiceImpl.class);

    private final BookingRepository bookingRepository;
    private final BookingValidator bookingValidator;
    private final CreditCardPaymentClient creditCardPaymentClient;
    private final AtomicLong bookingSequence = new AtomicLong(1);

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            BookingValidator bookingValidator,
            CreditCardPaymentClient creditCardPaymentClient) {
        this.bookingRepository = bookingRepository;
        this.bookingValidator = bookingValidator;
        this.creditCardPaymentClient = creditCardPaymentClient;
    }

    @Override
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        bookingValidator.validate(request);

        if (request.paymentMode() == PaymentMode.CREDIT_CARD) {
            logger.info("Validating credit-card payment reference");
            CreditCardPaymentClient.PaymentStatus paymentStatus =
                    creditCardPaymentClient.retrievePaymentStatus(request.paymentReference());
            if (paymentStatus != CreditCardPaymentClient.PaymentStatus.APPROVED) {
            logger.warn("Credit-card payment was not approved");
                throw new ResponseStatusException(
                        HttpStatus.PAYMENT_REQUIRED,
                        "Credit-card payment was not approved");
            }
        }

        String bookingId = "BKG%07d".formatted(bookingSequence.getAndIncrement());
        
        BookingStatus bookingStatus = request.paymentMode() == PaymentMode.BANK_TRANSFER
            ? BookingStatus.PENDING_PAYMENT
            : BookingStatus.CONFIRMED;

        Booking booking = new Booking(
                bookingId,
                request.customerName().trim(),
                request.vehicleId().trim(),
                request.rentalStartDate(),
                request.rentalEndDate(),
                request.vehicleCategory(),
                request.paymentMode(),
                request.paymentReference().trim(),
                bookingStatus);

        bookingRepository.save(booking);
        logger.info("Persisted booking bookingId={} paymentMode={} status={}",
            bookingId, request.paymentMode(), bookingStatus);
        return toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingDetailsResponse> getAllBookings() {
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