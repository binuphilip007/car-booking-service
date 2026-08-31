package com.velocitymotors.carbooking.service.adapter.outbound.kafka;

import com.velocitymotors.carbooking.model.event.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.model.event.PublishedEventMetadata;

public interface BankTransferPaymentEventPublisher {

    PublishedEventMetadata publish(BankTransferPaymentEventRequest event);
}