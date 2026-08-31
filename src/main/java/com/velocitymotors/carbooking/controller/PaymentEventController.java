package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.model.event.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.model.event.BankTransferPaymentEventResponse;
import com.velocitymotors.carbooking.model.event.PublishedEventMetadata;
import com.velocitymotors.carbooking.service.adapter.outbound.kafka.BankTransferPaymentEventPublisher;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payment-events")
@RequiredArgsConstructor
@Slf4j
public class PaymentEventController {

    private final BankTransferPaymentEventPublisher eventPublisher;

    @PostMapping("/bank-transfer")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public BankTransferPaymentEventResponse publishBankTransferPaymentEvent(
            @Valid @RequestBody BankTransferPaymentEventRequest event) {
        log.info("Publishing bank-transfer payment event paymentId={}", event.paymentId());
        PublishedEventMetadata metadata = eventPublisher.publish(event);
        return new BankTransferPaymentEventResponse(event, metadata);
    }
}