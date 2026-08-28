package com.velocitymotors.carbooking.service.adapter.outbound.kafka;

import com.velocitymotors.carbooking.model.payment.BankTransferPaymentEventRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class KafkaBankTransferPaymentEventPublisher implements BankTransferPaymentEventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(KafkaBankTransferPaymentEventPublisher.class);

    private final KafkaTemplate<String, BankTransferPaymentEventRequest> kafkaTemplate;
    private final String topic;

    public KafkaBankTransferPaymentEventPublisher(
            KafkaTemplate<String, BankTransferPaymentEventRequest> kafkaTemplate,
            @Value("${bank-transfer.payment-events-topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Override
    public void publish(BankTransferPaymentEventRequest event) {
        kafkaTemplate.send(topic, event.paymentId(), event);
        logger.info("Submitted bank-transfer event to Kafka topic={} paymentId={}", topic, event.paymentId());
    }
}