# Test Management Platform

## 1. Purpose

Build a modern, multi-tenant test management platform comparable in capability to Zephyr.

The platform manages:

* Requirements
* Test cases
* Test suites
* Test plans
* Test cycles
* Test executions
* Defects
* Releases
* Builds
* Test environments
* Automated test results
* Traceability
* Dashboards
* Reports
* Integrations
* Users
* Teams
* Permissions
* Audit history

The initial release should focus on manual test management, execution, traceability and reporting.

Automation, CI/CD, AI and enterprise capabilities should be added incrementally.

---

# 2. Recommended Technology Stack

## 2.1 Architecture

Use a **modular monolith** for the MVP.

Do NOT start with microservices.

```text
Browser
   |
   v
Next.js Web Application
   |
   v
Spring Boot REST API
   |
   +------------------+
   |                  |
   v                  v
PostgreSQL           Redis
   |
   +------------------+
   |
   v
Object Storage
(S3 / MinIO)

Async Processing
      |
      v
    Kafka

Search
      |
      v
  OpenSearch
```

The application should be structured internally as modules so individual modules can later become services if scale requires it.

---

# 3. Frontend

## Technology

* Next.js
* React
* TypeScript
* Tailwind CSS
* shadcn/ui
* TanStack Query
* TanStack Table
* React Hook Form
* Zod
* Zustand where client-side state is required
* Playwright

## Why

Next.js + React provides:

* Excellent TypeScript support
* Fast development
* Component ecosystem
* Good enterprise dashboard support
* Server/client rendering options
* Excellent testing ecosystem

## Frontend structure

```text
frontend/
├── app/
│   ├── login/
│   ├── dashboard/
│   ├── projects/
│   ├── requirements/
│   ├── test-cases/
│   ├── test-suites/
│   ├── test-plans/
│   ├── test-cycles/
│   ├── executions/
│   ├── defects/
│   ├── releases/
│   ├── reports/
│   ├── automation/
│   └── settings/
│
├── components/
├── features/
├── hooks/
├── lib/
├── services/
├── types/
└── tests/
```

---

# 4. Backend

## Recommended

**Spring Boot + Java 21**

Use:

* Spring Boot
* Spring Web
* Spring Security
* Spring Data JPA
* Hibernate
* Bean Validation
* Spring Kafka
* Spring Actuator
* Flyway
* Testcontainers

## Why Spring Boot

This product has:

* Complex relational data
* Complex authorization
* Enterprise integrations
* Large transactions
* Auditing
* Background jobs
* Reporting
* Multi-tenancy
* Long-term maintainability requirements

Spring Boot is particularly well suited to this.

---

# 5. Backend Alternatives

## Node.js

Good choice if:

* Team is heavily TypeScript-based
* Product is smaller
* Real-time features dominate
* Rapid development is the priority

Recommended stack:

```text
Node.js
NestJS
TypeScript
PostgreSQL
Prisma
Redis
Kafka
```

However, for this particular product I would choose Spring Boot.

## Django

Good choice if:

* Team is Python-heavy
* Admin tooling is important
* AI/data processing is a major part of the product

Recommended stack:

```text
Django
Django REST Framework
PostgreSQL
Celery
Redis
```

For a large enterprise-oriented test management platform, Spring Boot is my first choice.

---

# 6. Database

## PostgreSQL

Use PostgreSQL as the primary database.

Reasons:

* Strong relational model
* Transactions
* JSONB
* Full-text capabilities
* Excellent indexing
* Mature ecosystem
* Strong Spring integration
* Suitable for complex relationships

Do not use MongoDB as the primary database.

The domain is highly relational.

---

# 7. Database Migration

Use:

**Flyway**

All schema changes must be version-controlled.

Example:

```text
db/
└── migration/
    ├── V1__initial_schema.sql
    ├── V2__users.sql
    ├── V3__projects.sql
    ├── V4__test_cases.sql
    ├── V5__test_execution.sql
    └── ...
```

Never modify production schema manually.

---

# 8. Redis

Use Redis for:

* Session-related caching
* API caching
* Rate limiting
* Temporary state
* Distributed locks
* Background job coordination
* Frequently accessed project metadata

Do not use Redis as the system of record.

---

# 9. Search

Use **OpenSearch** once the dataset becomes large.

Search:

* Test cases
* Requirements
* Defects
* Comments
* Executions
* Releases
* Builds

For the MVP, PostgreSQL search can be sufficient.

Introduce OpenSearch when search performance or advanced search requirements justify it.

---

# 10. Object Storage

Use S3-compatible storage.

Examples:

* AWS S3
* MinIO for local development

Store:

* Screenshots
* Videos
* Logs
* PDFs
* Test evidence
* Attachments
* Automation artifacts

Never store large files directly inside PostgreSQL.

---

# 11. Authentication

Use:

**Keycloak + OpenID Connect**

Initial authentication:

```text
Browser
   |
   v
Keycloak
   |
   v
JWT
   |
   v
Spring Boot
```

Support:

* Username/password
* MFA
* OIDC
* OAuth
* SAML later
* SSO
* Refresh tokens
* Session management

Enterprise version:

* SAML
* SCIM
* Identity provider integration

---

# 12. Authorization

Implement RBAC.

Hierarchy:

```text
Organization
    |
    +-- User
    |
    +-- Team
    |
    +-- Role
          |
          +-- Permissions

Project
    |
    +-- Project Roles
```

Example:

```text
TEST_CASE_CREATE
TEST_CASE_READ
TEST_CASE_UPDATE
TEST_CASE_DELETE

TEST_EXECUTION_CREATE
TEST_EXECUTION_UPDATE

TEST_PLAN_CREATE
TEST_PLAN_UPDATE

DEFECT_CREATE
DEFECT_UPDATE

REPORT_READ
REPORT_CREATE

PROJECT_ADMIN
USER_ADMIN
```

---

# 13. Multi-Tenancy

Use a shared database with tenant isolation for the MVP.

Every tenant-owned record should contain:

```text
organization_id
```

Example:

```text
organizations
projects
test_cases
requirements
defects
test_plans
```

All queries must enforce tenant isolation.

Never trust the client to provide authorization boundaries.

The backend must derive tenant context from the authenticated user.

---

# 14. Core Database Schema

## organizations

```sql
organizations
-------------
id UUID PK
name VARCHAR
slug VARCHAR UNIQUE
description TEXT
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

## users

```sql
users
-----
id UUID PK
organization_id UUID FK
external_auth_id VARCHAR
email VARCHAR
first_name VARCHAR
last_name VARCHAR
avatar_url VARCHAR
timezone VARCHAR
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

## teams

```sql
teams
-----
id UUID PK
organization_id UUID FK
name VARCHAR
description TEXT
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

## team_members

```sql
team_members
------------
team_id UUID FK
user_id UUID FK
created_at TIMESTAMP

PRIMARY KEY(team_id, user_id)
```

---

# 15. Projects

```sql
projects
--------
id UUID PK
organization_id UUID FK
key VARCHAR
name VARCHAR
description TEXT
status VARCHAR
owner_id UUID FK
created_at TIMESTAMP
updated_at TIMESTAMP
```

Unique:

```text
organization_id + key
```

---

# 16. Project Members

```sql
project_members
---------------
project_id UUID FK
user_id UUID FK
role_id UUID FK
created_at TIMESTAMP
```

---

# 17. Roles

```sql
roles
-----
id UUID PK
organization_id UUID FK
name VARCHAR
description TEXT
system_role BOOLEAN
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 18. Permissions

```sql
permissions
-----------
id UUID PK
code VARCHAR UNIQUE
description TEXT
```

---

# 19. Role Permissions

```sql
role_permissions
----------------
role_id UUID FK
permission_id UUID FK

PRIMARY KEY(role_id, permission_id)
```

---

# 20. Releases

```sql
releases
--------
id UUID PK
project_id UUID FK
name VARCHAR
version VARCHAR
description TEXT
status VARCHAR
start_date DATE
release_date DATE
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 21. Builds

```sql
builds
------
id UUID PK
project_id UUID FK
release_id UUID FK
name VARCHAR
version VARCHAR
branch VARCHAR
commit_sha VARCHAR
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 22. Environments

```sql
environments
------------
id UUID PK
project_id UUID FK
name VARCHAR
type VARCHAR
url VARCHAR
description TEXT
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 23. Requirements

```sql
requirements
------------
id UUID PK
project_id UUID FK
key VARCHAR
title VARCHAR
description TEXT
status VARCHAR
priority VARCHAR
owner_id UUID FK
release_id UUID FK
created_by UUID FK
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 24. Test Folders

```sql
test_folders
------------
id UUID PK
project_id UUID FK
parent_id UUID FK NULL
name VARCHAR
description TEXT
created_at TIMESTAMP
updated_at TIMESTAMP
```

This supports nested folders.

---

# 25. Test Cases

```sql
test_cases
----------
id UUID PK
project_id UUID FK
folder_id UUID FK
key VARCHAR
title VARCHAR
description TEXT
objective TEXT
preconditions TEXT
priority VARCHAR
severity VARCHAR
status VARCHAR
test_type VARCHAR
automation_status VARCHAR
owner_id UUID FK
created_by UUID FK
release_id UUID FK
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 26. Test Case Versions

```sql
test_case_versions
------------------
id UUID PK
test_case_id UUID FK
version_number INTEGER
snapshot JSONB
created_by UUID FK
created_at TIMESTAMP
change_summary TEXT
```

Store immutable versions.

---

# 27. Test Steps

```sql
test_steps
----------
id UUID PK
test_case_id UUID FK
step_number INTEGER
action TEXT
test_data TEXT
expected_result TEXT
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 28. Tags

```sql
tags
----
id UUID PK
project_id UUID FK
name VARCHAR
created_at TIMESTAMP
```

---

# 29. Test Case Tags

```sql
test_case_tags
--------------
test_case_id UUID FK
tag_id UUID FK

PRIMARY KEY(test_case_id, tag_id)
```

---

# 30. Test Suites

```sql
test_suites
-----------
id UUID PK
project_id UUID FK
parent_id UUID FK NULL
name VARCHAR
description TEXT
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 31. Suite Test Cases

```sql
test_suite_cases
----------------
suite_id UUID FK
test_case_id UUID FK
sort_order INTEGER

PRIMARY KEY(suite_id, test_case_id)
```

---

# 32. Test Plans

```sql
test_plans
----------
id UUID PK
project_id UUID FK
release_id UUID FK
name VARCHAR
description TEXT
status VARCHAR
owner_id UUID FK
start_date DATE
end_date DATE
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 33. Test Cycles

```sql
test_cycles
-----------
id UUID PK
project_id UUID FK
test_plan_id UUID FK
release_id UUID FK
build_id UUID FK
environment_id UUID FK
name VARCHAR
description TEXT
status VARCHAR
start_date TIMESTAMP
end_date TIMESTAMP
owner_id UUID FK
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 34. Test Cycle Cases

```sql
test_cycle_cases
----------------
id UUID PK
cycle_id UUID FK
test_case_id UUID FK
assignee_id UUID FK
sort_order INTEGER
created_at TIMESTAMP
```

---

# 35. Test Executions

```sql
test_executions
---------------
id UUID PK
cycle_id UUID FK
test_case_id UUID FK
assignee_id UUID FK
environment_id UUID FK
build_id UUID FK
status VARCHAR
started_at TIMESTAMP
completed_at TIMESTAMP
duration_ms BIGINT
actual_result TEXT
comment TEXT
created_at TIMESTAMP
updated_at TIMESTAMP
```

Statuses:

```text
NOT_RUN
IN_PROGRESS
PASSED
FAILED
BLOCKED
SKIPPED
NOT_APPLICABLE
```

---

# 36. Execution Steps

```sql
execution_steps
---------------
id UUID PK
execution_id UUID FK
test_step_id UUID FK
status VARCHAR
actual_result TEXT
comment TEXT
executed_at TIMESTAMP
```

---

# 37. Defects

```sql
defects
-------
id UUID PK
project_id UUID FK
key VARCHAR
title VARCHAR
description TEXT
steps_to_reproduce TEXT
expected_result TEXT
actual_result TEXT
priority VARCHAR
severity VARCHAR
status VARCHAR
assignee_id UUID FK
reporter_id UUID FK
release_id UUID FK
environment_id UUID FK
build_id UUID FK
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 38. Execution Defects

```sql
execution_defects
-----------------
execution_id UUID FK
defect_id UUID FK

PRIMARY KEY(execution_id, defect_id)
```

---

# 39. Requirement Test Cases

```sql
requirement_test_cases
----------------------
requirement_id UUID FK
test_case_id UUID FK

PRIMARY KEY(requirement_id, test_case_id)
```

---

# 40. Attachments

```sql
attachments
-----------
id UUID PK
organization_id UUID FK
entity_type VARCHAR
entity_id UUID
file_name VARCHAR
storage_key VARCHAR
mime_type VARCHAR
size_bytes BIGINT
uploaded_by UUID FK
created_at TIMESTAMP
```

Use object storage for the actual file.

---

# 41. Comments

```sql
comments
--------
id UUID PK
organization_id UUID FK
entity_type VARCHAR
entity_id UUID
author_id UUID FK
body TEXT
parent_id UUID NULL
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 42. Audit Logs

```sql
audit_logs
----------
id UUID PK
organization_id UUID FK
user_id UUID FK
entity_type VARCHAR
entity_id UUID
action VARCHAR
old_value JSONB
new_value JSONB
ip_address VARCHAR
user_agent TEXT
created_at TIMESTAMP
```

Audit logs should be append-only.

---

# 43. Automation Runs

```sql
automation_runs
---------------
id UUID PK
project_id UUID FK
build_id UUID FK
environment_id UUID FK
framework VARCHAR
status VARCHAR
total_tests INTEGER
passed_tests INTEGER
failed_tests INTEGER
skipped_tests INTEGER
started_at TIMESTAMP
completed_at TIMESTAMP
created_at TIMESTAMP
```

---

# 44. Automation Results

```sql
automation_results
------------------
id UUID PK
automation_run_id UUID FK
test_case_id UUID FK NULL
external_test_id VARCHAR
test_name VARCHAR
status VARCHAR
duration_ms BIGINT
error_message TEXT
stack_trace TEXT
created_at TIMESTAMP
```

---

# 45. Integrations

```sql
integrations
------------
id UUID PK
organization_id UUID FK
project_id UUID FK NULL
type VARCHAR
name VARCHAR
configuration JSONB
status VARCHAR
created_at TIMESTAMP
updated_at TIMESTAMP
```

Credentials must never be stored as plaintext.

Use a secret manager.

---

# 46. Webhooks

```sql
webhooks
--------
id UUID PK
organization_id UUID FK
url VARCHAR
secret VARCHAR
active BOOLEAN
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 47. Webhook Events

```sql
webhook_events
--------------
id UUID PK
webhook_id UUID FK
event_type VARCHAR
payload JSONB
status VARCHAR
attempts INTEGER
last_attempt_at TIMESTAMP
created_at TIMESTAMP
```

---

# 48. Saved Filters

```sql
saved_filters
-------------
id UUID PK
project_id UUID FK
user_id UUID FK
name VARCHAR
entity_type VARCHAR
filter JSONB
is_shared BOOLEAN
created_at TIMESTAMP
updated_at TIMESTAMP
```

---

# 49. REST API

Base URL:

```text
/api/v1
```

---

# 50. Authentication API

```http
GET /api/v1/me
POST /api/v1/api-keys
DELETE /api/v1/api-keys/{id}
```

Authentication itself should primarily be handled through OIDC/Keycloak.

---

# 51. Organizations

```http
GET    /organizations
POST   /organizations
GET    /organizations/{id}
PATCH  /organizations/{id}
DELETE /organizations/{id}
```

---

# 52. Projects

```http
GET    /projects
POST   /projects
GET    /projects/{projectId}
PATCH  /projects/{projectId}
DELETE /projects/{projectId}
```

---

# 53. Requirements

```http
GET    /projects/{projectId}/requirements
POST   /projects/{projectId}/requirements
GET    /requirements/{id}
PATCH  /requirements/{id}
DELETE /requirements/{id}
```

Relationships:

```http
GET    /requirements/{id}/test-cases
POST   /requirements/{id}/test-cases/{testCaseId}
DELETE /requirements/{id}/test-cases/{testCaseId}
```

---

# 54. Test Cases

```http
GET    /projects/{projectId}/test-cases
POST   /projects/{projectId}/test-cases
GET    /test-cases/{id}
PATCH  /test-cases/{id}
DELETE /test-cases/{id}
```

Steps:

```http
GET    /test-cases/{id}/steps
POST   /test-cases/{id}/steps
PATCH  /test-cases/{id}/steps/{stepId}
DELETE /test-cases/{id}/steps/{stepId}
```

Versions:

```http
GET /test-cases/{id}/versions
GET /test-cases/{id}/versions/{version}
POST /test-cases/{id}/restore/{version}
```

---

# 55. Test Suites

```http
GET    /projects/{projectId}/test-suites
POST   /projects/{projectId}/test-suites
GET    /test-suites/{id}
PATCH  /test-suites/{id}
DELETE /test-suites/{id}
```

Test membership:

```http
POST   /test-suites/{id}/test-cases
DELETE /test-suites/{id}/test-cases/{testCaseId}
GET    /test-suites/{id}/test-cases
```

---

# 56. Test Plans

```http
GET    /projects/{projectId}/test-plans
POST   /projects/{projectId}/test-plans
GET    /test-plans/{id}
PATCH  /test-plans/{id}
DELETE /test-plans/{id}
```

---

# 57. Test Cycles

```http
GET    /projects/{projectId}/test-cycles
POST   /projects/{projectId}/test-cycles
GET    /test-cycles/{id}
PATCH  /test-cycles/{id}
DELETE /test-cycles/{id}
```

Add tests:

```http
POST /test-cycles/{id}/test-cases
DELETE /test-cycles/{id}/test-cases/{testCaseId}
```

---

# 58. Executions

```http
GET    /test-cycles/{cycleId}/executions
POST   /test-cycles/{cycleId}/executions
GET    /executions/{id}
PATCH  /executions/{id}
```

Execute:

```http
POST /executions/{id}/start
POST /executions/{id}/complete
POST /executions/{id}/pass
POST /executions/{id}/fail
POST /executions/{id}/block
```

---

# 59. Execution Steps

```http
GET   /executions/{executionId}/steps
PATCH /executions/{executionId}/steps/{stepId}
```

---

# 60. Defects

```http
GET    /projects/{projectId}/defects
POST   /projects/{projectId}/defects
GET    /defects/{id}
PATCH  /defects/{id}
DELETE /defects/{id}
```

---

# 61. Releases

```http
GET    /projects/{projectId}/releases
POST   /projects/{projectId}/releases
GET    /releases/{id}
PATCH  /releases/{id}
DELETE /releases/{id}
```

---

# 62. Builds

```http
GET    /projects/{projectId}/builds
POST   /projects/{projectId}/builds
GET    /builds/{id}
PATCH  /builds/{id}
```

---

# 63. Environments

```http
GET    /projects/{projectId}/environments
POST   /projects/{projectId}/environments
GET    /environments/{id}
PATCH  /environments/{id}
DELETE /environments/{id}
```

---

# 64. Reports

```http
GET /projects/{projectId}/reports/execution
GET /projects/{projectId}/reports/coverage
GET /projects/{projectId}/reports/defects
GET /projects/{projectId}/reports/traceability
GET /projects/{projectId}/reports/release
```

---

# 65. Dashboard

```http
GET /projects/{projectId}/dashboard
```

Response should contain metrics such as:

```json
{
  "totalTests": 1200,
  "executed": 1080,
  "passed": 920,
  "failed": 100,
  "blocked": 60,
  "notRun": 120,
  "passRate": 85.18,
  "coverage": 91.2
}
```

---

# 66. Search API

```http
GET /search
```

Example:

```text
/search?q=login&entity=test-case
```

Advanced:

```text
/search?
entity=test-case
&status=FAILED
&priority=HIGH
&assignee=UUID
&tag=regression
```

---

# 67. Import API

```http
POST /projects/{projectId}/imports/test-cases
POST /projects/{projectId}/imports/requirements
POST /projects/{projectId}/imports/defects
```

Imports should be asynchronous.

Return:

```json
{
  "jobId": "uuid",
  "status": "PROCESSING"
}
```

---

# 68. Export API

```http
POST /projects/{projectId}/exports/test-cases
POST /projects/{projectId}/exports/executions
POST /projects/{projectId}/exports/report
```

Large exports should be background jobs.

---

# 69. Automation API

```http
POST /projects/{projectId}/automation/runs
POST /projects/{projectId}/automation/results
GET  /automation/runs/{id}
GET  /automation/runs/{id}/results
```

Support:

* JUnit XML
* TestNG XML
* Cucumber JSON
* pytest XML
* Playwright results
* Cypress results

---

# 70. API Design Rules

Every endpoint should support:

* Pagination
* Sorting
* Filtering
* Consistent errors
* Request validation
* Authorization

Example:

```http
GET /test-cases?page=0&size=50&sort=createdAt,desc
```

Response:

```json
{
  "content": [],
  "page": 0,
  "size": 50,
  "totalElements": 1200,
  "totalPages": 24
}
```

---

# 71. Error Format

Use a consistent format:

```json
{
  "timestamp": "2026-08-09T16:00:00Z",
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "Invalid request",
  "path": "/api/v1/test-cases",
  "errors": [
    {
      "field": "title",
      "message": "Title is required"
    }
  ]
}
```

---

# 72. Idempotency

Use idempotency keys for operations such as:

* Import
* Automation result upload
* Webhooks
* External integrations

Example:

```http
Idempotency-Key: 5b4d...
```

---

# 73. API Versioning

Use:

```text
/api/v1
```

Future breaking version:

```text
/api/v2
```

Avoid breaking existing clients.

---

# 74. Events

Internal events:

```text
TestCaseCreated
TestCaseUpdated
TestExecutionStarted
TestExecutionCompleted
TestExecutionFailed
DefectCreated
DefectUpdated
TestCycleCompleted
ReleaseCreated
AutomationRunCompleted
```

Kafka should be introduced where asynchronous processing or integration fan-out is required.

---

# 75. Background Jobs

Use background workers for:

* CSV import
* Excel import
* Report generation
* PDF generation
* Export
* Automation result processing
* Search indexing
* Email
* Webhooks
* Notifications

Do not execute expensive operations inside normal HTTP requests.

---

# 76. Reporting Architecture

For normal dashboards:

```text
PostgreSQL
    ↓
Aggregated queries
    ↓
Spring Boot
    ↓
Dashboard
```

For large-scale analytics later:

```text
PostgreSQL
    ↓
Kafka
    ↓
Analytics storage
    ↓
Reporting
```

Do not prematurely build a data warehouse.

---

# 77. Backend Package Structure

```text
backend/
└── src/main/java/com/company/testmanagement/

    ├── organization/
    ├── user/
    ├── team/
    ├── project/
    ├── permission/
    ├── requirement/
    ├── testcase/
    ├── testsuite/
    ├── testplan/
    ├── testcycle/
    ├── execution/
    ├── defect/
    ├── release/
    ├── build/
    ├── environment/
    ├── attachment/
    ├── comment/
    ├── report/
    ├── automation/
    ├── integration/
    ├── webhook/
    ├── notification/
    ├── audit/
    └── common/
```

Each module should contain:

```text
controller/
service/
repository/
entity/
dto/
mapper/
validation/
```

Avoid a giant shared service layer.

---

# 78. Testing Strategy

## Backend

* JUnit 5
* Mockito
* Spring Boot Test
* Testcontainers
* REST integration tests
* Repository tests
* Security tests

## Frontend

* Vitest
* React Testing Library
* Playwright

## API

Test:

* Authentication
* Authorization
* CRUD
* Validation
* Pagination
* Filtering
* Multi-tenancy
* Bulk operations
* Imports
* Exports

---

# 79. Testcontainers

Use real infrastructure during integration tests:

```text
PostgreSQL
Redis
Kafka
OpenSearch
```

This avoids relying exclusively on mocks.

---

# 80. CI/CD

GitHub Actions pipeline:

```text
Pull Request
     |
     +-- Lint
     |
     +-- Unit Tests
     |
     +-- Integration Tests
     |
     +-- Security Scan
     |
     +-- Build
     |
     +-- Docker Image
     |
     v
Deploy
```

---

# 81. Docker

Services:

```text
frontend
backend
postgres
redis
kafka
opensearch
minio
keycloak
```

For local development use:

```text
docker-compose.yml
```

---

# 82. Kubernetes

Do not require Kubernetes for local development.

Production architecture:

```text
Internet
   |
Load Balancer
   |
Ingress
   |
Kubernetes
   |
   +-- Frontend
   |
   +-- Backend
   |
   +-- Workers
```

Managed services should be preferred for:

* PostgreSQL
* Redis
* Kafka
* Object storage

when operating in production.

---

# 83. Observability

Use:

* OpenTelemetry
* Prometheus
* Grafana
* Loki
* Jaeger/Tempo
* Sentry or equivalent error tracking

Monitor:

* Request latency
* Error rates
* Database latency
* Queue latency
* Kafka lag
* JVM memory
* CPU
* API throughput
* Background job failures

---

# 84. Security Requirements

Mandatory:

* HTTPS
* Secure cookies
* CSRF protection where applicable
* CORS configuration
* Input validation
* Output encoding
* SQL injection protection
* Rate limiting
* Secure headers
* Secrets management
* Password policy through identity provider
* Audit logs
* Tenant isolation

Never:

* Log passwords
* Log access tokens
* Store credentials in source control
* Trust client-provided organization IDs
* Build SQL using string concatenation

---

# 85. MVP Scope

The first production MVP should contain:

## Authentication

* Login
* Logout
* User profile
* Organization
* Project
* Roles
* Permissions

## Test Management

* Test cases
* Test steps
* Folders
* Suites
* Tags
* Search
* Filters
* Bulk editing

## Planning

* Requirements
* Releases
* Test plans
* Test cycles

## Execution

* Assign tests
* Execute tests
* Pass
* Fail
* Block
* Skip
* Execution history
* Attachments

## Defects

* Create defect
* Edit defect
* Link defect to execution
* Defect status
* Priority
* Severity

## Traceability

* Requirement → Test Case
* Test Case → Execution
* Execution → Defect
* Release → Test Cycle

## Reporting

* Execution dashboard
* Pass/fail statistics
* Requirement coverage
* Defect statistics
* Release progress
* CSV export

---

# 86. MVP Explicitly Excludes

Do NOT initially build:

* Microservices
* AI test generation
* Advanced ML
* Offline mode
* Mobile apps
* SAML
* SCIM
* Multi-region
* Complex data warehouse
* Advanced capacity planning
* Smart test selection
* Dozens of integrations

These can be added after product-market validation.

---

# 87. MVP Development Phases

## Phase 0 — Architecture

Duration: 1–2 weeks

Build:

* Repository
* CI/CD
* Docker
* PostgreSQL
* Spring Boot
* Next.js
* Authentication
* Database migrations
* API conventions
* Logging
* Error handling

Deliverable:

```text
Developer can run the complete application locally.
```

---

## Phase 1 — Organizations & Projects

Duration: 1–2 weeks

Build:

* Organizations
* Users
* Teams
* Roles
* Permissions
* Projects
* Project members
* Project settings

Deliverable:

```text
User → Organization → Project
```

---

## Phase 2 — Test Repository

Duration: 2–3 weeks

Build:

* Test folders
* Test cases
* Test steps
* Tags
* Custom fields
* Test suites
* Search
* Filters
* Version history

Deliverable:

```text
Project
  ↓
Test Repository
  ↓
Test Cases
```

---

## Phase 3 — Test Planning

Duration: 2 weeks

Build:

* Requirements
* Releases
* Test plans
* Test cycles
* Test assignments

Deliverable:

```text
Requirement
   ↓
Test Case
   ↓
Test Plan
   ↓
Test Cycle
```

---

## Phase 4 — Execution

Duration: 2–3 weeks

Build:

* Execution UI
* Step execution
* Pass/fail/block
* Assignments
* Environments
* Builds
* Execution history
* Attachments

Deliverable:

```text
Test Cycle
    ↓
Execution
    ↓
Result
```

---

## Phase 5 — Defects & Traceability

Duration: 2 weeks

Build:

* Defects
* Defect links
* Requirement links
* Traceability matrix
* Coverage

Deliverable:

```text
Requirement
    ↓
Test Case
    ↓
Execution
    ↓
Defect
```

---

## Phase 6 — Reporting

Duration: 2 weeks

Build:

* Dashboard
* Execution report
* Coverage report
* Defect report
* Release report
* CSV export

Deliverable:

```text
Testing → Metrics → Release Decision
```

---

## Phase 7 — Automation

Duration: 2–3 weeks

Build:

* Automation runs
* Result ingestion
* JUnit XML
* Test mapping
* CI integration
* Automation dashboard

Deliverable:

```text
CI Pipeline
    ↓
Automation Results
    ↓
Test Management
    ↓
Dashboard
```

---

# 88. Post-MVP Roadmap

## Version 1.1

* Jira integration
* GitHub integration
* Slack
* Email notifications
* Advanced reports
* Scheduled reports

## Version 1.2

* Jenkins
* GitHub Actions
* GitLab CI
* Cucumber
* Playwright
* Cypress
* API automation

## Version 1.3

* Custom workflows
* Approval workflows
* Parameterized testing
* Exploratory testing
* Risk-based testing

## Version 2.0

* SSO
* SAML
* SCIM
* Enterprise permissions
* Advanced audit
* Advanced analytics

## Version 2.1+

* AI test generation
* AI failure analysis
* Smart test selection
* Flaky test detection
* Quality gates
* Intelligent recommendations

---

# 89. Definition of Done — MVP

A feature is complete when:

* Backend API exists
* Authorization is implemented
* Tenant isolation is verified
* Database migration exists
* Unit tests exist
* Integration tests exist
* Frontend UI exists
* Loading states exist
* Error states exist
* Empty states exist
* Audit event exists where required
* API documentation exists
* Accessibility has been checked
* E2E test exists for critical workflows

---

# 90. Critical End-to-End Workflow

The MVP must successfully support this complete scenario:

```text
1. Create organization
        ↓
2. Create project
        ↓
3. Invite tester
        ↓
4. Create release
        ↓
5. Create requirement
        ↓
6. Create test case
        ↓
7. Add test steps
        ↓
8. Link test case to requirement
        ↓
9. Create test plan
        ↓
10. Create test cycle
        ↓
11. Add test case to cycle
        ↓
12. Assign tester
        ↓
13. Execute test
        ↓
14. Test fails
        ↓
15. Create defect
        ↓
16. Link defect to execution
        ↓
17. Fix defect
        ↓
18. Retest
        ↓
19. Test passes
        ↓
20. View release dashboard
        ↓
21. Confirm requirement coverage
        ↓
22. Export report
```

This workflow should be the primary acceptance test for the MVP.

---

# 91. Recommended Repository

```text
test-management/
│
├── frontend/
│   ├── app/
│   ├── components/
│   ├── features/
│   ├── services/
│   ├── types/
│   └── tests/
│
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
│
├── infrastructure/
│   ├── docker/
│   ├── kubernetes/
│   └── terraform/
│
├── docs/
│   ├── api/
│   ├── architecture/
│   └── database/
│
├── scripts/
│
├── docker-compose.yml
├── README.md
└── management.md
```

---

# 92. Recommended Development Order

Build in this exact order:

```text
Foundation
   ↓
Authentication
   ↓
Organizations
   ↓
Projects
   ↓
Users / Roles / Permissions
   ↓
Test Cases
   ↓
Test Suites
   ↓
Requirements
   ↓
Releases
   ↓
Test Plans
   ↓
Test Cycles
   ↓
Test Execution
   ↓
Defects
   ↓
Traceability
   ↓
Dashboard
   ↓
Reports
   ↓
API
   ↓
Automation
   ↓
Integrations
```

---

# 93. Final Technology Decision

| Area                | Technology                  |
| ------------------- | --------------------------- |
| Frontend            | Next.js                     |
| UI                  | React + TypeScript          |
| Styling             | Tailwind CSS                |
| Components          | shadcn/ui                   |
| Tables              | TanStack Table              |
| Data fetching       | TanStack Query              |
| Forms               | React Hook Form + Zod       |
| Backend             | Spring Boot                 |
| Language            | Java 21                     |
| ORM                 | Hibernate / Spring Data JPA |
| Database            | PostgreSQL                  |
| Migrations          | Flyway                      |
| Cache               | Redis                       |
| Messaging           | Kafka                       |
| Search              | OpenSearch                  |
| Files               | S3 / MinIO                  |
| Authentication      | Keycloak / OIDC             |
| API                 | REST / OpenAPI              |
| API documentation   | Swagger UI                  |
| Unit testing        | JUnit 5                     |
| Integration testing | Testcontainers              |
| Frontend testing    | Vitest + RTL                |
| E2E                 | Playwright                  |
| Containers          | Docker                      |
| Orchestration       | Kubernetes                  |
| CI/CD               | GitHub Actions              |
| Observability       | OpenTelemetry               |
| Metrics             | Prometheus                  |
| Dashboards          | Grafana                     |
| Logs                | Loki                        |
| IaC                 | Terraform                   |
| Cloud               | AWS / Azure / GCP           |

---

# 94. Architectural Principle

Start simple.

```text
                    MVP
                     |
              Modular Monolith
                     |
        +------------+------------+
        |            |            |
     Spring       PostgreSQL    Redis
      Boot
        |
   REST API
        |
     Next.js
```

Later, if scale requires it:

```text
                 API Gateway
                     |
          +----------+----------+
          |          |          |
       Core API   Automation   Reporting
          |          |          |
       Postgres     Kafka    Analytics DB
          |
       Redis
          |
      OpenSearch
```

Do not introduce microservices merely because the product is enterprise-oriented.

The first goal is to make the **core test-management workflow excellent**.

---

# 95. Product Success Criteria

The product should eventually allow a QA manager to answer five questions immediately:

1. **What needs testing?**
2. **What has been tested?**
3. **What passed or failed?**
4. **What defects are blocking release?**
5. **Can this release safely go to production?**

Every major feature should contribute to answering one or more of these questions.
