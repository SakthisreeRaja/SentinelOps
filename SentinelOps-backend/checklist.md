# SENTINELOPS — COMPLETE PROJECT DEVELOPMENT WORKFLOW All checklist

**Project:** AI-Assisted Incident Intelligence & Safe Remediation Platform

**Primary backend:** Java + Spring Boot
**Database:** PostgreSQL
**AI:** LLM API + Spring AI
**Messaging:** Kafka
**Deployment:** Docker + Docker Compose

## DEVELOPMENT CHECKLIST

### Stage 1 — Planning and System Design

- [ ] 1\. Define the requirements and scope of SentinelOps.
- [ ] 2\. Identify users, roles, and their permissions.
- [ ] 3\. Design the high-level architecture using diagrams.net.
- [ ] 4\. Design the database ER diagram.
- [ ] 5\. Design REST API contracts using OpenAPI.
- [ ] 6\. Define incident priorities (P1–P4), incident lifecycle, and anomaly detection rules.
- [ ] 7\. Decide the initial modular monolith architecture and document important design decisions.

### Stage 2 — Backend Project Setup

- [ ] 8\. Set up Java, Maven, Git, IDE, and Spring Boot.
- [ ] 9\. Add Spring Web, Spring Data JPA, Validation, Spring Security, PostgreSQL Driver, Actuator, and testing dependencies.
- [ ] 10\. Configure development and test profiles using environment variables.
- [ ] 11\. Configure PostgreSQL and database migrations using Flyway.
- [ ] 12\. Create packages for controllers, services, repositories, entities, DTOs, configuration, and exceptions.
- [ ] 13\. Implement global exception handling, request validation, structured logging, and OpenAPI documentation.
- [ ] 14\. Create a health endpoint and verify that the application starts successfully.

### Stage 3 — Database and Core Backend APIs

- [ ] 15\. Create database entities for users, monitored services, telemetry events, incidents, incident history, AI analyses, remediation actions, and audit logs.
- [ ] 16\. Implement repositories using Spring Data JPA and Hibernate.
- [ ] 17\. Implement CRUD REST APIs for monitored services.
- [ ] 18\. Implement incident creation, retrieval, updating, assignment, acknowledgment, and resolution APIs.
- [ ] 19\. Add pagination, filtering, sorting, and historical incident search.
- [ ] 20\. Add database constraints, indexes, and transaction boundaries.
- [ ] 21\. Implement incident state transitions and duplicate-incident prevention.
- [ ] 22\. Test APIs and persistence using JUnit, Mockito, and Spring Boot Test.

### Stage 4 — Authentication and Authorization

- [ ] 23\. Implement user registration or controlled administrator-created accounts.
- [ ] 24\. Implement secure password hashing.
- [ ] 25\. Configure Spring Security and JWT authentication.
- [ ] 26\. Implement role-based authorization for ADMIN, OPERATOR, and VIEWER.
- [ ] 27\. Add resource-level authorization and protect sensitive endpoints.
- [ ] 28\. Handle expired tokens, invalid credentials, forbidden requests, and secure error responses.
- [ ] 29\. Test authentication and authorization rules.

### Stage 5 — Synthetic Application and Telemetry

- [ ] 30\. Build a dummy application using Spring Boot that generates synthetic application and infrastructure telemetry.
- [ ] 31\. Generate CPU usage, memory usage, request latency, error rates, structured logs, and business metrics.
- [ ] 32\. Define telemetry event schemas, event IDs, timestamps, service IDs, and correlation IDs.
- [ ] 33\. Implement REST APIs for telemetry ingestion.
- [ ] 34\. Validate incoming events and store them in PostgreSQL.
- [ ] 35\. Handle malformed, duplicate, delayed, and out-of-order events.
- [ ] 36\. Add synthetic failure scenarios such as CPU spikes, increased latency, and application errors.
- [ ] 37\. Test the complete telemetry ingestion workflow.

### Stage 6 — Anomaly Detection and Incident Correlation

- [ ] 38\. Implement configurable threshold-based anomaly detection.
- [ ] 39\. Implement statistical anomaly scoring using moving averages or standard-deviation-based methods.
- [ ] 40\. Correlate related anomalies using service identifiers, time windows, and trace or correlation IDs.
- [ ] 41\. Assign incident priorities using business-impact rules.
- [ ] 42\. Store anomaly evidence, scores, detection timestamps, and incident relationships.
- [ ] 43\. Prevent duplicate incidents using fingerprints, database constraints, and transaction-safe logic.
- [ ] 44\. Implement operator feedback for true positives and false positives.
- [ ] 45\. Test anomaly detection, correlation, priority assignment, and false-positive scenarios.

### Stage 7 — Docker and Kafka Integration

- [ ] 46\. Learn Docker fundamentals and create Docker Compose configuration for PostgreSQL and Kafka.
- [ ] 47\. Learn Kafka topics, partitions, producers, consumers, consumer groups, and offsets.
- [ ] 48\. Add Spring Kafka to the Spring Boot application.
- [ ] 49\. Publish telemetry events to Kafka topics.
- [ ] 50\. Implement consumers for asynchronous anomaly detection and incident processing.
- [ ] 51\. Add retry policies, backoff, and dead-letter topics.
- [ ] 52\. Implement idempotent consumers and duplicate-message handling.
- [ ] 53\. Implement the transactional outbox pattern using PostgreSQL and Kafka for reliable event publication.
- [ ] 54\. Test consumer crashes, message retries, duplicate events, and temporary broker unavailability.

### Stage 8 — AI-Powered Incident Investigation

- [ ] 55\. Choose an LLM provider and integrate it through Spring AI or a dedicated provider adapter.
- [ ] 56\. Create an AI investigation service separate from core incident-management logic.
- [ ] 57\. Send incident metadata and relevant telemetry to the model.
- [ ] 58\. Generate incident summaries, likely-cause hypotheses, diagnostic suggestions, and evidence references.
- [ ] 59\. Validate AI responses using structured output schemas.
- [ ] 60\. Store AI analysis results and their processing status in PostgreSQL.
- [ ] 61\. Implement timeout handling, rate-limit handling, and graceful fallback when the model is unavailable.
- [ ] 62\. Create a collection of synthetic operational runbooks.
- [ ] 63\. Implement runbook retrieval using keyword or metadata search.
- [ ] 64\. Add RAG using embeddings and a vector store when semantic retrieval is needed.
- [ ] 65\. Protect against prompt injection, unsupported claims, and untrusted log instructions.
- [ ] 66\. Evaluate AI results using predefined incidents and expected evidence.

### Stage 9 — Safe Remediation and Auditing

- [ ] 67\. Define an allowlist of simulated remediation actions.
- [ ] 68\. Implement REST APIs to request and inspect remediation actions.
- [ ] 69\. Add role checks, approval workflows, and action precondition validation.
- [ ] 70\. Implement a simulated remediation executor for the dummy application.
- [ ] 71\. Add execution status tracking, idempotency, and bounded retries.
- [ ] 72\. Implement rollback or compensation simulation where appropriate.
- [ ] 73\. Store audit records for requests, approvals, execution, and outcomes.
- [ ] 74\. Test unauthorized actions, failed execution, duplicate requests, and approval failures.

### Stage 10 — Frontend Dashboard

- [ ] 75\. Set up React or Angular.
- [ ] 76\. Implement login and authenticated API communication.
- [ ] 77\. Build the application health dashboard with real-time or periodically refreshed metrics.
- [ ] 78\. Build the incident list with priority, status, date, and service filters.
- [ ] 79\. Build incident details with telemetry evidence, incident history, and assignments.
- [ ] 80\. Display AI summaries, hypotheses, supporting evidence, and recommendations.
- [ ] 81\. Build remediation approval, execution-status, and audit-history views.
- [ ] 82\. Add loading, error, empty-state, and historical-data views.

### Stage 11 — Testing and Reliability

- [ ] 83\. Complete unit tests for business logic using JUnit and Mockito.
- [ ] 84\. Write integration tests using Spring Boot Test and Testcontainers.
- [ ] 85\. Test REST APIs, JWT security, validation, and authorization.
- [ ] 86\. Test concurrent incident creation and duplicate event processing.
- [ ] 87\. Test Kafka retries, dead-letter handling, and outbox recovery.
- [ ] 88\. Test AI provider failures and invalid model responses.
- [ ] 89\. Test remediation approvals, execution failures, and audit records.
- [ ] 90\. Run end-to-end tests covering telemetry through remediation.
- [ ] 91\. Perform load testing using k6 or JMeter.
- [ ] 92\. Optimize PostgreSQL queries, indexes, and connection pooling based on measurements.

### Stage 12 — Observability and Production Readiness

- [ ] 93\. Configure Spring Boot Actuator health and readiness endpoints.
- [ ] 94\. Add Micrometer metrics and Prometheus collection.
- [ ] 95\. Add OpenTelemetry distributed tracing.
- [ ] 96\. Add structured logs and request correlation IDs.
- [ ] 97\. Monitor API latency, errors, ingestion throughput, Kafka consumer lag, and AI failures.
- [ ] 98\. Define operational alerts and recovery procedures.
- [ ] 99\. Test behavior when PostgreSQL, Kafka, or the AI provider becomes unavailable.

### Stage 13 — Deployment and Delivery

- [ ] 100\. Create a Dockerfile for the Spring Boot backend.
- [ ] 101\. Finalize Docker Compose for the backend, PostgreSQL, Kafka, and dummy application.
- [ ] 102\. Externalize configuration and secrets.
- [ ] 103\. Configure GitHub Actions for automated builds, tests, and container image creation.
- [ ] 104\. Deploy to a test environment or cloud platform if available.
- [ ] 105\. Verify database migrations, health checks, persistent storage, and application startup.
- [ ] 106\. Document deployment and recovery procedures.

### Stage 14 — Final Documentation and Interview Preparation

- [ ] 107\. Write the README with setup instructions, architecture, APIs, configuration, and testing.
- [ ] 108\. Publish the architecture diagram, ER diagram, API documentation, and event-flow diagram.
- [ ] 109\. Document the database, Kafka, security, and AI design decisions.
- [ ] 110\. Prepare a demonstration showing telemetry ingestion, anomaly detection, incident creation, AI investigation, and simulated remediation.
- [ ] 111\. Demonstrate at least one failure scenario and explain the recovery mechanism.
- [ ] 112\. Review system design trade-offs, concurrency, transactions, idempotency, scaling, security, and AI limitations.
- [ ] 113\. Run the complete test suite and verify that the project starts from a clean checkout.
- [ ] 114\. Tag a stable release and finalize the project for the placement interview.

## FINAL WORKFLOW

Requirements → System Design → Spring Boot Setup → PostgreSQL → REST APIs → Security → Telemetry Ingestion → Anomaly Detection → Kafka → AI Investigation → Safe Remediation → Dashboard → Testing → Observability → Docker → CI/CD → Deployment → Interview Demo.

**Development rule:** Complete each stage and verify it works before moving to the next. Keep the initial system as a modular Spring Boot application, then introduce Kafka and AI as separate capabilities. Kubernetes and independently deployed microservices can be considered later if your requirements justify them.
