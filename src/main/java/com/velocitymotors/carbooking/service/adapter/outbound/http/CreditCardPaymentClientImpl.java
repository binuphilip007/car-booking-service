package com.velocitymotors.carbooking.service.adapter.outbound.http;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Component
public class CreditCardPaymentClientImpl implements CreditCardPaymentClient {

    private static final Logger logger = LoggerFactory.getLogger(CreditCardPaymentClientImpl.class);

    private final RestTemplate restTemplate;
    private final String paymentStatusUrl;

    public CreditCardPaymentClientImpl(
            RestTemplate restTemplate,
            @Value("${credit-card-validation.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.paymentStatusUrl = baseUrl + "/payment-status";
    }

    @Override
    public PaymentStatus retrievePaymentStatus(String paymentReference) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            logger.info("Calling credit-card validation service");
            PaymentStatusResponse response = restTemplate.postForObject(
                    paymentStatusUrl,
                    new HttpEntity<>(Map.of("paymentReference", paymentReference), headers),
                    PaymentStatusResponse.class);
            if (response == null || response.status() == null) {
                throw new IllegalStateException("Payment status was missing");
            }
            PaymentStatus paymentStatus = PaymentStatus.valueOf(response.status());
            logger.info("Credit-card validation completed status={}", paymentStatus);
            return paymentStatus;
        } catch (RestClientException | IllegalArgumentException exception) {
            logger.error("Credit-card validation service call failed", exception);
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE,
                    "Credit-card payment validation is unavailable",
                    exception);
        }
    }

    private record PaymentStatusResponse(String status) {
    }
}