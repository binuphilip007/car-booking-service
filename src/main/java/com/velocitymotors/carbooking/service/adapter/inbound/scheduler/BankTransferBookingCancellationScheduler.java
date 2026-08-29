package com.velocitymotors.carbooking.service.adapter.inbound.scheduler;

import com.velocitymotors.carbooking.service.BankTransferBookingCancellationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankTransferBookingCancellationScheduler {

    private final BankTransferBookingCancellationService cancellationService;

    @Scheduled(fixedDelayString = "${booking.cancellation.fixed-delay-ms}")
    public void cancelExpiredBookings() {
        log.debug("Starting automatic bank-transfer cancellation scan");
        int cancelledCount = cancellationService.cancelExpiredBookings();
        log.debug("Automatic bank-transfer cancellation completed; cancelledCount={}", cancelledCount);
    }
}
