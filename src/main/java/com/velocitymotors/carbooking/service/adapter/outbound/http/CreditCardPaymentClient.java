package com.velocitymotors.carbooking.service.adapter.outbound.http;

public interface CreditCardPaymentClient {

    PaymentStatus retrievePaymentStatus(String paymentReference);

    enum PaymentStatus {
        APPROVED,
        REJECTED
    }
}