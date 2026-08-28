package com.velocitymotors.carbooking.service.adapter.inbound.kafka;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import com.velocitymotors.carbooking.model.payment.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.repository.BookingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankTransferPaymentEventListenerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Test
    void confirmsPendingBankTransferUsingPaymentId() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));

        listener().handlePaymentEvent(validEvent("PAY-10001"));

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        verify(bookingRepository).findByPaymentReference("PAY-10001");
        verify(bookingRepository).save(booking);
    }

    @Test
    void rejectsUnknownPaymentIdForDltRecovery() {
        when(bookingRepository.findByPaymentReference("PAY-UNKNOWN")).thenReturn(Optional.empty());

        assertThrows(InvalidBankTransferPaymentEventException.class,
                () -> listener().handlePaymentEvent(validEvent("PAY-UNKNOWN")));

        verify(bookingRepository).findByPaymentReference("PAY-UNKNOWN");
        verify(bookingRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void acceptsDuplicateEventForAlreadyConfirmedBankTransfer() {
        Booking booking = booking(BookingStatus.CONFIRMED, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));

        listener().handlePaymentEvent(validEvent("PAY-10001"));

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void ignoresFullPaymentForNonBankTransferBooking() {
        Booking booking = booking(BookingStatus.CONFIRMED, PaymentMode.CASH);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));

        assertThrows(InvalidBankTransferPaymentEventException.class,
            () -> listener().handlePaymentEvent(validEvent("PAY-10001")));

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        verify(bookingRepository, never()).save(booking);
    }

    @Test
    void rejectsNonPositiveAmount() {
        BankTransferPaymentEventRequest invalidEvent = new BankTransferPaymentEventRequest(
                "PAY-10001", "ACC-123", BigDecimal.ZERO, "TXN123456789 BKG0012345");

        assertThrows(InvalidBankTransferPaymentEventException.class,
                () -> listener().handlePaymentEvent(invalidEvent));
        verify(bookingRepository, never()).findByPaymentReference("PAY-10001");
    }

    @Test
    void rejectsInvalidTransactionDetails() {
        BankTransferPaymentEventRequest invalidEvent = new BankTransferPaymentEventRequest(
                "PAY-10001", "ACC-123", BigDecimal.valueOf(500), "INVALID");

        assertThrows(InvalidBankTransferPaymentEventException.class,
                () -> listener().handlePaymentEvent(invalidEvent));
        verify(bookingRepository, never()).findByPaymentReference("PAY-10001");
    }

    private BankTransferPaymentEventRequest validEvent(String paymentId) {
        return new BankTransferPaymentEventRequest(
                paymentId, "ACC-123", BigDecimal.valueOf(500),
                "TXN123456789 BKG0012345");
    }

    private BankTransferPaymentEventListener listener() {
        return new BankTransferPaymentEventListener(bookingRepository);
    }

    private Booking booking(BookingStatus status, PaymentMode paymentMode) {
        return new Booking("BKG00000001", "Binu Philip", "VH1001",
                LocalDateTime.parse("2026-09-01T10:00:00"), LocalDateTime.parse("2026-09-05T10:00:00"),
                VehicleCategory.SUV, paymentMode, "PAY-10001", status);
    }
}
