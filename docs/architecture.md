# SentinelOps Architecture

> **Document status:** Initial proposed design\
> **Scope:** Synthetic-data prototype using Java, Spring Boot,
> PostgreSQL, and a replaceable AI provider\
> **Design principle:** Build a complete, reliable vertical slice before
> introducing distributed infrastructure.

## 1. Purpose and Scope

SentinelOps converts telemetry from application and infrastructure
systems into prioritized, evidence-backed incidents. It helps operators
investigate incidents with AI-generated summaries and runbook
recommendations, then supports approved remediation in a simulated
environment.

This document defines the initial high-level architecture,
modular-monolith boundaries, database ER model, REST API contract
overview, incident priorities and lifecycle, anomaly-detection rules,
and key architecture decisions.

### 1.1 Actors and system boundary

-   **Telemetry producer:** A synthetic application or authorized
    machine identity that submits telemetry.
-   **Operator:** A customer-side user who investigates incidents,
    provides feedback, and requests or approves permitted remediation.
-   **Administrator:** A platform user who manages identities, service
    configuration, detection rules, runbooks, and remediation policies.
-   **SentinelOps backend:** The authoritative system for validation,
    incident state, priority, authorization, remediation policy, and
    audit records.
-   **AI provider:** An optional external or mock LLM behind a
    provider-independent adapter. It is not authoritative for incident
    state or permissions.

**Assumption:** This initial design models one SentinelOps deployment.
Tenant isolation is not included unless multi-customer SaaS hosting is
explicitly selected as a requirement.

## 2. High-Level Architecture

The initial system is a **modular monolith**: one Spring Boot
application, internally divided into modules with explicit
responsibilities. PostgreSQL is the system of record. A local telemetry
simulator and dashboard interact with the backend through documented
REST APIs.

``` mermaid
flowchart TB
    subgraph ACTORS["Actors and data sources"]
        SIM["Synthetic telemetry simulator"]
        OP["Operator"]
        ADMIN["Administrator"]
    end

    subgraph APP["SentinelOps — Spring Boot modular monolith"]
        API["REST API layer<br/>DTO validation · JWT · error handling"]
        ING["Telemetry ingestion"]
        DET["Anomaly detection"]
        COR["Event correlation"]
        INC["Incident management<br/>lifecycle · P1–P4 priority"]
        AI["AI investigation adapter<br/>structured output validation"]
        RB["Runbook retrieval"]
        REM["Remediation policy and workflow"]
        AUD["Feedback and audit"]
        OBS["Health · metrics · structured logs"]
    end

    DB[("PostgreSQL<br/>system of record")]
    LLM["LLM provider or mock"]
    UI["Observability dashboard"]
    EXEC["Simulated action executor<br/>allowlisted actions only"]

    SIM -->|"Authenticated telemetry"| API
    OP <-->|"Incident operations"| UI
    ADMIN <-->|"Configuration and oversight"| UI
    UI <-->|"REST / JSON"| API
    API --> ING
    ING --> DET
    DET --> COR
    COR --> INC
    INC <--> AI
    AI <--> RB
    AI <-->|"Provider adapter; timeout/fallback"| LLM
    INC --> REM
    REM -->|"Approved action only"| EXEC
    INC --> AUD
    REM --> AUD
    ING <--> DB
    DET <--> DB
    COR <--> DB
    INC <--> DB
    AI <--> DB
    RB <--> DB
    REM <--> DB
    AUD <--> DB
    API -.-> OBS
    ING -.-> OBS
    DET -.-> OBS
    REM -.-> OBS
```

### 2.1 Main processing flow

1.  The simulator sends timestamped telemetry to
    `POST /api/v1/telemetry/events`.
2.  The API authenticates the producer, validates the payload, and
    deduplicates by producer event ID.
3.  The detector evaluates configured rules and stores anomaly findings
    with evidence references.
4.  The correlator groups related findings within a time window using
    service, dependency, trace, and correlation identifiers.
5.  Incident management creates or updates a single incident, calculates
    a deterministic priority, and records its rationale.
6.  The dashboard displays the incident and its evidence.
7.  An operator may request AI investigation. Runbook passages and
    evidence are supplied to the provider adapter; the returned
    structure is validated before it is shown.
8.  A permitted remediation request passes authorization, approval,
    precondition, idempotency, and allowlist checks before the simulated
    executor runs it.
9.  The system records the action, before/after state, result, audit
    events, and whether telemetry shows recovery.

### 2.2 AI safety boundary

AI may summarize evidence, suggest hypotheses, and recommend actions
from the approved catalogue. It must not set authoritative incident
priority, change incident state, grant permissions, or execute arbitrary
commands. If the AI provider is unavailable or returns invalid output,
the core telemetry and incident workflow continues.

## 3. Initial Modular Monolith

Keep these as **logical modules in one deployable Spring Boot
application**. They can use separate Java packages and service
interfaces without becoming separate microservices.

  -----------------------------------------------------------------------
  Module                  Responsibility          Owns / manages
  ----------------------- ----------------------- -----------------------
  `identity-access`       Human authentication,   Users, roles,
                          machine identity        permissions or role
                          authentication, roles   mappings
                          and authorization       

  `service-registry`      Service identity,       Service definitions and
                          criticality, ownership, configuration
                          dependencies and        
                          detector configuration  

  `telemetry`             Ingestion API, payload  Telemetry event
                          validation,             metadata and payload
                          normalization, event    
                          idempotency             

  `detection`             Baselines, rule         Anomaly findings
                          evaluation, anomaly     
                          scores and detection    
                          version                 

  `correlation`           Group related findings  Correlation decisions
                          and suppress duplicate  and evidence links
                          incident creation       

  `incident`              Incident lifecycle,     Incidents and lifecycle
                          P1--P4 priority,        history
                          assignment, comments    
                          and history             

  `ai-investigation`      Provider adapter,       AI insight records
                          structured response     
                          validation, fallback    
                          status and evidence     
                          references              

  `runbook`               Curated runbooks and    Runbooks and runbook
                          retrieval of relevant   sections
                          passages                

  `remediation`           Allowlisted actions,    Remediation requests
                          approvals,              and execution results
                          preconditions,          
                          idempotency and         
                          simulated execution     

  `feedback-audit`        Operator feedback and   Feedback and audit
                          append-oriented audit   events
                          history                 

  `dashboard-api`         Dashboard-oriented read No independent source
                          endpoints and bounded   of truth
                          aggregations            

  `observability`         Health, metrics,        Runtime telemetry; not
                          structured logs and     incident business state
                          tracing configuration   
  -----------------------------------------------------------------------

### 3.1 Package boundary example

``` text
com.sentinelops
├── identityaccess/
├── serviceregistry/
├── telemetry/
├── detection/
├── correlation/
├── incident/
├── aiinvestigation/
├── runbook/
├── remediation/
├── feedbackaudit/
├── dashboard/
└── shared/
    ├── error/
    ├── security/
    └── observability/
```

**Boundary rules**

-   Controllers handle HTTP concerns; domain rules belong in
    application/domain services.
-   DTOs are separate from JPA entities.
-   Modules communicate through defined service interfaces or domain
    events, not by reaching into each other's repositories.
-   PostgreSQL remains authoritative for durable state.
-   Use transactions around related database changes. Do not hold a
    database transaction open while waiting for an LLM response.
-   Use optimistic locking for concurrent incident updates.
-   Add Kafka in a later phase for asynchronous telemetry processing,
    retries, and dead-letter handling after the synchronous vertical
    slice works.
-   Add Redis only when a measured requirement justifies it.

## 4. Database ER Diagram

The ER diagram below is the **proposed initial logical data model**.
Exact column types, nullability, indexes, and migration details should
be finalized in database migrations.

``` mermaid
erDiagram
    USER_ACCOUNT ||--o{ USER_ROLE : has
    ROLE ||--o{ USER_ROLE : assigned
    SERVICE_DEFINITION ||--o{ TELEMETRY_EVENT : emits
    SERVICE_DEFINITION ||--o{ ANOMALY_FINDING : has
    TELEMETRY_EVENT ||--o{ ANOMALY_EVIDENCE : supports
    ANOMALY_FINDING ||--o{ ANOMALY_EVIDENCE : references
    INCIDENT ||--o{ INCIDENT_EVIDENCE : contains
    TELEMETRY_EVENT ||--o{ INCIDENT_EVIDENCE : cited_by
    ANOMALY_FINDING ||--o{ INCIDENT_EVIDENCE : cited_by
    INCIDENT ||--o{ INCIDENT_HISTORY : records
    USER_ACCOUNT ||--o{ INCIDENT_HISTORY : performs
    INCIDENT ||--o{ INCIDENT_COMMENT : has
    USER_ACCOUNT ||--o{ INCIDENT_COMMENT : writes
    INCIDENT ||--o{ AI_INSIGHT : analyzed_by
    AI_INSIGHT ||--o{ AI_EVIDENCE_REFERENCE : cites
    TELEMETRY_EVENT ||--o{ AI_EVIDENCE_REFERENCE : referenced_by
    RUNBOOK ||--o{ RUNBOOK_SECTION : contains
    RUNBOOK_SECTION ||--o{ AI_RUNBOOK_REFERENCE : cited_by
    AI_INSIGHT ||--o{ AI_RUNBOOK_REFERENCE : cites
    INCIDENT ||--o{ REMEDIATION_EXECUTION : has
    REMEDIATION_ACTION ||--o{ REMEDIATION_EXECUTION : requested_as
    USER_ACCOUNT ||--o{ REMEDIATION_EXECUTION : requests
    USER_ACCOUNT ||--o{ REMEDIATION_EXECUTION : approves
    INCIDENT ||--o{ OPERATOR_FEEDBACK : receives
    USER_ACCOUNT ||--o{ OPERATOR_FEEDBACK : submits
    USER_ACCOUNT ||--o{ AUDIT_EVENT : acts
    SERVICE_DEFINITION }o--o{ SERVICE_DEFINITION : depends_on

    USER_ACCOUNT {
        uuid id PK
        string username UK
        string password_hash
        string status
        timestamp created_at
    }
    ROLE {
        uuid id PK
        string name UK
    }
    USER_ROLE {
        uuid user_id FK
        uuid role_id FK
    }
    SERVICE_DEFINITION {
        uuid id PK
        string name UK
        string criticality
        string owner
        boolean enabled
    }
    TELEMETRY_EVENT {
        uuid id PK
        string producer_event_id UK
        uuid service_id FK
        timestamp occurred_at
        timestamp received_at
        string event_type
        string trace_id
        string correlation_id
        jsonb payload
        string schema_version
    }
    ANOMALY_FINDING {
        uuid id PK
        uuid service_id FK
        timestamp window_start
        timestamp window_end
        decimal score
        string detector_version
        jsonb reason_codes
    }
    ANOMALY_EVIDENCE {
        uuid finding_id FK
        uuid telemetry_event_id FK
    }
    INCIDENT {
        uuid id PK
        string title
        string status
        string priority
        string priority_rationale
        decimal anomaly_score
        timestamp first_seen_at
        timestamp last_seen_at
        uuid assigned_user_id FK
        integer version
    }
    INCIDENT_EVIDENCE {
        uuid id PK
        uuid incident_id FK
        uuid telemetry_event_id FK
        uuid finding_id FK
        string relationship
    }
    INCIDENT_HISTORY {
        uuid id PK
        uuid incident_id FK
        uuid actor_user_id FK
        string from_status
        string to_status
        string action
        timestamp created_at
        jsonb metadata
    }
    INCIDENT_COMMENT {
        uuid id PK
        uuid incident_id FK
        uuid author_user_id FK
        string body
        timestamp created_at
    }
    AI_INSIGHT {
        uuid id PK
        uuid incident_id FK
        string provider
        string model_version
        string prompt_version
        string status
        jsonb structured_output
        timestamp created_at
    }
    AI_EVIDENCE_REFERENCE {
        uuid id PK
        uuid ai_insight_id FK
        uuid telemetry_event_id FK
    }
    RUNBOOK {
        uuid id PK
        string title
        string service_scope
        boolean enabled
    }
    RUNBOOK_SECTION {
        uuid id PK
        uuid runbook_id FK
        string heading
        text content
    }
    AI_RUNBOOK_REFERENCE {
        uuid id PK
        uuid ai_insight_id FK
        uuid runbook_section_id FK
    }
    REMEDIATION_ACTION {
        uuid id PK
        string action_key UK
        string description
        string risk_level
        boolean enabled
    }
    REMEDIATION_EXECUTION {
        uuid id PK
        uuid incident_id FK
        uuid action_id FK
        uuid requested_by FK
        uuid approved_by FK
        string idempotency_key UK
        string status
        jsonb precondition_snapshot
        jsonb before_state
        jsonb after_state
        text result
        timestamp requested_at
        timestamp completed_at
    }
    OPERATOR_FEEDBACK {
        uuid id PK
        uuid incident_id FK
        uuid submitted_by FK
        string label
        text reason
        timestamp created_at
    }
    AUDIT_EVENT {
        uuid id PK
        uuid actor_user_id FK
        string operation
        string target_type
        string target_id
        string outcome
        string correlation_id
        timestamp created_at
        jsonb safe_metadata
    }
```

### 4.1 Data integrity notes

-   `producer_event_id` must be unique within the relevant producer
    scope. If multiple producers can reuse IDs, use a composite
    uniqueness constraint such as `(producer_id, producer_event_id)`.
-   `idempotency_key` must be unique for remediation requests within the
    defined scope.
-   Store incident evidence as references to events/findings rather than
    repeatedly copying large payloads.
-   Use foreign keys, check constraints, and indexes to protect data
    integrity and support actual filter/sort patterns.
-   Use an optimistic-lock version field on `INCIDENT` to detect
    concurrent updates.
-   `INCIDENT_HISTORY` and `AUDIT_EVENT` should be append-oriented.
    Restrict ordinary application paths from editing or deleting
    historical records.
-   A service-dependency join table will be needed to implement the
    self-referencing dependency relationship.
-   Store secrets as hashes or in environment/secret configuration;
    never store plaintext passwords or commit credentials.
-   JSONB is appropriate for variable telemetry payloads and snapshots,
    but important queryable attributes should remain typed columns.

## 5. REST API Contract

The API uses `/api/v1`, JSON request/response bodies, consistent error
responses, bounded pagination, and authenticated access for protected
operations. The full machine-readable OpenAPI contract is maintained
separately in `openapi.yaml`.

### 5.1 Initial endpoint inventory

  ----------------------------------------------------------------------------------------------------------------------
  Method            Path                                                        Purpose                Access
  ----------------- ----------------------------------------------------------- ---------------------- -----------------
  `POST`            `/api/v1/telemetry/events`                                  Ingest one telemetry   Authorized
                                                                                event                  telemetry
                                                                                                       producer

  `POST`            `/api/v1/telemetry/events/batch`                            Ingest a bounded batch Authorized
                                                                                (optional initial      telemetry
                                                                                extension)             producer

  `GET`             `/api/v1/incidents`                                         Filter/sort/paginate   Operator /
                                                                                incidents              permitted admin

  `GET`             `/api/v1/incidents/{incidentId}`                            Incident details,      Operator /
                                                                                evidence, rationale    permitted admin
                                                                                and history            

  `POST`            `/api/v1/incidents/{incidentId}/acknowledge`                Acknowledge an         Authorized
                                                                                incident               operator

  `POST`            `/api/v1/incidents/{incidentId}/assign`                     Assign an incident     Authorized
                                                                                                       operator

  `POST`            `/api/v1/incidents/{incidentId}/comments`                   Add an investigation   Authorized
                                                                                comment                operator

  `POST`            `/api/v1/incidents/{incidentId}/resolve`                    Resolve an incident    Authorized
                                                                                                       operator, subject
                                                                                                       to state rules

  `POST`            `/api/v1/incidents/{incidentId}/feedback`                   Submit                 Authorized
                                                                                true/false-positive    operator
                                                                                feedback               

  `POST`            `/api/v1/incidents/{incidentId}/ai-analysis`                Request AI analysis    Authorized
                                                                                                       operator

  `GET`             `/api/v1/incidents/{incidentId}/ai-analysis/{analysisId}`   Retrieve AI analysis   Authorized
                                                                                status/result          operator

  `POST`            `/api/v1/incidents/{incidentId}/remediations`               Request an allowlisted Authorized
                                                                                remediation            operator

  `POST`            `/api/v1/remediations/{remediationId}/approve`              Approve a pending      Explicitly
                                                                                remediation            authorized
                                                                                                       approver

  `GET`             `/api/v1/remediations/{remediationId}`                      View execution status  Authorized
                                                                                and outcome            operator /
                                                                                                       approver

  `GET`             `/api/v1/services`                                          List configured        Authorized human
                                                                                services               user

  `POST`            `/api/v1/services`                                          Register a service     Administrator

  `PATCH`           `/api/v1/services/{serviceId}`                              Update service         Administrator
                                                                                configuration          

  `GET`             `/api/v1/runbooks`                                          Search/list runbooks   Authorized human
                                                                                                       user

  `POST`            `/api/v1/runbooks`                                          Create/update runbook  Administrator
                                                                                content                

  `GET`             `/actuator/health`                                          Runtime health check   Health endpoint
                                                                                                       policy

  `GET`             `/actuator/metrics`                                         Runtime metrics        Restricted
                                                                                                       operational
                                                                                                       access
  ----------------------------------------------------------------------------------------------------------------------

### 5.2 API conventions

-   Use JWT bearer authentication for human users. Use a separate
    authenticated machine identity for telemetry producers.
-   Validate DTOs at the boundary; return `400 Bad Request` for
    malformed or invalid requests.
-   Return `401` for missing/invalid authentication, `403` for
    insufficient permission, `404` for unknown resources, `409` for
    state/version/idempotency conflicts, and `429` when rate limits are
    exceeded.
-   Use bounded pagination, for example `page`, `size` (with a
    documented maximum), `sort`, and explicit filters.
-   Use a consistent error structure with `timestamp`, `status`, `code`,
    `message`, `path`, and `correlationId`; do not expose stack traces
    or secrets.
-   Telemetry ingestion may return `202 Accepted` when processing
    asynchronously, with an event/processing identifier. If processing
    is synchronous in the first vertical slice, document the actual
    response behavior and do not claim asynchronous acceptance.
-   Use an `Idempotency-Key` header for remediation commands. Telemetry
    deduplication uses a producer event ID or a documented idempotency
    key.
-   Use optimistic locking / a version or `If-Match` mechanism for
    concurrent incident updates.
-   Do not hold a database transaction open while calling the AI
    provider.

## 6. Incident Priorities (P1--P4)

The original brief requires transparent business-impact prioritization
but does not prescribe numeric cutoffs. The following is a **proposed
initial policy**; thresholds must be configurable and tuned with
synthetic test scenarios.

  --------------------------------------------------------------------------
  Priority          Meaning             Example signals    Expected response
  ----------------- ------------------- ------------------ -----------------
  **P1 ---          Severe or           Critical           Immediate
  Critical**        widespread          payment/order      attention;
                    customer/business   service            notify/escalate
                    impact; critical    unavailable,       according to
                    service unavailable sustained severe   configured policy
                    or major business   errors, sharp      
                    KPI collapse        order-completion   
                                        drop plus high     
                                        latency            

  **P2 --- High**   Major degradation   High error rate    Prompt
                    affecting an        and latency on a   investigation and
                    important service   high-criticality   mitigation
                    or substantial user service; business  
                    segment             KPI meaningfully   
                                        reduced            

  **P3 --- Medium** Limited or moderate Elevated latency   Investigate in
                    degradation with    or resource        normal
                    bounded impact or a pressure with      operational queue
                    non-critical        limited error/KPI  
                    service affected    impact             

  **P4 --- Low**    Minor, transient,   Brief resource     Observe,
                    low-confidence or   spike without      investigate when
                    low-impact anomaly  user-facing        appropriate, or
                    requiring           degradation;       tune the detector
                    observation         isolated           
                                        low-severity       
                                        symptom            
  --------------------------------------------------------------------------

### 6.1 Proposed deterministic scoring model

Compute a normalized incident impact score from 0--100 using explainable
inputs:

-   Anomaly severity: 0--40
-   Service criticality: 0--25
-   Business KPI impact: 0--20
-   Duration / persistence: 0--10
-   Corroboration across independent signals: 0--5

Proposed mapping:

-   **P1:** score `80–100`, or a configured critical-service hard rule.
-   **P2:** score `60–79`.
-   **P3:** score `30–59`.
-   **P4:** score `0–29`.

These ranges are starting assumptions, not values specified by the
problem statement. Store the score inputs, rule IDs, and a
human-readable priority rationale. A priority must be explainable and
deterministic for the same evidence/configuration.

Do not let the LLM determine or override the authoritative priority. AI
can explain the evidence, but the backend rule engine owns the priority.

## 7. Incident Lifecycle

The lifecycle uses explicit states and validated transitions. The
proposed states follow the expanded engineering brief.

``` mermaid
stateDiagram-v2
    direction LR
    [*] --> OPEN : Correlated incident created
    OPEN --> ACKNOWLEDGED : Operator acknowledges
    ACKNOWLEDGED --> INVESTIGATING : Investigation begins
    OPEN --> INVESTIGATING : Investigation begins
    INVESTIGATING --> MITIGATING : Mitigation is approved / started
    MITIGATING --> INVESTIGATING : Action fails or more diagnosis needed
    MITIGATING --> RESOLVED : Recovery verified
    INVESTIGATING --> RESOLVED : Issue resolved and verified
    OPEN --> FALSE_POSITIVE : Authorized operator marks false positive
    ACKNOWLEDGED --> FALSE_POSITIVE : Authorized operator marks false positive
    INVESTIGATING --> FALSE_POSITIVE : Authorized operator marks false positive
    RESOLVED --> [*]
    FALSE_POSITIVE --> [*]
```

### 7.1 Lifecycle rules

-   New correlated incidents begin as `OPEN`.
-   Acknowledgment records the actor and timestamp.
-   Only valid transitions are accepted; the service layer enforces the
    transition table.
-   Entering `MITIGATING` requires a remediation workflow or another
    documented mitigation activity.
-   A remediation execution reporting success is not, by itself, proof
    that the incident is resolved. Recovery must be verified from
    subsequent telemetry or an explicit verification rule.
-   `FALSE_POSITIVE` requires feedback and should preserve the original
    evidence and decision history.
-   Reopening a resolved incident is not included in the initial
    transition graph. If required later, add an explicit transition and
    tests.
-   Concurrent changes use optimistic locking; stale updates return a
    conflict rather than silently overwriting another operator's work.

## 8. Anomaly Detection and Correlation Rules

Start with deterministic, configurable rules and an explainable
baseline. A machine-learning detector may be introduced later behind an
interface, but it is not required for the initial version.

### 8.1 Telemetry validation

Reject or quarantine events with missing required fields, invalid types,
impossible values, unsupported service identities, timestamps outside
configured bounds, oversized payloads, or unsupported schema versions.
Keep producer event ID, event time, received time, service ID, event
type, and trace/correlation IDs where available.

### 8.2 Initial anomaly rules

The numeric thresholds below are **illustrative defaults for synthetic
data**, not requirements from the source brief. Make them configurable
per service and environment.

  -----------------------------------------------------------------------------------------------------
  Rule ID                      Signal /          Example initial condition        Evidence / outcome
                               condition                                          
  ---------------------------- ----------------- -------------------------------- ---------------------
  `CPU_HIGH`                   CPU utilization   Above 85% for 3 consecutive      Record severity and
                                                 windows                          affected service

  `MEMORY_HIGH`                Memory            Above 90% for 3 consecutive      Record severity and
                               utilization       windows                          persistence

  `LATENCY_HIGH`               API p95 latency   Above configured service         Record baseline,
                                                 baseline by 2x and above a       observed value and
                                                 minimum floor for 3 windows      deviation

  `ERROR_RATE_HIGH`            Application error Above 5% and above minimum       Record numerator,
                               rate              request volume for 2 windows     denominator and
                                                                                  window

  `BUSINESS_KPI_DROP`          Completed orders  Drop greater than 30% against    Record baseline and
                               or another        comparable baseline for 3        observed KPI
                               configured KPI    windows                          

  `MULTI_SIGNAL_DEGRADATION`   Correlated        Two or more related signals      Increase
                               application and   overlap in time and share        corroboration score;
                               infrastructure    service/dependency/correlation   group evidence
                               symptoms          context                          

  `TRANSIENT_SPIKE`            Short-lived       One window only, without         Lower
                               isolated          corroborating user impact        confidence/priority
                               deviation                                          or wait for
                                                                                  persistence
  -----------------------------------------------------------------------------------------------------

For rate-based metrics, require a minimum sample/request count to avoid
overreacting to tiny denominators. Use rolling baselines or comparable
time windows where appropriate, and document how missing or late data is
handled.

### 8.3 Correlation policy

-   Configure a time window (for example, 5 minutes initially) and make
    it tunable.
-   Prefer exact trace or correlation-ID matches when present.
-   Otherwise correlate by service identity, known dependency
    relationships, event time overlap, and compatible symptom types.
-   Group multiple symptoms into one incident when the evidence supports
    a shared cause; do not create one incident per metric sample.
-   Update an existing incident when new evidence matches its active
    correlation key/window.
-   Use a stable deduplication/correlation key and database constraints
    or transactional checks to prevent duplicate incidents under
    concurrent processing.
-   Preserve evidence references, detector version, correlation
    rationale, first/last-seen times, and the rule IDs that fired.
-   Define explicit behavior for late/out-of-order events. Late evidence
    may update an incident if it falls within the allowed window; it
    must not silently corrupt lifecycle history.

## 9. Remediation Safety Requirements

Only preconfigured action IDs may be requested. Example simulated
actions include restarting a dummy worker, changing a simulated replica
count, or rolling back a test configuration version.

Before execution, the backend must verify:

1.  The requesting identity is authenticated and authorized.
2.  The action is enabled in the allowlist.
3.  The incident and action are in a valid state.
4.  Required approval has been recorded by an authorized approver.
5.  Preconditions still hold and the request is not stale.
6.  The idempotency key has not already caused a side effect.
7.  Retry count and timeout remain within configured limits.

Record requester, approver, action ID, idempotency key, preconditions,
before/after state, timestamps, result, and verification outcome. Never
pass model-generated shell commands to an executor. The executor is a
local simulator, not a production control plane.

## 10. Important Architecture Decisions

### ADR-001: Modular monolith first

**Decision:** Implement one Spring Boot deployment with clear internal
modules.

**Reasoning:** It reduces deployment and debugging overhead while the
domain model and workflows are still evolving. Module boundaries
preserve the option to split components later.

**Trade-off:** Modules share a deployment lifecycle and initially scale
together. Extract a service only when separate scaling, isolation,
ownership, or deployment cadence justifies the operational cost.

### ADR-002: PostgreSQL is the system of record

**Decision:** Persist incidents, telemetry metadata, findings,
configuration, remediation executions, feedback, and audit records in
PostgreSQL.

**Reasoning:** The project needs durable state, constraints,
transactions, indexes, and reliable relational queries.

**Trade-off:** High-volume raw telemetry may eventually need
partitioning, retention policies, or a dedicated storage strategy. Do
not add another datastore without measured need.

### ADR-003: Synchronous vertical slice before Kafka

**Decision:** Make the initial workflow correct and testable before
introducing Kafka. Add Kafka in a later phase for asynchronous
processing, retries, and dead-letter handling.

**Reasoning:** This separates domain correctness from messaging
complexity.

**Trade-off:** The first version has less decoupling and may have lower
throughput. Once Kafka is added, define delivery semantics, consumer
idempotency, retry limits, dead-letter inspection, and reliable
database/message publication.

### ADR-004: AI is optional and non-authoritative

**Decision:** Use a provider-independent interface with timeouts, schema
validation, bounded retries where safe, and a fallback status.

**Reasoning:** Incident ingestion, detection, prioritization, and
evidence access must continue during an AI outage. AI output can be
uncertain or malformed.

**Trade-off:** The system needs a structured evidence bundle, runbook
retrieval, output validation, and explicit degraded states. AI
recommendations remain advisory.

### ADR-005: Simulated allowlisted remediation only

**Decision:** Execute only known action IDs through a sandbox executor,
after authorization and approval checks.

**Reasoning:** The prototype must demonstrate a safe workflow without
controlling production infrastructure or running arbitrary
model-generated commands.

**Trade-off:** The simulator cannot prove real infrastructure recovery.
The demo must show before/after state and verify recovery through
synthetic telemetry.

### ADR-006: Explicit lifecycle and optimistic locking

**Decision:** Validate incident state transitions in the service layer
and use a version field for concurrent updates.

**Reasoning:** Two operators may act on the same incident. Silent
last-write-wins behavior can lose decisions or permit invalid
transitions.

**Trade-off:** Clients must handle `409 Conflict` and refresh stale
data.

### ADR-007: JWT and least privilege

**Decision:** Authenticate human users and telemetry producers using
separate identity paths, then enforce role, resource, and
remediation-policy checks in the backend.

**Reasoning:** UI controls alone are not security boundaries; telemetry
producers should not receive human or administrative permissions.

**Trade-off:** Token lifecycle, machine credentials, role assignments,
and secret management require explicit configuration and tests.

### ADR-008: Explainable configurable detection first

**Decision:** Start with thresholds, rolling baselines, persistence, and
correlation rules behind a detector interface.

**Reasoning:** The project requires defensible anomaly scores and
priority rationale, and the synthetic scenarios must be reproducible.

**Trade-off:** Simple rules can produce false positives and need tuning.
Feedback is collected for evaluation; it must not silently retrain or
change rules without a reviewed process.

## 11. Delivery Sequence

1.  **Core backend:** REST DTOs, validation, PostgreSQL schema,
    migrations, telemetry persistence, OpenAPI and domain tests.
2.  **Secure incident domain:** JWT, roles, lifecycle transitions,
    P1--P4 policy, transactions, optimistic locking and audit history.
3.  **Detection pipeline:** Synthetic generator, anomaly rules,
    correlation windows, evidence links and deduplication.
4.  **Dashboard vertical slice:** Current metrics, incident list/detail,
    evidence, status and feedback.
5.  **AI investigation:** Runbook retrieval, provider adapter, typed
    output validation, timeout/fallback tests.
6.  **Safe remediation:** Allowlist, approval workflow, preconditions,
    idempotency, simulation, audit and recovery verification.
7.  **Reliability and operations:** Kafka, retry/DLQ handling, metrics,
    tracing, load tests, Docker Compose and failure scenarios.

## 12. Acceptance Checks

-   A multi-dimensional synthetic anomaly creates one correlated
    incident with evidence, score, and priority rationale.
-   Duplicate telemetry and duplicate message delivery do not create
    duplicate evidence or remediation side effects.
-   Invalid payloads are rejected with clear errors.
-   Illegal lifecycle transitions and stale concurrent updates are
    rejected safely.
-   AI timeout or malformed output does not stop core incident
    processing.
-   Unauthorized remediation is denied and audited; no action executes
    without required approval.
-   Remediation records before/after state and reports failure if
    recovery is not observed.
-   The dashboard displays current and historical incidents and
    remediation status.
-   Automated tests cover domain logic, API behavior, security,
    persistence, messaging boundaries, and important failure cases.
-   Docker Compose starts the documented local environment using
    synthetic data only.
-   A repeatable five-minute demonstration shows normal operation,
    anomaly detection, AI-assisted investigation, approved simulated
    remediation, and at least one failure scenario.
