# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.


## Quick Commands

### Development
```bash
# Local development
./gradlew bootRun -Dspring-boot.run.profiles=local

# Production
./gradlew bootRun --args='--spring.profiles.active=prod'

# Build & test
./gradlew build
./gradlew test
./gradlew clean build
```

### Environment Setup
```bash
export GITLAB_ACCESS_TOKEN='your-token'
export GITLAB_ROOT_GROUP_ID='123456'
export GITLAB_BRANCH='main'
export WEBHOOK_SECRET_TOKEN='your-webhook-secret'
```

### Testing
```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests "BookmarkServiceTest"

# Integration tests
./gradlew test --tests "*.integration*"
```

## Architecture
Spring Boot 3.5/Java 21 service that manages technical documentation bookmarks through GitLab API integration.

### Core Components
- **Bookmark**: GitLab repo bookmark management with JSON schema validation
- **GitLabService**: Repository scanning and file retrieval via GitLab4J
- **Security**: API key authentication (prod only via SecurityStartupValidator)
- **WebClient**: Resilient HTTP client with retry policies

### Stack
- Java 21, Spring Boot 3.5.4
- GitLab4J for GitLab API
- Resilience4j for circuit breakers
- MapStruct for DTO mapping
- Jasypt for property encryption

### API Access
Local: http://localhost:8095/swagger-ui.html
Prod: API key required (SECURITY_API_KEY_ENABLED=true)

### Key Config
- Port: 8095
- GitLab token requires read_repository permission
- Local profile uses environment variables
- Production enforces API key validation at startup

## Project Guidelines (Read First)

This repository follows strict engineering conventions. Before making changes, read and follow the full guidelines:
- .junie/guidelines.md

Essentials distilled from the guidelines:
- Architecture
  - Component-based development (small reusable components). Business orchestration only in Facade(Service).
  - Layering: Controller ↔ Facade(Service) ↔ Repository/Adapter. No business composition in Controller/Repository.
  - API I/O must use DTOs only (do not expose entities).
- Common response and errors
  - Reuse the existing common response/error model. Do not invent a new shape.
  - Validate requests with Bean Validation; return unified error payload for validation and domain errors.
- Transactions
  - Put @Transactional on Facade public methods; mark queries as readOnly = true.
- Logging
  - Include request/correlation context; never log sensitive data. Keep server time UTC.
- Testing (BDD-first)
  - Write BDD-style tests (Given–When–Then). Cover normal, boundary, and error flows.
  - Mock only external dependencies. Prefer real implementations for small stateless components.
- Persistence/JPA
  - Prevent N+1 (fetch join/entity graph). Keep JPA details out of services.
- API
  - Resourceful URIs, versioning (/api/v1), pagination schema, ISO-8601 times, enum name() serialization.
- Dependencies
  - Do not add new external libraries unless explicitly requested.

### Claude Code workflow checklist
- Before coding
  - Read .junie/guidelines.md and check existing common response/error classes or advices.
  - Identify if there’s an existing component to reuse (validators, mappers, policies, converters, enrichers).
- When implementing
  - Keep controllers thin: validate + delegate to Facade; no business logic.
  - Put orchestration and transactions in Facade; compose small components.
  - Use DTOs for all API boundaries; add Bean Validation on request DTOs.
  - Ensure errors are mapped to the unified response model; do not bypass global handlers/advices.
  - Avoid N+1 in repository queries; keep JPA specifics in adapters.
  - Add BDD-style tests for success, validation failure, and business rule violations.
- After implementing
  - Verify logs contain context without sensitive data; maintain UTC timestamps.
  - Run tests: ./gradlew test
  - Build if needed: ./gradlew clean build

Quick link: .junie/guidelines.md