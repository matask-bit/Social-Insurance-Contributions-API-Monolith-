## Social Insurance Contributions API – Plan (v1)

### Scope (v1)
- **Architecture**: Single Spring Boot monolith exposing a REST API.
- **Domain focus**: Calculation, storage, and retrieval of social insurance contributions and related benefit claims.
- **In scope (v1)**:
  - Basic CRUD for `Citizen`, `Employer`, `Contribution`, and `BenefitClaim`.
  - Recording contributions paid by employers for citizens.
  - Simple eligibility check and registration for benefit claims (rule-driven, not workflow-heavy).
  - Read-only reporting-style queries (by citizen, employer, period).
- **Out of scope (v1)**:
  - Payments processing, invoicing, or payroll integration.
  - Complex workflow/state machine for claims (simple status model only).
  - Multi-tenancy, microservices, and advanced security (beyond basic authN/authZ).

### Core Domain Entities
- **Citizen**
  - Identified by national ID (unique), plus name, date of birth, contact info.
  - May be linked to multiple employers over time.

- **Employer**
  - Identified by a registration number (unique), legal name, contact info.
  - Responsible for reporting and paying contributions for associated citizens.

- **Contribution**
  - Represents a periodic contribution for a citizen from an employer.
  - Key attributes: citizen, employer, period (year/month), base salary, contribution amount, contribution type, status.
  - One contribution record per citizen, employer, and period per contribution type.

- **BenefitClaim**
  - Represents a citizen’s claim for a social insurance benefit (e.g., pension, sickness).
  - Key attributes: citizen, claim type, claim date, status, decision date, reason/notes.
  - Eligibility is based on contribution history over a defined period.

### Main Business Rules (v1)
- **Citizen rules**
  - National ID must be unique and immutable once created.
  - Basic mandatory fields: national ID, full name, date of birth.

- **Employer rules**
  - Employer registration number must be unique.
  - Cannot be deactivated if it still has active contributions in the current period.

- **Contribution rules**
  - Combination of (citizen, employer, contribution type, year, month) must be unique.
  - Amount must be non-negative; base salary must be positive.
  - Only contributions in `DRAFT` or `REJECTED` status can be edited.
  - Only contributions in `DRAFT` can transition to `POSTED`.
  - Posted contributions cannot be deleted; they may only be corrected via an adjustment entry (v1: out of scope, can be a new separate record with reference).

- **BenefitClaim rules**
  - A claim must be linked to exactly one citizen.
  - Initial status is `SUBMITTED`.
  - Allowed status transitions (v1):
    - `SUBMITTED` → `UNDER_REVIEW`
    - `UNDER_REVIEW` → `APPROVED` or `REJECTED`
  - Eligibility check (simplified):
    - Minimum contribution periods and/or sum amounts per claim type (values configurable).

### High-Level REST Endpoints (v1)

#### Citizens
- **GET** `/api/v1/citizens`
- **POST** `/api/v1/citizens`
- **GET** `/api/v1/citizens/{citizenId}`
- **PUT** `/api/v1/citizens/{citizenId}`
- **DELETE** `/api/v1/citizens/{citizenId}` (soft-delete or deactivation)

#### Employers
- **GET** `/api/v1/employers`
- **POST** `/api/v1/employers`
- **GET** `/api/v1/employers/{employerId}`
- **PUT** `/api/v1/employers/{employerId}`
- **DELETE** `/api/v1/employers/{employerId}` (soft-delete or deactivation)

#### Contributions
- **GET** `/api/v1/contributions`
- **POST** `/api/v1/contributions`
- **GET** `/api/v1/contributions/{contributionId}`
- **PUT** `/api/v1/contributions/{contributionId}`
- **POST** `/api/v1/contributions/{contributionId}/post`
- **GET** `/api/v1/citizens/{citizenId}/contributions`
- **GET** `/api/v1/employers/{employerId}/contributions`

#### Benefit Claims
- **GET** `/api/v1/benefit-claims`
- **POST** `/api/v1/benefit-claims`
- **GET** `/api/v1/benefit-claims/{claimId}`
- **PUT** `/api/v1/benefit-claims/{claimId}`
- **POST** `/api/v1/benefit-claims/{claimId}/submit` (if not auto-submitted on create)
- **POST** `/api/v1/benefit-claims/{claimId}/review`
- **POST** `/api/v1/benefit-claims/{claimId}/approve`
- **POST** `/api/v1/benefit-claims/{claimId}/reject`
- **GET** `/api/v1/citizens/{citizenId}/benefit-claims`

#### Technical / Misc
- **GET** `/actuator/health`
- **GET** `/v3/api-docs`
- **GET** `/swagger-ui/index.html`

### Non-Functional Requirements (v1)
- **Validation**
  - Use Bean Validation (Jakarta Validation) on request DTOs and entities.
  - Enforce mandatory fields, ranges (e.g., positive amounts), and format rules (e.g., national ID pattern).
  - Reject invalid requests with structured error responses.

- **Error handling**
  - Centralized `@ControllerAdvice` for exception mapping.
  - Return consistent error payload (timestamp, path, error code, human-readable message, optional details).
  - Map domain/business errors to appropriate HTTP statuses (e.g., 400, 404, 409).

- **Database & migrations**
  - Use relational database (e.g., PostgreSQL) with proper indexing on IDs, foreign keys, and period fields.
  - Use Flyway or Liquibase for schema migrations; migrations must be versioned and repeatable for all environments.
  - No manual schema changes outside migrations.

- **OpenAPI / API documentation**
  - Generate OpenAPI 3 spec via springdoc-openapi.
  - Keep request/response models documented with clear descriptions and example payloads where useful.
  - Swagger UI enabled in non-production environments; optionally protected in production.

- **Testing**
  - Unit tests for domain logic (rules for contributions and benefit claims).
  - Web layer tests for controllers (REST endpoints) focusing on status codes and payload structure.
  - Integration tests with an in-memory or containerized database (e.g., Testcontainers).
  - Basic smoke tests for critical flows (create citizen/employer, record contribution, submit and approve claim).

- **Security & other concerns**
  - Basic authentication/authorization (e.g., JWT or OAuth2 resource server), at least role-based (admin, employer, internal-operator).
  - Audit fields (`createdAt`, `createdBy`, `updatedAt`, `updatedBy`) on main entities.
  - Basic logging (request/response summary, key domain events) with correlation IDs where possible.

