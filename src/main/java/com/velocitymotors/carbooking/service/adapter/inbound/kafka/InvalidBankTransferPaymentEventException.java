package com.velocitymotors.carbooking.service.adapter.inbound.kafka;

public class InvalidBankTransferPaymentEventException extends RuntimeException {

    public InvalidBankTransferPaymentEventException(String message) {
        super(message);
    }

    public InvalidBankTransferPaymentEventException(String message, Throwable cause) {
        super(message, cause);
    }
}