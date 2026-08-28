package com.velocitymotors.carbooking.service.impl;

import com.velocitymotors.carbooking.model.entity.Booking;
import com.velocitymotors.carbooking.model.entity.BookingStatus;
import com.velocitymotors.carbooking.model.entity.PaymentMode;
import com.velocitymotors.carbooking.repository.BookingRepository;
import com.velocitymotors.carbooking.service.BankTransferBookingCancellationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;

@Service
public class BankTransferBookingCancellationServiceImpl
        implements BankTransferBookingCancellationService {

    private static final Logger logger = LoggerFactory.getLogger(
            BankTransferBookingCancellationServiceImpl.class);

    private final BookingRepository bookingRepository;
        private final long cancellationMinutes;
        private final int cancellationBatchSize;

    public BankTransferBookingCancellationServiceImpl(
            BookingRepository bookingRepository,
                        @Value("${booking.cancellation.minutes:2880}") long cancellationMinutes,
                        @Value("${booking.cancellation.batch-size:100}") int cancellationBatchSize) {
        this.bookingRepository = bookingRepository;
                this.cancellationMinutes = cancellationMinutes;
                this.cancellationBatchSize = cancellationBatchSize;
    }

    @Override
    @Transactional
    public int cancelExpiredBookings() {
        LocalDateTime cutoffDate = LocalDateTime.now().plusMinutes(cancellationMinutes);
        int cancelledCount = 0;
        Pageable firstBatch = PageRequest.of(0, cancellationBatchSize);

        while (true) {
            var bookings = bookingRepository
                    .findByPaymentModeAndBookingStatusAndRentalStartDateLessThanEqual(
                            PaymentMode.BANK_TRANSFER,
                            BookingStatus.PENDING_PAYMENT,
                            cutoffDate,
                            firstBatch);
            if (bookings.isEmpty()) {
                break;
            }

            for (Booking booking : bookings) {
                booking.cancel();
                bookingRepository.save(booking);
                cancelledCount++;
                logger.info("Cancelled unpaid bank-transfer bookingId={} rentalStartDate={} cutoffDate={}",
                        booking.getBookingId(), booking.getRentalStartDate(), cutoffDate);
            }
        }

        return cancelledCount;
    }
}