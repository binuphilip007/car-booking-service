package com.velocitymotors.carbooking.service.adapter.outbound.kafka;

import com.velocitymotors.carbooking.model.payment.BankTransferPaymentEventRequest;

public interface BankTransferPaymentEventPublisher {

    void publish(BankTransferPaymentEventRequest event);
}