package com.velocitymotors.carbooking.repository.impl;

import com.velocitymotors.carbooking.model.entity.ProcessedPaymentEvent;
import com.velocitymotors.carbooking.repository.ProcessedPaymentEventJpaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedPaymentEventRepositoryImplTest {

    @Mock
    private ProcessedPaymentEventJpaRepository processedPaymentEventJpaRepository;

    @InjectMocks
    private ProcessedPaymentEventRepositoryImpl processedPaymentEventRepository;

    @Captor
    private ArgumentCaptor<ProcessedPaymentEvent> eventCaptor;

    @Test
    void reportsEventAsAlreadyProcessedWhenTransactionReferenceExists() {
        when(processedPaymentEventJpaRepository.existsById("TX123")).thenReturn(true);

        assertTrue(processedPaymentEventRepository.isAlreadyProcessed("TX123"));
    }

    @Test
    void reportsEventAsNotProcessedWhenTransactionReferenceIsUnknown() {
        when(processedPaymentEventJpaRepository.existsById("TX999")).thenReturn(false);

        assertFalse(processedPaymentEventRepository.isAlreadyProcessed("TX999"));
    }

    @Test
    void persistsProcessedEventWithTransactionReferenceAndBookingId() {
        processedPaymentEventRepository.markProcessed("TX123", "BK1001");

        verify(processedPaymentEventJpaRepository).save(eventCaptor.capture());
        ProcessedPaymentEvent saved = eventCaptor.getValue();
        assertEquals("TX123", saved.getTransactionReference());
        assertEquals("BK1001", saved.getBookingId());
        assertNotNull(saved.getProcessedAt());
    }
}
