package com.velocitymotors.carbooking.controller;

import com.velocitymotors.carbooking.CarBookingApplication;
import com.velocitymotors.carbooking.service.adapter.outbound.http.CreditCardPaymentClient;
import com.velocitymotors.carbooking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = CarBookingApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingRepository bookingRepository;

    @MockitoBean
    private CreditCardPaymentClient creditCardPaymentClient;

    @BeforeEach
    void clearBookings() {
        bookingRepository.deleteAll();
    }

    @Test
    void createsConfirmedDigitalWalletBooking() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("DIGITAL_WALLET")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingId").isString())
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));
    }

    @Test
    void createsConfirmedCashBooking() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest("CASH")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));
    }

        @Test
        void rejectsDuplicatePaymentReference() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("CASH")))
            .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("DIGITAL_WALLET")))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.error").value("paymentReference is already in use"));
        }

            @Test
            void listsAllBookings() throws Exception {
            mockMvc.perform(post("/api/v1/bookings")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(validRequest("DIGITAL_WALLET")))
                .andExpect(status().isCreated());

            mockMvc.perform(get("/api/v1/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", org.hamcrest.Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].customerName").value("Binu Philip"))
                .andExpect(jsonPath("$[0].vehicleId").value("VH1001"))
                .andExpect(jsonPath("$[0].rentalStartDate").value("2026-09-01T10:00:00"))
                .andExpect(jsonPath("$[0].rentalEndDate").value("2026-09-05T10:00:00"))
                .andExpect(jsonPath("$[0].vehicleCategory").value("SUV"))
                .andExpect(jsonPath("$[0].paymentMode").value("DIGITAL_WALLET"))
                .andExpect(jsonPath("$[0].paymentReference").value("WALLET123"))
                .andExpect(jsonPath("$[0].bookingId").isString())
                .andExpect(jsonPath("$[0].bookingStatus").value("CONFIRMED"));
            }

    @Test
    void rejectsRentalLongerThan21Days() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestWithDates("2026-09-01", "2026-09-23")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("A vehicle cannot be booked for more than 21 days"));
    }

    @Test
    void rejectsEndDateBeforeStartDate() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestWithDates("2026-09-05", "2026-09-01")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error")
                        .value("rentalEndDate must be after rentalStartDate"));
    }

    @Test
    void rejectsUnknownVehicle() throws Exception {
        String request = validRequest("DIGITAL_WALLET").replace("VH1001", "VH9999");

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid vehicleId"));
    }

    @Test
    void rejectsMalformedBookingRequest() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid-json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Malformed request body"));
    }

    @Test
        void createsConfirmedCreditCardBookingWhenPaymentIsApproved() throws Exception {
        when(creditCardPaymentClient.retrievePaymentStatus("CC123456789"))
            .thenReturn(CreditCardPaymentClient.PaymentStatus.APPROVED);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("CREDIT_CARD", "CC123456789")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.bookingStatus").value("CONFIRMED"));
        }

        @Test
        void rejectsCreditCardBookingWhenPaymentIsRejected() throws Exception {
        when(creditCardPaymentClient.retrievePaymentStatus("CC987654321"))
            .thenReturn(CreditCardPaymentClient.PaymentStatus.REJECTED);

        mockMvc.perform(post("/api/v1/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(validRequest("CREDIT_CARD", "CC987654321")))
            .andExpect(status().isPaymentRequired())
            .andExpect(jsonPath("$.error").value("Credit-card payment was not approved"));
        }

        @Test
        void createsPendingBankTransferBooking() throws Exception {
        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                            .content(validRequest("BANK_TRANSFER")))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.bookingId").isString())
                    .andExpect(jsonPath("$.bookingStatus").value("PENDING_PAYMENT"));
    }

    private String validRequest(String paymentMode) {
        return validRequest(paymentMode, "WALLET123");
        }

        private String validRequest(String paymentMode, String paymentReference) {
        return validRequestWithDates("2026-09-01", "2026-09-05")
            .replace("DIGITAL_WALLET", paymentMode)
            .replace("WALLET123", paymentReference);
    }

    private String validRequestWithDates(String startDate, String endDate) {
        return """
                {
                  "customerName": "Binu Philip",
                  "vehicleId": "VH1001",
                  "rentalStartDate": "%sT10:00:00",
                  "rentalEndDate": "%sT10:00:00",
                  "vehicleCategory": "SUV",
                  "paymentMode": "DIGITAL_WALLET",
                  "paymentReference": "WALLET123"
                }
                """.formatted(startDate, endDate);
    }
}