package com.velocitymotors.carbooking.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Guards against double-counting a bank-transfer instalment when Kafka redelivers an event. */
@Entity
@Table(name = "processed_payment_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedPaymentEvent {

    @Id
    @Column(name = "transaction_reference")
    private String transactionReference;

    @Column(name = "booking_id", nullable = false)
    private String bookingId;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedPaymentEvent(String transactionReference, String bookingId) {
        this.transactionReference = transactionReference;
        this.bookingId = bookingId;
        this.processedAt = LocalDateTime.now();
    }
}
