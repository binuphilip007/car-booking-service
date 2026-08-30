package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.model.entity.Booking;

import java.util.Optional;
import java.time.LocalDateTime;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(String bookingId);

    Optional<Booking> findByPaymentReference(String paymentId);

        List<Booking> findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
            PaymentMode paymentMode,
            BookingStatus bookingStatus,
            LocalDateTime rentalStartDate,
            Pageable pageable);

    Page<Booking> findAll(Pageable pageable);

    void deleteAll();
}