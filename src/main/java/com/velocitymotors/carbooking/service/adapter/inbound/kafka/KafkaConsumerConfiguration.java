package com.velocitymotors.carbooking.service.adapter.inbound.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class KafkaConsumerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerConfiguration.class);

    @Bean
    CommonErrorHandler kafkaErrorHandler(
            KafkaTemplate<Object, Object> kafkaTemplate,
            @Value("${bank-transfer.payment-events-topic}") String topic) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, exception) -> {
                    logger.error("Routing failed Kafka record to DLT topic={} partition={} offset={}",
                        record.topic() + ".DLT", record.partition(), record.offset(), exception);
                    return new org.apache.kafka.common.TopicPartition(
                        topic + ".DLT",
                        record.partition());
                });

        ExponentialBackOff backOff = new ExponentialBackOff(1_000L, 2.0);
        backOff.setMaxElapsedTime(30_000L);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);
        errorHandler.addNotRetryableExceptions(InvalidBankTransferPaymentEventException.class);
        errorHandler.setRetryListeners((record, exception, deliveryAttempt) -> logger.warn(
            "Kafka processing failed for topic={} partition={} offset={} attempt={}",
            record.topic(), record.partition(), record.offset(), deliveryAttempt, exception));
        return errorHandler;
    }
}