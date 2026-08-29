package com.velocitymotors.carbooking.service.adapter.outbound.kafka;

import com.velocitymotors.carbooking.model.payment.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.model.payment.PublishedEventMetadata;

public interface BankTransferPaymentEventPublisher {

    PublishedEventMetadata publish(BankTransferPaymentEventRequest event);
}