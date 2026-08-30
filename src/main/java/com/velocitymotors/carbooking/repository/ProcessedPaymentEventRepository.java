package com.velocitymotors.carbooking.repository;

public interface ProcessedPaymentEventRepository {

    boolean isAlreadyProcessed(String transactionReference);

    void markProcessed(String transactionReference, String bookingId);
}
