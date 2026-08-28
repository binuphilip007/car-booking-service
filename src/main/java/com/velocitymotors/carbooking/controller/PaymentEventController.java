package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.model.payment.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.service.adapter.outbound.kafka.BankTransferPaymentEventPublisher;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/v1/payment-events")
public class PaymentEventController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentEventController.class);

    private final BankTransferPaymentEventPublisher eventPublisher;

    public PaymentEventController(BankTransferPaymentEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    @PostMapping("/bank-transfer")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BankTransferPaymentEventRequest publishBankTransferPaymentEvent(
            @Valid @RequestBody BankTransferPaymentEventRequest event) {
        logger.info("Publishing bank-transfer payment event paymentId={}", event.paymentId());
        eventPublisher.publish(event);
        return event;
    }
}