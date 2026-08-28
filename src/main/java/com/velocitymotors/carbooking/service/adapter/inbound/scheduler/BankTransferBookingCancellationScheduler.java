package com.velocitymotors.carbooking.service.adapter.inbound.scheduler;

import com.velocitymotors.carbooking.service.BankTransferBookingCancellationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class BankTransferBookingCancellationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(
            BankTransferBookingCancellationScheduler.class);

    private final BankTransferBookingCancellationService cancellationService;

    public BankTransferBookingCancellationScheduler(
            BankTransferBookingCancellationService cancellationService) {
        this.cancellationService = cancellationService;
    }

    @Scheduled(fixedDelayString = "${booking.cancellation.fixed-delay-ms}")
    public void cancelExpiredBookings() {
        logger.debug("Starting automatic bank-transfer cancellation scan");
        int cancelledCount = cancellationService.cancelExpiredBookings();
        if (cancelledCount > 0) {
            logger.info("Automatic bank-transfer cancellation completed; cancelledCount={}",
                    cancelledCount);
        }
    }
}