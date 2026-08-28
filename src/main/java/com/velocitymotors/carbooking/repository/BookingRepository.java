package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.model.entity.Booking;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import org.springframework.data.domain.Pageable;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> findById(String bookingId);

    Optional<Booking> findByPaymentReference(String paymentId);

        List<Booking> findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
            PaymentMode paymentMode,
            BookingStatus bookingStatus,
            LocalDateTime rentalStartDate,
            Pageable pageable);

    List<Booking> findAll();

    void deleteAll();
}