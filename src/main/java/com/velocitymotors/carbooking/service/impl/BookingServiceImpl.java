package com.velocitymotors.carbooking.service.impl;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingIdSequence;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.api.request.BookingRequest;
import com.velocitymotors.carbooking.model.api.response.BookingResponse;
import com.velocitymotors.carbooking.model.api.response.BookingDetailsResponse;
import com.velocitymotors.carbooking.service.adapter.outbound.http.CreditCardPaymentClient;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.velocitymotors.carbooking.repository.BookingIdSequenceJpaRepository;
import com.velocitymotors.carbooking.service.BookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.velocitymotors.carbooking.validator.BookingValidator;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingIdSequenceJpaRepository bookingIdSequenceJpaRepository;
    private final CreditCardPaymentClient creditCardPaymentClient;
    private final TransactionTemplate transactionTemplate;
    private final BookingValidator bookingValidator;

    @Override
    public BookingResponse createBooking(BookingRequest request) {
        bookingValidator.validate(request);

        String paymentReference = request.paymentReference().trim();

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

        BookingStatus bookingStatus = request.paymentMode() == PaymentMode.BANK_TRANSFER
            ? BookingStatus.PENDING_PAYMENT
            : BookingStatus.CONFIRMED;

        return transactionTemplate.execute(status -> persistBooking(request, paymentReference, bookingStatus));
    }

    private BookingResponse persistBooking(
            BookingRequest request,
            String paymentReference,
            BookingStatus bookingStatus) {
        String bookingId = nextBookingId();
        BigDecimal amountPaid = bookingStatus == BookingStatus.CONFIRMED
                ? request.totalAmount()
                : BigDecimal.ZERO;

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
            .totalAmount(request.totalAmount())
            .amountPaid(amountPaid)
            .build();

        bookingRepository.save(booking);
        log.debug("Persisted booking bookingId={} paymentMode={} status={} amountPaid={}",
            bookingId, request.paymentMode(), bookingStatus, amountPaid);
        return toBookingResponse(booking);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookingDetailsResponse> getAllBookings(Pageable pageable) {
        log.debug("Retrieving all bookings");
        return bookingRepository.findAll(pageable)
            .map(this::toBookingDetailsResponse);
    }

    private BookingResponse toBookingResponse(Booking booking) {
        return new BookingResponse(booking.getBookingId(), booking.getBookingStatus());
    }

    private String nextBookingId() {
        BookingIdSequence sequence = bookingIdSequenceJpaRepository.save(new BookingIdSequence());
        return "BKG%07d".formatted(sequence.getSequenceValue());
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
            booking.getBookingStatus(),
            booking.getTotalAmount(),
            booking.getAmountPaid());
    }
}