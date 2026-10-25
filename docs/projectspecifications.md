# SentinelOps: AI-Assisted Incident Intelligence & Safe Remediation Platform

## I. Problem Statement & Background

A growing software company operates multiple application services across virtual machines and containers. These systems continuously generate infrastructure metrics, application logs, distributed traces, and business-performance indicators.

Currently, these signals are monitored independently. When a production issue occurs, operators must manually correlate CPU spikes, increased API latency, application errors, and changes in business metrics to determine the underlying problem. Duplicate alerts create noise, critical incidents may be overlooked, and selecting the correct remediation action requires significant manual investigation.

The company needs an intelligent observability platform that converts disconnected telemetry into actionable incidents, prioritizes them according to business impact, and assists operators in identifying and resolving problems.

The challenge is to develop a reliable backend system using Java and Spring Boot that supports telemetry ingestion, anomaly detection, cross-layer event correlation, incident management, AI-assisted investigation, and controlled remediation.

## II. Objective

Build a runnable, production-style prototype that:

1. Ingests synthetic infrastructure and application telemetry.
2. Detects anomalies and correlates related signals into unified incidents.
3. Assigns incident priorities from P1 to P4 using transparent business-impact rules.
4. Uses AI to summarize incidents, identify plausible causes, retrieve relevant runbooks, and recommend diagnostic or remediation actions.
5. Allows authorized operators to approve safe remediation actions in a simulated environment.
6. Records incident history, operator feedback, remediation outcomes, and audit events.
7. Exposes the complete workflow through REST APIs and an interactive observability dashboard.

## III. Core Functional Requirements

### 1. Telemetry Ingestion

Build a synthetic application that continuously generates CPU utilization, memory usage, request latency, error rates, structured logs, trace identifiers, and business KPIs. Develop validated Spring Boot APIs to receive these events reliably.

### 2. Anomaly Detection and Correlation

Calculate anomaly scores using explainable statistical baselines or configurable rules. Correlate related application and infrastructure events using timestamps, service dependencies, and trace or correlation identifiers.

### 3. Incident Management

Create incidents with a defined lifecycle, priority, affected services, supporting evidence, and resolution history. Support filtering, pagination, assignment, acknowledgment, resolution, and false-positive feedback.

### 4. AI-Assisted Investigation

Integrate an LLM through a replaceable backend adapter. Generate structured incident summaries, likely-cause hypotheses, evidence references, and relevant runbook recommendations. Validate the returned output and handle model failures gracefully.

### 5. Safe Remediation

Support allowlisted simulated actions such as restarting a dummy worker or rolling back a test configuration. Enforce authorization, approval policies, precondition checks, idempotency, retries, and audit logging. AI must never execute arbitrary commands directly.

### 6. Operator Dashboard

Display current metrics, anomaly scores, historical incidents, P1–P4 priorities, AI insights, feedback, and remediation status.

## IV. Technical Requirements

The primary backend must use Java and Spring Boot, with PostgreSQL as the system of record. The implementation should demonstrate REST API design, DTO validation, exception handling, database transactions, indexing, Spring Security with JWT, automated testing, and deployment using Docker Compose.

Introduce Kafka for asynchronous telemetry processing, reliable event delivery, retries, and dead-letter handling after the basic workflow works. Use Redis only when a demonstrated requirement justifies caching, rate limiting, or temporary coordination.

Implement application metrics, structured logging, health checks, and distributed tracing.

Integrate AI through a provider-independent interface with timeout handling, structured output validation, runbook retrieval, and a fallback when the AI provider is unavailable.

## V. Definition of Done

The project is complete when:

- A synthetic multi-dimensional anomaly produces one correlated incident with a score, evidence, and a justified P1–P4 priority.
- Duplicate events and message retries do not cause duplicate side effects.
- The dashboard presents current and historical incidents and their remediation status.
- AI produces evidence-linked recommendations without becoming a dependency for core incident processing.
- Authorized operators can approve simulated remediation and inspect its audited outcome.
- Automated tests cover domain logic, API behavior, security, persistence, messaging, and important failure scenarios.
- Docker Compose starts the required local environment from a documented command.
- A reproducible five-minute demonstration shows normal operation, incident detection, AI-assisted investigation, remediation, and at least one failure-handling scenario.

All development and demonstrations must use synthetic data and must not require production credentials or real customer data.

## VI. Expected Outcome

Deliver a working, documented AI-powered observability prototype that demonstrates backend engineering beyond basic CRUD APIs. The solution must show sound database design, API contracts, transaction and concurrency handling, asynchronous processing, failure recovery, security, testing, observability, and defensible system architecture decisions.

The developer must be able to explain the architecture, its trade-offs, its failure modes, its scaling strategy, and the role of AI in the overall system.