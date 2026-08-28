package com.velocitymotors.carbooking.service.adapter.inbound.kafka;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.payment.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.repository.BookingRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.math.BigDecimal.ZERO;

@Component
public class BankTransferPaymentEventListener {

    private static final String TRANSACTION_DETAILS_PATTERN = "^\\S{12}\\s+\\S{10}$";
    private static final Logger logger = LoggerFactory.getLogger(BankTransferPaymentEventListener.class);

    private final BookingRepository bookingRepository;

    public BankTransferPaymentEventListener(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    @KafkaListener(
            topics = "${bank-transfer.payment-events-topic}",
            groupId = "${spring.kafka.consumer.group-id}")
    @Transactional
    public void handlePaymentEvent(BankTransferPaymentEventRequest event) {
        validatePaymentEvent(event);

        try {
            bookingRepository.findByPaymentReference(event.paymentId())
                    .ifPresentOrElse(booking -> {
                        if (booking.getPaymentMode() != PaymentMode.BANK_TRANSFER) {
                            throw mismatch(event.paymentId(), "payment mode is not BANK_TRANSFER");
                        }
                        if (booking.getBookingStatus() == BookingStatus.CONFIRMED) {
                            logger.info("Ignoring duplicate bank-transfer payment event for already confirmed "
                                            + "bookingId={} paymentId={}",
                                    booking.getBookingId(), event.paymentId());
                            return;
                        }
                        if (booking.getBookingStatus() != BookingStatus.PENDING_PAYMENT) {
                            throw mismatch(event.paymentId(), "booking is not pending payment");
                        }
                        booking.confirm();
                        bookingRepository.save(booking);
                        logger.info("Confirmed bank-transfer bookingId={} using paymentId={}",
                                booking.getBookingId(), event.paymentId());
                    }, () -> {
                        logger.warn("No pending bank-transfer booking matched paymentId={}",
                                event.paymentId());
                        throw mismatch(event.paymentId(), "no booking matched");
                    });
        } catch (InvalidBankTransferPaymentEventException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            logger.error("Database failure while processing paymentId={}; Kafka retry will be attempted",
                    event.paymentId(), exception);
            throw exception;
        }
    }

    private InvalidBankTransferPaymentEventException mismatch(String paymentId, String reason) {
        return new InvalidBankTransferPaymentEventException(
                "Invalid bank-transfer payment event for paymentId " + paymentId + ": " + reason);
    }

    private void validatePaymentEvent(BankTransferPaymentEventRequest event) {
        if (event == null
            || event.paymentId() == null
            || event.paymentId().isBlank()
            || event.senderAccountNumber() == null
            || event.senderAccountNumber().isBlank()
                || event.paymentAmount() == null
                || event.paymentAmount().compareTo(ZERO) <= 0) {
            logger.warn("Invalid bank-transfer event amount or paymentId; event will be routed to DLT");
            throw new InvalidBankTransferPaymentEventException(
                    "Payment amount must be greater than zero");
        }
        if (event.transactionDetails() == null
            || !event.transactionDetails().matches(TRANSACTION_DETAILS_PATTERN)) {
            logger.warn("Invalid transaction details for paymentId={}; event will be routed to DLT",
                    event.paymentId());
            throw new InvalidBankTransferPaymentEventException(
                    "Transaction details must contain a 12-character transaction reference and 10-character booking ID");
        }
                logger.debug("Validated bank-transfer payment event paymentId={}", event.paymentId());
    }

    private boolean isPendingBankTransfer(Booking booking) {
        return booking.getPaymentMode() == PaymentMode.BANK_TRANSFER
                && booking.getBookingStatus() == BookingStatus.PENDING_PAYMENT;
    }
}