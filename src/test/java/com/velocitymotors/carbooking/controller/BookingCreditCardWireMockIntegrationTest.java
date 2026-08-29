package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.CarBookingApplication;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CarBookingApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingCreditCardWireMockIntegrationTest {

    private static final String PAYMENT_REFERENCE = "CC123456789";
    private static final String PAYMENT_STATUS_PATH = "/host/credit-card-payment-api/payment-status";
    private static final WireMockServer wireMockServer = new WireMockServer(0);

    static {
        wireMockServer.start();
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @DynamicPropertySource
    static void configureDownstreamUrl(DynamicPropertyRegistry registry) {
        registry.add("credit-card-validation.base-url",
                () -> wireMockServer.baseUrl() + "/host/credit-card-payment-api");
    }

    @AfterAll
    static void stopWireMock() {
        wireMockServer.stop();
    }

    @BeforeEach
    void resetState() {
        wireMockServer.resetAll();
        bookingRepository.deleteAll();
    }

    @Test
    void createsConfirmedBookingWhenCreditCardPaymentIsApproved() throws Exception {
        wireMockServer.stubFor(com.github.tomakehurst.wiremock.client.WireMock.post(urlEqualTo(PAYMENT_STATUS_PATH))
                .withRequestBody(matchingJsonPath("$.paymentReference", equalTo(PAYMENT_REFERENCE)))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("{\"status\":\"APPROVED\"}")));

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreditCardBookingRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));

        wireMockServer.verify(postRequestedFor(urlEqualTo(PAYMENT_STATUS_PATH))
                .withRequestBody(matchingJsonPath("$.paymentReference", equalTo(PAYMENT_REFERENCE))));
    }

    private String validCreditCardBookingRequest() {
        return """
                {
                  "customerName": "Binu Philip",
                  "vehicleId": "VH1001",
                  "rentalStartDate": "2026-09-01T10:00:00",
                  "rentalEndDate": "2026-09-05T10:00:00",
                  "vehicleCategory": "SUV",
                  "paymentMode": "CREDIT_CARD",
                  "paymentReference": "%s"
                }
                """.formatted(PAYMENT_REFERENCE);
    }
}
