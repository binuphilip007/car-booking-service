package com.velocitymotors.carbooking.model.event;

import lombok.Builder;

import java.time.Instant;

@Builder
public record PublishedEventMetadata(
        String topic,
        int partition,
        long offset,
        String key,
        Instant timestamp) {
}
