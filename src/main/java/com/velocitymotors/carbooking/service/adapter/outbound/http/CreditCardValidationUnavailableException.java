package com.velocitymotors.carbooking.service.adapter.outbound.http;

/** Signals a transient failure calling credit-card-validation-service; safe to retry. */
public class CreditCardValidationUnavailableException extends RuntimeException {

    public CreditCardValidationUnavailableException(String message) {
        super(message);
    }

    public CreditCardValidationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
