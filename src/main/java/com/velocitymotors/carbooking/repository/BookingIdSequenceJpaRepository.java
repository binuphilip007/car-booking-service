package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.model.entity.BookingIdSequence;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingIdSequenceJpaRepository extends JpaRepository<BookingIdSequence, Long> {
}