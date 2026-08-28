package com.velocitymotors.carbooking.model.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
public record BankTransferPaymentEventRequest(
        @NotBlank String paymentId,
        @NotBlank String senderAccountNumber,
        @NotNull BigDecimal paymentAmount,
        @NotBlank String transactionDetails) {
}