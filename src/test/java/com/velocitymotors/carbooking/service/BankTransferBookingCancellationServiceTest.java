package com.velocitymotors.carbooking.service;

import com.velocitymotors.carbooking.service.impl.BankTransferBookingCancellationServiceImpl;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import com.velocitymotors.carbooking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class BankTransferBookingCancellationServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Test
    void cancelsPendingBankTransfersAtThe48HourCutoff() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository
                .findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                        eq(PaymentMode.BANK_TRANSFER),
                        eq(BookingStatus.PENDING_PAYMENT),
                        any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(List.of(booking), List.of());

        int cancelledCount = service().cancelExpiredBookings();

        assertEquals(1, cancelledCount);
        assertEquals(BookingStatus.CANCELLED, booking.getBookingStatus());
        verify(bookingRepository).save(booking);
    }

    @Test
    void usesCurrentDatePlusTwoDaysAsCancellationCutoff() {
        service().cancelExpiredBookings();

        ArgumentCaptor<LocalDateTime> cutoffCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(bookingRepository)
                .findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                    eq(PaymentMode.BANK_TRANSFER),
                    eq(BookingStatus.PENDING_PAYMENT),
                        cutoffCaptor.capture(),
                        any(Pageable.class));

        long cutoffDifference = java.time.Duration.between(
            LocalDateTime.now(), cutoffCaptor.getValue()).toMinutes();
        assertTrue(cutoffDifference >= 48 * 60 - 1 && cutoffDifference <= 48 * 60);
    }

    @Test
    void doesNotChangeConfirmedBookings() {
        Booking booking = booking(BookingStatus.CONFIRMED, PaymentMode.BANK_TRANSFER);
        when(bookingRepository
                .findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                        eq(PaymentMode.BANK_TRANSFER),
                        eq(BookingStatus.PENDING_PAYMENT),
                        any(LocalDateTime.class),
                        any(Pageable.class)))
                .thenReturn(List.of());

        int cancelledCount = service().cancelExpiredBookings();

        assertEquals(0, cancelledCount);
        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
    }

    private BankTransferBookingCancellationService service() {
        return new BankTransferBookingCancellationServiceImpl(bookingRepository, 2880, 100);
    }

    private Booking booking(BookingStatus status, PaymentMode paymentMode) {
        return new Booking(
                "BKG00000001",
                "Binu Philip",
                "VH1001",
                LocalDateTime.now().plusHours(24),
                LocalDateTime.now().plusHours(120),
                VehicleCategory.SUV,
                paymentMode,
                "PAY-10001",
                status);
    }
}
