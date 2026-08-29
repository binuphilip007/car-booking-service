package com.velocitymotors.carbooking.service.adapter.outbound.http;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.function.Supplier;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE;

@Component
@Slf4j
public class CreditCardPaymentClientImpl implements CreditCardPaymentClient {

    private static final String RESILIENCE_INSTANCE = "creditCardValidation";

    private final RestTemplate restTemplate;
    private final String paymentStatusUrl;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public CreditCardPaymentClientImpl(
            RestTemplate restTemplate,
            @Value("${credit-card-validation.base-url}") String baseUrl,
            CircuitBreakerRegistry circuitBreakerRegistry,
            RetryRegistry retryRegistry) {
        this.restTemplate = restTemplate;
        this.paymentStatusUrl = baseUrl + "/payment-status";
        this.circuitBreaker = circuitBreakerRegistry.circuitBreaker(RESILIENCE_INSTANCE);
        this.retry = retryRegistry.retry(RESILIENCE_INSTANCE);
    }

    @Override
    public PaymentStatus retrievePaymentStatus(String paymentReference) {
        Supplier<PaymentStatus> downstreamCall = () -> callDownstream(paymentReference);
        Supplier<PaymentStatus> resilientCall = CircuitBreaker.decorateSupplier(
                circuitBreaker, Retry.decorateSupplier(retry, downstreamCall));

        try {
            return resilientCall.get();
        } catch (CallNotPermittedException exception) {
            log.error("Credit-card validation circuit breaker is open; short-circuiting call", exception);
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE, "Credit-card payment validation is unavailable", exception);
        } catch (CreditCardValidationUnavailableException exception) {
            log.error("Credit-card validation unavailable after retries were exhausted", exception);
            throw new ResponseStatusException(
                    SERVICE_UNAVAILABLE, "Credit-card payment validation is unavailable", exception);
        }
    }

    private PaymentStatus callDownstream(String paymentReference) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            log.debug("Calling credit-card validation service");
            ResponseEntity<PaymentStatusResponse> responseEntity = restTemplate.postForEntity(
                    paymentStatusUrl,
                    new HttpEntity<>(Map.of("paymentReference", paymentReference), headers),
                    PaymentStatusResponse.class);
            PaymentStatusResponse response = responseEntity.getBody();
            if (response == null || response.status() == null) {
                throw new ResponseStatusException(
                        BAD_GATEWAY, "Credit-card payment validation returned no payment status");
            }
            PaymentStatus paymentStatus = parseStatus(response.status());
                log.debug(
                    "Credit-card validation completed status={} httpStatus={}",
                    paymentStatus,
                    responseEntity.getStatusCode());
            return paymentStatus;
        } catch (HttpClientErrorException.NotFound exception) {
            log.warn("Credit-card validation service did not recognise the payment reference", exception);
            throw new ResponseStatusException(
                    BAD_REQUEST, "Unknown credit-card payment reference", exception);
        } catch (HttpClientErrorException.BadRequest exception) {
            log.warn("Credit-card validation service rejected the payment reference as invalid", exception);
            throw new ResponseStatusException(
                    BAD_REQUEST, "Invalid credit-card payment reference", exception);
        } catch (HttpClientErrorException exception) {
            // undocumented client error (401/403/415/...); retrying will not change the outcome
            log.error("Credit-card validation service returned an undocumented client error", exception);
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Credit-card payment validation returned an unexpected response",
                    exception);
        } catch (HttpServerErrorException exception) {
            throw new CreditCardValidationUnavailableException(
                    "Credit-card validation service returned a server error", exception);
        } catch (RestClientException exception) {
            throw new CreditCardValidationUnavailableException(
                    "Credit-card validation service call failed", exception);
        }
    }

    private PaymentStatus parseStatus(String status) {
        try {
            return PaymentStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            log.error("Credit-card validation service returned an unsupported status={}", status);
            throw new ResponseStatusException(
                    BAD_GATEWAY,
                    "Credit-card payment validation returned an unsupported payment status",
                    exception);
        }
    }

    private record PaymentStatusResponse(String status) {
    }
}

