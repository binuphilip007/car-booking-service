package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.model.entity.ProcessedPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPaymentEventJpaRepository
        extends JpaRepository<ProcessedPaymentEvent, String> {
}
