package com.velocitymotors.carbooking.repository.impl;

import com.velocitymotors.carbooking.model.entity.ProcessedPaymentEvent;
import com.velocitymotors.carbooking.repository.ProcessedPaymentEventJpaRepository;
import com.velocitymotors.carbooking.repository.ProcessedPaymentEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProcessedPaymentEventRepositoryImpl implements ProcessedPaymentEventRepository {

    private final ProcessedPaymentEventJpaRepository processedPaymentEventJpaRepository;

    @Override
    public boolean isAlreadyProcessed(String transactionReference) {
        return processedPaymentEventJpaRepository.existsById(transactionReference);
    }

    @Override
    public void markProcessed(String transactionReference, String bookingId) {
        processedPaymentEventJpaRepository.save(
                new ProcessedPaymentEvent(transactionReference, bookingId));
    }
}
