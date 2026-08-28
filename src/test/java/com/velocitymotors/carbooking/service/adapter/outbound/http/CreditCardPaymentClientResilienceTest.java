package com.velocitymotors.carbooking.service.adapter.outbound.http;

import com.velocitymotors.carbooking.CarBookingApplication;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@SpringBootTest(classes = CarBookingApplication.class)
@ActiveProfiles("test")
class CreditCardPaymentClientResilienceTest {

    private static final String PAYMENT_STATUS_URL =
            "http://localhost:9090/host/credit-card-payment-api/payment-status";

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CreditCardPaymentClient creditCardPaymentClient;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void bindMockServer() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        circuitBreakerRegistry.circuitBreaker("creditCardValidation").reset();
    }

    @Test
    void retriesTransientFailuresAndEventuallySucceeds() {
        mockServer.expect(ExpectedCount.times(2), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withServerError());
        mockServer.expect(ExpectedCount.once(), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withSuccess("{\"status\":\"APPROVED\"}", MediaType.APPLICATION_JSON));

        CreditCardPaymentClient.PaymentStatus status =
                creditCardPaymentClient.retrievePaymentStatus("CC123456789");

        assertEquals(CreditCardPaymentClient.PaymentStatus.APPROVED, status);
        mockServer.verify();
    }

    @Test
    void exhaustsRetriesAndReturnsServiceUnavailable() {
        mockServer.expect(ExpectedCount.times(3), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withServerError());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> creditCardPaymentClient.retrievePaymentStatus("CC123456789"));

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
        mockServer.verify();
    }

    @Test
    void doesNotRetryDeterministicClientErrors() {
        mockServer.expect(ExpectedCount.once(), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Payment not found\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> creditCardPaymentClient.retrievePaymentStatus("CC000000000"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Unknown credit-card payment reference", exception.getReason());
        mockServer.verify();
    }

    @Test
    void mapsDownstreamBadRequestToInvalidReference() {
        mockServer.expect(ExpectedCount.once(), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST)
                        .body("{\"error\":\"paymentReference is required\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> creditCardPaymentClient.retrievePaymentStatus("CC!!!"));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Invalid credit-card payment reference", exception.getReason());
        mockServer.verify();
    }

    @Test
    void mapsUndocumentedClientErrorToBadGateway() {
        mockServer.expect(ExpectedCount.once(), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> creditCardPaymentClient.retrievePaymentStatus("CC123456789"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("Credit-card payment validation returned an unexpected response", exception.getReason());
        mockServer.verify();
    }

    @Test
    void mapsUnsupportedPaymentStatusToBadGateway() {
        mockServer.expect(ExpectedCount.once(), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withSuccess("{\"status\":\"PENDING\"}", MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> creditCardPaymentClient.retrievePaymentStatus("CC123456789"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("Credit-card payment validation returned an unsupported payment status", exception.getReason());
        mockServer.verify();
    }

    @Test
    void mapsMissingPaymentStatusToBadGateway() {
        mockServer.expect(ExpectedCount.once(), requestTo(PAYMENT_STATUS_URL))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> creditCardPaymentClient.retrievePaymentStatus("CC123456789"));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("Credit-card payment validation returned no payment status", exception.getReason());
        mockServer.verify();
    }
}
