package com.velocitymotors.carbooking.repository.impl;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import com.velocitymotors.carbooking.repository.BookingJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingRepositoryImplTest {

    @Mock
    private BookingJpaRepository bookingJpaRepository;

    @InjectMocks
    private BookingRepositoryImpl bookingRepository;

    @Test
    void delegatesSaveToJpaRepository() {
        Booking booking = booking("BK1001");
        when(bookingJpaRepository.save(booking)).thenReturn(booking);

        assertSame(booking, bookingRepository.save(booking));
        verify(bookingJpaRepository).save(booking);
    }

    @Test
    void delegatesFindByIdToJpaRepository() {
        Booking booking = booking("BK1002");
        when(bookingJpaRepository.findById("BK1002")).thenReturn(Optional.of(booking));

        Optional<Booking> found = bookingRepository.findById("BK1002");

        assertTrue(found.isPresent());
        assertSame(booking, found.get());
    }

    @Test
    void returnsEmptyWhenBookingIdIsUnknown() {
        when(bookingJpaRepository.findById("BK9999")).thenReturn(Optional.empty());

        assertTrue(bookingRepository.findById("BK9999").isEmpty());
    }

    @Test
    void delegatesFindByPaymentReferenceToJpaRepository() {
        Booking booking = booking("BK1003");
        when(bookingJpaRepository.findByPaymentReference("WALLET123")).thenReturn(Optional.of(booking));

        Optional<Booking> found = bookingRepository.findByPaymentReference("WALLET123");

        assertTrue(found.isPresent());
        assertSame(booking, found.get());
    }

    @Test
    void returnsEmptyWhenPaymentReferenceIsUnknown() {
        when(bookingJpaRepository.findByPaymentReference("MISSING")).thenReturn(Optional.empty());

        assertTrue(bookingRepository.findByPaymentReference("MISSING").isEmpty());
    }

    @Test
    void delegatesExpiredBankTransferLookupToJpaRepository() {
        Booking booking = booking("BK1004");
        LocalDateTime cutoff = LocalDateTime.of(2026, 9, 1, 10, 0);
        Pageable pageable = PageRequest.of(0, 50);
        when(bookingJpaRepository.findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                PaymentMode.BANK_TRANSFER, BookingStatus.PENDING_PAYMENT, cutoff, pageable))
                .thenReturn(List.of(booking));

        List<Booking> found = bookingRepository.findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                PaymentMode.BANK_TRANSFER, BookingStatus.PENDING_PAYMENT, cutoff, pageable);

        assertEquals(List.of(booking), found);
        verify(bookingJpaRepository).findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                PaymentMode.BANK_TRANSFER, BookingStatus.PENDING_PAYMENT, cutoff, pageable);
    }

    @Test
    void delegatesFindAllToJpaRepository() {
        Pageable pageable = PageRequest.of(1, 20);
        Page<Booking> page = new PageImpl<>(List.of(booking("BK1005")), pageable, 1);
        when(bookingJpaRepository.findAll(pageable)).thenReturn(page);

        assertSame(page, bookingRepository.findAll(pageable));
    }

    @Test
    void delegatesDeleteAllToJpaRepository() {
        bookingRepository.deleteAll();

        verify(bookingJpaRepository).deleteAll();
    }

    private Booking booking(String bookingId) {
        return Booking.builder()
                .bookingId(bookingId)
                .customerName("Binu Philip")
                .vehicleId("VH1001")
                .rentalStartDate(LocalDateTime.of(2026, 9, 1, 10, 0))
                .rentalEndDate(LocalDateTime.of(2026, 9, 5, 10, 0))
                .vehicleCategory(VehicleCategory.SUV)
                .paymentMode(PaymentMode.BANK_TRANSFER)
                .paymentReference("BT" + bookingId)
                .bookingStatus(BookingStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("500.00"))
                .build();
    }
}
