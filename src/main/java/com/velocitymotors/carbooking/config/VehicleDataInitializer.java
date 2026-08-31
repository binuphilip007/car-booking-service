package com.velocitymotors.carbooking.config;

import com.velocitymotors.carbooking.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class VehicleDataInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_VEHICLE_IDS = List.of("VH1001", "VH1002", "VH1003");

    private final VehicleRepository vehicleRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (vehicleRepository.count() > 0) {
            return;
        }
        DEFAULT_VEHICLE_IDS.forEach(vehicleRepository::save);
        log.info("Seeded {} vehicles", DEFAULT_VEHICLE_IDS.size());
    }
}
