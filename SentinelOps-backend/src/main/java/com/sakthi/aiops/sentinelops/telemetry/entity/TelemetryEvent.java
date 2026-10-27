package com.sakthi.aiops.sentinelops.telemetry.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "telemetry_events",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_telemetry_producer_event",
                        columnNames = {"producer_event_id"}
                )
        }
)
public class TelemetryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "producer_event_id", nullable = false, updatable = false)
    private String producerEventId;

    @Column(name = "service_id", nullable = false)
    private UUID serviceId;

    @Column(name = "occured_at", nullable = false)
    private Instant occuredAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "trace_id", nullable = false)
    private String traceId;

    @Column(name = "corelation_id", nullable = false)
    private String correlationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(name = "schema_version", nullable = false)
    private String schemaVersion;

    protected TelemetryEvent(){}

    public TelemetryEvent(
            String producerEventId,
            UUID serviceId,
            Instant occurredAt,
            String eventType,
            String traceId,
            String correlationId,
            Map<String, Object> payload,
            String schemaVersion
    ){
        this.producerEventId = producerEventId;
        this.serviceId = serviceId;
        this.occuredAt = occurredAt;
        this.eventType = eventType;
        this.traceId = traceId;
        this.correlationId = correlationId;
        this.payload = payload;
        this.schemaVersion = schemaVersion;
        this.receivedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getProducerEventId() {
        return producerEventId;
    }

    public void setProducerEventId(String producerEventId) {
        this.producerEventId = producerEventId;
    }

    public UUID getServiceId() {
        return serviceId;
    }

    public void setServiceId(UUID serviceId) {
        this.serviceId = serviceId;
    }

    public Instant getOccuredAt() {
        return occuredAt;
    }

    public void setOccuredAt(Instant occuredAt) {
        this.occuredAt = occuredAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public void setReceivedAt(Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }

    public String getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

}
