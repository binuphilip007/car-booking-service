package com.velocitymotors.carbooking.repository;

import com.velocitymotors.carbooking.model.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import org.springframework.data.domain.Pageable;

public interface BookingJpaRepository extends JpaRepository<Booking, String> {

	Optional<Booking> findByPaymentReference(String paymentReference);

	boolean existsByPaymentReference(String paymentReference);

	List<Booking> findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
			PaymentMode paymentMode,
			BookingStatus bookingStatus,
			LocalDateTime rentalStartDate,
			Pageable pageable);
}