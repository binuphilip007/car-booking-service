package com.velocitymotors.carbooking.service.adapter.inbound.kafka;

import com.velocitymotors.carbooking.CarBookingApplication;
import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.model.entity.VehicleCategory;
import com.velocitymotors.carbooking.model.event.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.repository.BookingRepository;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest(classes = CarBookingApplication.class)
@ContextConfiguration(classes = BankTransferPaymentEventKafkaIntegrationTest.KafkaTestConfiguration.class)
@ActiveProfiles("test")
class BankTransferPaymentEventKafkaIntegrationTest {

    private static final String TOPIC = "bank-transfer-payment-events-it";
    private static final String PAYMENT_ID = "PAY-IT-10001";
    private static final String BOOKING_ID = "BKG0010001";

    @Container
    static final KafkaContainer KAFKA = new KafkaContainer(
            DockerImageName.parse("apache/kafka:3.8.0"));

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private KafkaTemplate<String, BankTransferPaymentEventRequest> kafkaTemplate;

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.listener.auto-startup", () -> true);
        registry.add("spring.kafka.consumer.group-id", () -> "car-booking-service-it");
        registry.add("bank-transfer.payment-events-topic", () -> TOPIC);
    }

    @BeforeEach
    void seedPendingBooking() {
        bookingRepository.deleteAll();
        bookingRepository.save(new Booking(
                BOOKING_ID,
                "Integration Test Customer",
                "VH1001",
                LocalDateTime.now().plusDays(3),
                LocalDateTime.now().plusDays(5),
                VehicleCategory.SUV,
                PaymentMode.BANK_TRANSFER,
                PAYMENT_ID,
                BookingStatus.PENDING_PAYMENT,
                BigDecimal.valueOf(500)));
    }

    @Test
    void confirmsPendingBookingWhenPaymentEventIsConsumedFromKafka() {
        kafkaTemplate.send(TOPIC, PAYMENT_ID, new BankTransferPaymentEventRequest(
                PAYMENT_ID,
                "ACC-IT-10001",
                BigDecimal.valueOf(500),
                "TXN123456789 " + BOOKING_ID));
        kafkaTemplate.flush();

        await().atMost(Duration.ofSeconds(15)).untilAsserted(() ->
                assertEquals(BookingStatus.CONFIRMED,
                        bookingRepository.findById(BOOKING_ID).orElseThrow().getBookingStatus()));
    }

    @Configuration
    static class KafkaTestConfiguration {

        @Bean
        NewTopic bankTransferPaymentEventsTopic() {
            return new NewTopic(TOPIC, 1, (short) 1);
        }
    }
}
