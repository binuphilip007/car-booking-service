package com.velocitymotors.carbooking.service.adapter.inbound.kafka;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import com.velocitymotors.carbooking.model.payment.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.velocitymotors.carbooking.repository.ProcessedPaymentEventRepository;
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

    @Mock
    private ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Test
    void confirmsPendingBankTransferWhenFullAmountIsReceived() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));

        listener().handlePaymentEvent(validEvent("PAY-10001"));

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        verify(bookingRepository).findByPaymentReference("PAY-10001");
        verify(bookingRepository).save(booking);
    }

    @Test
    void keepsBookingPendingWhenOnlyPartialAmountIsReceived() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));

        listener().handlePaymentEvent(event("PAY-10001", BigDecimal.valueOf(200)));

        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getBookingStatus());
        assertEquals(0, BigDecimal.valueOf(200).compareTo(booking.getAmountPaid()));
        assertEquals(0, BigDecimal.valueOf(300).compareTo(booking.outstandingAmount()));
        verify(bookingRepository).save(booking);
    }

    @Test
    void confirmsBookingOnceInstalmentsCoverTheTotalAmount() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));
        BankTransferPaymentEventListener listener = listener();

        listener.handlePaymentEvent(event("PAY-10001", BigDecimal.valueOf(200), "TXN123456781"));
        listener.handlePaymentEvent(event("PAY-10001", BigDecimal.valueOf(300), "TXN123456782"));

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        assertEquals(0, BigDecimal.valueOf(500).compareTo(booking.getAmountPaid()));
    }

    @Test
    void confirmsBookingWhenAmountIsOverpaid() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));

        listener().handlePaymentEvent(event("PAY-10001", BigDecimal.valueOf(600)));

        assertEquals(BookingStatus.CONFIRMED, booking.getBookingStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(booking.outstandingAmount()));
    }

    @Test
    void doesNotDoubleCountReplayedTransaction() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));
        when(processedPaymentEventRepository.isAlreadyProcessed("TXN123456789")).thenReturn(true);

        listener().handlePaymentEvent(event("PAY-10001", BigDecimal.valueOf(200)));

        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getBookingStatus());
        assertEquals(0, BigDecimal.ZERO.compareTo(booking.getAmountPaid()));
        verify(bookingRepository, never()).save(booking);
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
    void rejectsEventWhoseTransactionDetailsReferenceAnotherBooking() {
        Booking booking = booking(BookingStatus.PENDING_PAYMENT, PaymentMode.BANK_TRANSFER);
        when(bookingRepository.findByPaymentReference("PAY-10001")).thenReturn(Optional.of(booking));
        BankTransferPaymentEventRequest event = new BankTransferPaymentEventRequest(
                "PAY-10001", "ACC-123", BigDecimal.valueOf(500), "TXN123456789 BKG9999999");

        assertThrows(InvalidBankTransferPaymentEventException.class,
                () -> listener().handlePaymentEvent(event));

        assertEquals(BookingStatus.PENDING_PAYMENT, booking.getBookingStatus());
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
        return event(paymentId, BigDecimal.valueOf(500));
    }

    private BankTransferPaymentEventRequest event(String paymentId, BigDecimal paymentAmount) {
        return event(paymentId, paymentAmount, "TXN123456789");
    }

    private BankTransferPaymentEventRequest event(
            String paymentId, BigDecimal paymentAmount, String transactionReference) {
        return new BankTransferPaymentEventRequest(
                paymentId, "ACC-123", paymentAmount, transactionReference + " BKG0012345");
    }

    private BankTransferPaymentEventListener listener() {
        return new BankTransferPaymentEventListener(bookingRepository, processedPaymentEventRepository);
    }

    private Booking booking(BookingStatus status, PaymentMode paymentMode) {
        return new Booking("BKG0012345", "Binu Philip", "VH1001",
                LocalDateTime.parse("2026-09-01T10:00:00"), LocalDateTime.parse("2026-09-05T10:00:00"),
                VehicleCategory.SUV, paymentMode, "PAY-10001", status, BigDecimal.valueOf(500));
    }
}
