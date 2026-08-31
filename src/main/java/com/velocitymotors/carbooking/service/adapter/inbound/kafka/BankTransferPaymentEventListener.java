package com.velocitymotors.carbooking.service.adapter.inbound.kafka;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.event.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.velocitymotors.carbooking.repository.ProcessedPaymentEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static java.math.BigDecimal.ZERO;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankTransferPaymentEventListener {

    private static final String TRANSACTION_DETAILS_PATTERN = "^\\S{12}\\s+\\S{10}$";
    private final BookingRepository bookingRepository;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;

    @KafkaListener(
            topics = "${bank-transfer.payment-events-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handlePaymentEvent(BankTransferPaymentEventRequest event) {
        log.debug("Received bank-transfer payment event");
        validatePaymentEvent(event);

        try {
            bookingRepository.findByPaymentReference(event.paymentId())
                    .ifPresentOrElse(booking -> {
                        log.debug("Matched bookingId={} paymentMode={} bookingStatus={}",
                            booking.getBookingId(), booking.getPaymentMode(), booking.getBookingStatus());
                        String referencedBookingId = bookingIdFrom(event.transactionDetails());
                        if (!booking.getBookingId().equals(referencedBookingId)) {
                            log.warn("Booking id mismatch for paymentId={}: transactionDetails references bookingId={} "
                                            + "but paymentReference maps to bookingId={}; event will be routed to DLT",
                                    event.paymentId(), referencedBookingId, booking.getBookingId());
                            throw mismatch(event.paymentId(),
                                    "transactionDetails references bookingId " + referencedBookingId
                                            + " but paymentReference maps to bookingId " + booking.getBookingId());
                        }
                        if (booking.getPaymentMode() != PaymentMode.BANK_TRANSFER) {
                            throw mismatch(event.paymentId(), "payment mode is not BANK_TRANSFER");
                        }
                        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
                            log.debug("Ignoring duplicate bank-transfer payment event for already confirmed "
                                            + "bookingId={} paymentId={}",
                                    booking.getBookingId(), event.paymentId());
                            return;
                        }
                        if (booking.getBookingStatus() != BookingStatus.PENDING_PAYMENT) {
                            throw mismatch(event.paymentId(), "booking is not pending payment");
                        }
                        String transactionReference = transactionReferenceFrom(event.transactionDetails());
                        if (processedPaymentEventRepository.isAlreadyProcessed(transactionReference)) {
                            log.debug("Ignoring replayed bank-transfer payment event transactionReference={} "
                                            + "for bookingId={}",
                                    transactionReference, booking.getBookingId());
                            return;
                        }
                        processedPaymentEventRepository.markProcessed(transactionReference, booking.getBookingId());

                        boolean confirmed = booking.registerPayment(event.paymentAmount());
                        bookingRepository.save(booking);
                        if (confirmed) {
                            log.debug("Confirmed bank-transfer bookingId={} using paymentId={}; "
                                            + "totalAmount={} amountPaid={}",
                                    booking.getBookingId(), event.paymentId(),
                                    booking.getTotalAmount(), booking.getAmountPaid());
                        } else {
                            log.info("Recorded partial bank-transfer payment for bookingId={} paymentId={}; "
                                            + "totalAmount={} amountPaid={} outstanding={}; booking stays {}",
                                    booking.getBookingId(), event.paymentId(),
                                    booking.getTotalAmount(), booking.getAmountPaid(),
                                    booking.outstandingAmount(), booking.getBookingStatus());
                        }
                    }, () -> {
                        log.warn("No pending bank-transfer booking matched paymentId={}",
                                event.paymentId());
                        throw mismatch(event.paymentId(), "no booking matched");
                    });
        } catch (InvalidBankTransferPaymentEventException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            log.error("Database failure while processing paymentId={}; Kafka retry will be attempted",
                    event.paymentId(), exception);
            throw exception;
        }
    }

    private InvalidBankTransferPaymentEventException mismatch(String paymentId, String reason) {
        return new InvalidBankTransferPaymentEventException(
                "Invalid bank-transfer payment event for paymentId " + paymentId + ": " + reason);
    }

    private String bookingIdFrom(String transactionDetails) {
        return transactionDetails.trim().split("\\s+")[1];
    }

    private String transactionReferenceFrom(String transactionDetails) {
        return transactionDetails.trim().split("\\s+")[0];
    }

    private void validatePaymentEvent(BankTransferPaymentEventRequest event) {
        if (event == null) {
            log.warn("Received null bank-transfer payment event; event will be routed to DLT");
            throw new InvalidBankTransferPaymentEventException("Payment event payload is missing");
        }
        if (event.paymentId() == null || event.paymentId().isBlank()) {
            log.warn("Missing paymentId in bank-transfer event; event will be routed to DLT");
            throw new InvalidBankTransferPaymentEventException("Payment id must not be blank");
        }
        if (event.senderAccountNumber() == null || event.senderAccountNumber().isBlank()) {
            log.warn("Missing senderAccountNumber for paymentId={}; event will be routed to DLT",
                    event.paymentId());
            throw new InvalidBankTransferPaymentEventException(
                    "Sender account number must not be blank for paymentId " + event.paymentId());
        }
        if (event.paymentAmount() == null) {
            log.warn("Missing paymentAmount for paymentId={}; event will be routed to DLT", event.paymentId());
            throw new InvalidBankTransferPaymentEventException(
                    "Payment amount is missing for paymentId " + event.paymentId());
        }
        if (event.paymentAmount().compareTo(ZERO) <= 0) {
            log.warn("Non-positive paymentAmount={} for paymentId={}; event will be routed to DLT",
                    event.paymentAmount(), event.paymentId());
            throw new InvalidBankTransferPaymentEventException(
                    "Payment amount must be greater than zero for paymentId " + event.paymentId()
                            + " but was " + event.paymentAmount());
        }
        if (event.transactionDetails() == null
            || !event.transactionDetails().matches(TRANSACTION_DETAILS_PATTERN)) {
                log.warn("Invalid transactionDetails for paymentId={}; event will be routed to DLT",
                    event.paymentId());
            throw new InvalidBankTransferPaymentEventException(
                    "Transaction details must contain a 12-character transaction reference and 10-character booking ID"
                            + " for paymentId " + event.paymentId() + " but was '" + event.transactionDetails() + "'");
        }
                log.debug("Validated bank-transfer payment event paymentId={}", event.paymentId());
    }

    private boolean isPendingBankTransfer(Booking booking) {
        return booking.getPaymentMode() == PaymentMode.BANK_TRANSFER
                && booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT;
    }
}