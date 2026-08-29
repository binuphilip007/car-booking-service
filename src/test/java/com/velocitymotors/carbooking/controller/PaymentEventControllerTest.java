package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.CarBookingApplication;
import com.velocitymotors.carbooking.model.payment.PublishedEventMetadata;
import com.velocitymotors.carbooking.service.adapter.outbound.kafka.BankTransferPaymentEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CarBookingApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BankTransferPaymentEventPublisher eventPublisher;

    @Test
    void publishesBankTransferPaymentEvent() throws Exception {
        when(eventPublisher.publish(any())).thenReturn(new PublishedEventMetadata(
                "bank-transfer-payment-events", 2, 42L, "PAY-10001", Instant.parse("2026-08-28T10:15:30Z")));

        mockMvc.perform(post("/api/v1/payment-events/bank-transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                      "paymentId": "PAY-10001",
                      "senderAccountNumber": "ACC-123",
                      "paymentAmount": 500.00,
                      "transactionDetails": "TXN123456789 BKG0012345"
                                }
                                """))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.event.paymentId").value("PAY-10001"))
                .andExpect(jsonPath("$.metadata.topic").value("bank-transfer-payment-events"))
                .andExpect(jsonPath("$.metadata.partition").value(2))
                .andExpect(jsonPath("$.metadata.offset").value(42));

        verify(eventPublisher).publish(any());
    }
}