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

## III. Users, Roles, and Permissions

SentinelOps supports three primary user categories: Operator, Administrator, and System / Telemetry Producer. Each category is assigned permissions according to its responsibilities. Access control must prevent unauthorized incident modifications, configuration changes, and remediation actions.

### 1. Operator

The Operator is responsible for monitoring incidents, investigating problems, coordinating resolution, and providing feedback.

**Permissions:**

- View current and historical incidents, metrics, anomaly scores, and supporting evidence.
- Filter, sort, and paginate incident records.
- Acknowledge incidents and update their status through permitted lifecycle transitions.
- Assign incidents to authorized operators and add investigation comments.
- Request AI-assisted investigation and view summaries, likely causes, and runbook recommendations.
- Submit true-positive or false-positive feedback with an optional explanation.
- Request and approve permitted simulated remediation actions, subject to authorization, approval policies, and precondition checks.
- View remediation execution results, incident history, and relevant audit records.
- Resolve incidents and record resolution details when the required conditions are satisfied.

### 2. Administrator

The Administrator is responsible for configuring and maintaining the SentinelOps platform, managing access, and controlling operational policies.

**Permissions:**

- Manage user accounts, roles, and access permissions.
- Register and configure application services and their criticality levels.
- Configure anomaly detection thresholds, baselines, and related detection rules.
- Configure service dependencies and other settings required for event correlation.
- Manage runbooks and their associated diagnostic procedures.
- Configure remediation allowlists and approval policies.
- Configure applicable telemetry retention and platform settings.
- Inspect system health, operational metrics, and audit records.
- Review incident and remediation activity for operational oversight.

Administrative access does not automatically authorize the execution or approval of every remediation action. Remediation permissions must be explicitly granted according to the applicable authorization and approval policies.

### 3. System / Telemetry Producer

The System / Telemetry Producer is a machine identity representing the synthetic telemetry generator or another authorized telemetry-producing component. It is not necessarily a human user.

**Permissions:**

- Authenticate with the telemetry ingestion API using an approved machine-authentication mechanism.
- Submit telemetry events containing the required metrics, logs, timestamps, service identifiers, trace identifiers, and optional business KPIs.
- Receive ingestion acknowledgments and validation errors.
- Retry failed submissions according to the supported retry policy.
- Submit events only for authorized service identities and within the configured ingestion limits.

The System / Telemetry Producer must not modify incidents, manage users, change platform configuration, access administrative functions, or approve and execute remediation actions.

### 4. Permission Matrix

| Capability | Operator | Administrator | System / Telemetry Producer |
|---|---|---|---|
| Submit telemetry | No | No, unless separately authorized | Yes |
| View metrics and incidents | Yes | Yes, for administration and oversight | No general access |
| Acknowledge and assign incidents | Yes | Only if separately authorized or assigned the relevant role | No |
| Add comments and submit feedback | Yes | Only if separately authorized | No |
| Request AI investigation | Yes | Only if separately authorized | No |
| Manage users and roles | No | Yes | No |
| Configure services and detection rules | No | Yes | No |
| Manage runbooks and remediation policies | No | Yes | No |
| Request remediation | Yes, subject to policy | Only if separately authorized | No |
| Approve remediation | Only when explicitly authorized | Only when explicitly authorized | No |
| Execute simulated remediation | Through the authorized backend workflow; no direct execution access | Through the authorized backend workflow; no direct execution access | No |
| View audit records | Relevant operational records | Authorized administrative audit records | No general access |

### 5. Authentication and Authorization Rules

1. Human users must authenticate before accessing protected operator or administrative functionality. The backend will use Spring Security and JSON Web Tokens (JWT) to authenticate users and enforce role-based access control.
2. Telemetry producers must authenticate as authorized machine identities. Their credentials and permissions must be separate from human user accounts.
3. The backend must enforce authorization on every protected API endpoint. Hiding a dashboard button is not sufficient to prevent unauthorized operations.
4. Role checks must be supplemented with resource-level and policy checks where required. For example, an operator must not approve a remediation action unless they are authorized for that action and all applicable approval conditions are satisfied.
5. Incident lifecycle transitions must be validated by the backend. Users cannot bypass legal transitions by directly submitting arbitrary status values.
6. Remediation requests must pass authorization, approval, allowlist, precondition, and idempotency checks before execution.
7. Administrative configuration changes, incident state changes, approval decisions, and remediation attempts must be recorded in the audit trail, with the actor, operation, target, timestamp, and outcome where applicable.
8. The system must follow the principle of least privilege: every user or machine identity receives only the permissions necessary for its assigned responsibilities.

These permissions define the initial authorization model. Additional roles or finer-grained permissions may be introduced if the implementation demonstrates a genuine need for them.

## IV. Core Functional Requirements

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

## V. Technical Requirements

The primary backend must use Java and Spring Boot, with PostgreSQL as the system of record. The implementation should demonstrate REST API design, DTO validation, exception handling, database transactions, indexing, Spring Security with JWT, automated testing, and deployment using Docker Compose.

Introduce Kafka for asynchronous telemetry processing, reliable event delivery, retries, and dead-letter handling after the basic workflow works. Use Redis only when a demonstrated requirement justifies caching, rate limiting, or temporary coordination.

Implement application metrics, structured logging, health checks, and distributed tracing.

Integrate AI through a provider-independent interface with timeout handling, structured output validation, runbook retrieval, and a fallback when the AI provider is unavailable.

## VI. Definition of Done

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

## VII. Expected Outcome

Deliver a working, documented AI-powered observability prototype that demonstrates backend engineering beyond basic CRUD APIs. The solution must show sound database design, API contracts, transaction and concurrency handling, asynchronous processing, failure recovery, security, testing, observability, and defensible system architecture decisions.

The developer must be able to explain the architecture, its trade-offs, its failure modes, its scaling strategy, and the role of AI in the overall system.