package com.sakthi.aiops.sentinelops.telemetry.repository;

import com.sakthi.aiops.sentinelops.telemetry.entity.TelemetryEvent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TelemetryEventRepository extends JpaRepository<TelemetryEvent, UUID> {
    Optional<TelemetryEvent> findByProducerEventId(String producerEventId);
}
