package com.velocitymotors.carbooking.model.payment;

public record BankTransferPaymentEventResponse(
        BankTransferPaymentEventRequest event,
        PublishedEventMetadata metadata) {
}
