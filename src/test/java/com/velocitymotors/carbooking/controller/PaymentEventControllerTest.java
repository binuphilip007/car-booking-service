package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.CarBookingApplication;
import com.velocitymotors.carbooking.service.adapter.outbound.kafka.BankTransferPaymentEventPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = CarBookingApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PaymentEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BankTransferPaymentEventPublisher eventPublisher;

    @Test
    void publishesBankTransferPaymentEvent() throws Exception {
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
            .andExpect(jsonPath("$.paymentId").value("PAY-10001"));

        verify(eventPublisher).publish(any());
    }
}