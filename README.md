Social Insurance Contributions API

A production-style REST API for managing citizen social insurance contributions and determining eligibility based on paid contribution months.

This project demonstrates layered architecture, database migrations, integration testing, and containerized development using Spring Boot and PostgreSQL.

🚀 Tech Stack

Java 21

Spring Boot 3

Spring Data JPA

PostgreSQL 16

Flyway (database migrations)

Docker & Docker Compose

Maven

Spring Boot Test (Integration Testing)

📐 Architecture

Layered architecture following common enterprise patterns:

Controller → Service → Repository → Database
Layers

Controller

Exposes REST endpoints

Handles request validation

Contains OpenAPI annotations

Service

Implements business logic

Defines transactional boundaries

Repository

Handles JPA queries

Manages data access

Flyway

Manages schema versioning

Applies database migrations

Docker

Provides containerized PostgreSQL environment

## 📊 Architecture Diagram

```
        ┌─────────────────────┐
        │     REST Client     │
        └─────────┬───────────┘
                  │ HTTP
        ┌─────────▼───────────┐
        │     Controller      │
        └─────────┬───────────┘
                  │
        ┌─────────▼───────────┐
        │      Service        │
        │  (Business Logic)   │
        └─────────┬───────────┘
                  │
        ┌─────────▼───────────┐
        │     Repository      │
        │  (Spring Data JPA)  │
        └─────────┬───────────┘
                  │
        ┌─────────▼───────────┐
        │     PostgreSQL      │
        │   (Flyway Managed)  │
        └─────────────────────┘
```
🗄 Domain Overview
Citizens

Registered individuals contributing to the social insurance system.

Employers

Organizations paying contributions on behalf of citizens.

Contributions

Monthly records containing:

Citizen

Employer

Month

Amount

Currency

Payment timestamp

Eligibility

A citizen is considered eligible if they have at least N paid contribution months within a defined time window.

Example endpoint:

GET /api/v1/citizens/{citizenId}/eligibility?monthsBack=6&minMonthsPaid=3
🐳 Running Locally
1. Start PostgreSQL via Docker
docker compose up -d
2. Run the Application
mvn spring-boot:run

The application runs at:

http://localhost:8080

Swagger UI:

http://localhost:8080/swagger-ui.html
🧪 Running Tests

Integration tests (Docker must be running):

mvn verify -Pintegration
📦 Example API Endpoints
Create Citizen
POST /api/v1/citizens
Create Employer
POST /api/v1/employers
Create Contribution
POST /api/v1/contributions
Check Eligibility
GET /api/v1/citizens/{id}/eligibility
Paginated Contributions
GET /api/v1/contributions?page=0&size=20
🔄 Database Migrations

The schema is versioned using Flyway.

Migration scripts are located at:

src/main/resources/db/migration/

Migrations are automatically applied on application startup.

🧠 Design Notes

Uses a Clock bean for deterministic time-based testing

Avoids loading entire entity graphs for eligibility calculation

Uses count(distinct monthDate) for performance

Integration tests validate real HTTP behavior

Profiles

dev

docker

test

📌 Project Purpose

This project was built as a structured backend sample demonstrating:

Clean layered architecture

Business rule implementation

Database migration strategy

Integration testing

Containerized local development
