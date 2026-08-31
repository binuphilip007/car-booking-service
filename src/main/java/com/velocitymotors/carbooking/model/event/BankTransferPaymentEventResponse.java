package com.velocitymotors.carbooking.model.event;

public record BankTransferPaymentEventResponse(
        BankTransferPaymentEventRequest event,
        PublishedEventMetadata metadata) {
}
