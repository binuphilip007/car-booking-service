package com.velocitymotors.carbooking.service.adapter.outbound.kafka;

import com.velocitymotors.carbooking.model.event.BankTransferPaymentEventRequest;
import com.velocitymotors.carbooking.model.event.PublishedEventMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@Slf4j
public class KafkaBankTransferPaymentEventPublisher implements BankTransferPaymentEventPublisher {

    private final KafkaTemplate<String, BankTransferPaymentEventRequest> kafkaTemplate;
    private final String topic;

    public KafkaBankTransferPaymentEventPublisher(
            KafkaTemplate<String, BankTransferPaymentEventRequest> kafkaTemplate,
            @Value("${bank-transfer.payment-events-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public PublishedEventMetadata publish(BankTransferPaymentEventRequest event) {
        RecordMetadata metadata = kafkaTemplate.send(topic, event.paymentId(), event)
                .join()
                .getRecordMetadata();
        log.debug("Published bank-transfer event to Kafka topic={} partition={} offset={} paymentId={}",
                metadata.topic(), metadata.partition(), metadata.offset(), event.paymentId());
        return PublishedEventMetadata.builder()
                .topic(metadata.topic())
                .partition(metadata.partition())
                .offset(metadata.offset())
                .key(event.paymentId())
                .timestamp(Instant.ofEpochMilli(metadata.timestamp()))
                .build();
    }
}