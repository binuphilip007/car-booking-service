package com.velocitymotors.carbooking.repository.impl;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.repository.BookingJpaRepository;
import com.velocitymotors.carbooking.repository.BookingRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import org.springframework.data.domain.Pageable;

@Repository
public class BookingRepositoryImpl implements BookingRepository {

    private final BookingJpaRepository bookingJpaRepository;

    public BookingRepositoryImpl(BookingJpaRepository bookingJpaRepository) {
        this.bookingJpaRepository = bookingJpaRepository;
    }

    @Override
    public Booking save(Booking booking) {
        return bookingJpaRepository.save(booking);
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        return bookingJpaRepository.findById(bookingId);
    }

    @Override
    public Optional<Booking> findByPaymentReference(String paymentId) {
        return bookingJpaRepository.findByPaymentReference(paymentId);
    }

    @Override
    public List<Booking> findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
            PaymentMode paymentMode,
            BookingStatus bookingStatus,
            LocalDateTime rentalStartDate,
            Pageable pageable) {
        return bookingJpaRepository.findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                paymentMode, bookingStatus, rentalStartDate, pageable);
    }

    @Override
    public List<Booking> findAll() {
        return bookingJpaRepository.findAll();
    }

    @Override
    public void deleteAll() {
        bookingJpaRepository.deleteAll();
    }
}