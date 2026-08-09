# Test Management Platform - Project Overview

> A multi-tenant test management platform (requirements, test cases,
> execution, defects, traceability, reporting) comparable in capability to
> Zephyr.

## Problem

Teams need a unified platform to manage test cases, executions, requirements
traceability, and defects so a QA manager can immediately answer: what needs
testing, what's been tested, what passed or failed, what's blocking release,
and whether the release can ship safely.

## Users

- **QA managers** - need release-level visibility: coverage, pass/fail trends, what's blocking ship
- **Testers / test engineers** - need to plan, execute, and record test runs day-to-day
- **Release managers** - need traceability from requirement to defect to gate releases

Multi-tenant: every organization has its own teams, projects, and role-based
permissions. No anonymous access - this is an internal tool for authenticated
org members.

## Features

MVP, in build order. **Test execution (6) is the headline feature** - it's the
core workflow action everything else (planning, traceability, reporting)
exists to support and measure.

1. **Org & auth foundation** - organization creation, user accounts, login/logout via Keycloak/OIDC, profile
2. **Teams & roles** - teams, roles, permissions, RBAC assignment
3. **Projects** - create/manage projects, project membership
4. **Requirements** - create and manage requirements within a project
5. **Test case repository** - folders, test cases, test steps, tags
6. **Test suites** - group test cases into suites
7. **Search & filters** - keyword search and filtering across test cases/requirements/defects
8. **Releases, builds & environments** - create and manage each
9. **Test plans** - create test plans scoped to a release
10. **Test cycles** - create cycles under a plan, assign build/environment
11. **Test execution** - assign testers, execute steps, record pass/fail/block/skip, attachments
12. **Defects** - create/edit, link severity/priority/status
13. **Traceability** - link requirement to test case to execution to defect, coverage view
14. **Reporting dashboard** - execution stats, pass/fail, coverage, defect stats, release progress
15. **CSV export** - export reports and test data
16. **Automation result ingestion** - accept JUnit/TestNG/Cucumber/pytest/Playwright/Cypress results, automation dashboard

## Data model

All tables carry `organization_id` for tenant isolation (except join tables,
which inherit it through their parents) and standard `created_at`/`updated_at`
timestamps, omitted below for brevity. IDs are UUIDs unless noted.

> **Load-bearing:** every query must be scoped by `organization_id` derived
> server-side from the authenticated user's JWT - never from a client-supplied
> value. See `coding-standards.md`.

### Organization

- `name`, `slug` (unique), `description`, `status`

### User

- `organizationId`, `externalAuthId` (Keycloak subject), `email`, `firstName`, `lastName`, `status`

### Team / TeamMember

- `Team`: `organizationId`, `name`, `description`
- `TeamMember`: join of `teamId` + `userId`

### Role / Permission / RolePermission

- `Role`: `organizationId`, `name`, `systemRole` (bool)
- `Permission`: `code` (unique, e.g. `TEST_CASE_CREATE`, `DEFECT_UPDATE`, `PROJECT_ADMIN`), `description`
- `RolePermission`: join of `roleId` + `permissionId`

### Project / ProjectMember

- `Project`: `organizationId`, `key`, `name`, `status`, `ownerId` - unique on `(organizationId, key)`
- `ProjectMember`: join of `projectId` + `userId` + `roleId`

### Requirement

- `projectId`, `key`, `title`, `status`, `priority`, `ownerId`, `releaseId`

### Test folder / Test case / Test step / Tag

- `TestFolder`: `projectId`, `parentId` (nullable, nested folders)
- `TestCase`: `projectId`, `folderId`, `key`, `title`, `priority`, `severity`, `status`, `testType`, `automationStatus`, `ownerId`, `releaseId`
- `TestCaseVersion`: `testCaseId`, `versionNumber`, `snapshot` (JSON, immutable), `changeSummary` - version history
- `TestStep`: `testCaseId`, `stepNumber`, `action`, `testData`, `expectedResult`
- `Tag` / `TestCaseTag`: project-scoped tags, many-to-many with test cases

### Test suite

- `TestSuite`: `projectId`, `parentId` (nullable, nested suites)
- `TestSuiteCase`: join of `suiteId` + `testCaseId` + `sortOrder`

### Release / Build / Environment

- `Release`: `projectId`, `name`, `version`, `status`, `startDate`, `releaseDate`
- `Build`: `projectId`, `releaseId`, `name`, `version`, `branch`, `commitSha`, `status`
- `Environment`: `projectId`, `name`, `type`, `url`

### Test plan / Test cycle

- `TestPlan`: `projectId`, `releaseId`, `name`, `status`, `ownerId`, `startDate`, `endDate`
- `TestCycle`: `projectId`, `testPlanId`, `releaseId`, `buildId`, `environmentId`, `name`, `status`, date range, `ownerId`
- `TestCycleCase`: join of `cycleId` + `testCaseId` + `assigneeId` + `sortOrder`

### Test execution / Execution step

- `TestExecution`: `cycleId`, `testCaseId`, `assigneeId`, `environmentId`, `buildId`, `status`, `startedAt`, `completedAt`, `durationMs`, `actualResult`, `comment`
  - status enum: `NOT_RUN | IN_PROGRESS | PASSED | FAILED | BLOCKED | SKIPPED | NOT_APPLICABLE`
- `ExecutionStep`: `executionId`, `testStepId`, `status`, `actualResult`, `comment`

### Defect

- `projectId`, `key`, `title`, `stepsToReproduce`, `expectedResult`, `actualResult`, `priority`, `severity`, `status`, `assigneeId`, `reporterId`, `releaseId`, `environmentId`, `buildId`
- `ExecutionDefect`: join of `executionId` + `defectId` (traceability: execution to defect)

### Traceability joins

- `RequirementTestCase`: join of `requirementId` + `testCaseId` (traceability: requirement to test case)
- Full chain: Requirement -> TestCase -> TestExecution -> Defect, plus Release -> TestCycle

### Attachment / Comment / AuditLog

- `Attachment`: `organizationId`, `entityType`, `entityId`, `fileName`, `storageKey` (S3/MinIO), `mimeType`, `sizeBytes`, `uploadedBy` - polymorphic, points at any entity
- `Comment`: `organizationId`, `entityType`, `entityId`, `authorId`, `body`, `parentId` (nullable, threaded) - polymorphic
- `AuditLog`: `organizationId`, `userId`, `entityType`, `entityId`, `action`, `oldValue`/`newValue` (JSON), `ipAddress` - **append-only**

### Automation run / Automation result

- `AutomationRun`: `projectId`, `buildId`, `environmentId`, `framework`, `status`, `totalTests`, `passedTests`, `failedTests`, `skippedTests`, `startedAt`, `completedAt`
- `AutomationResult`: `automationRunId`, `testCaseId` (nullable), `externalTestId`, `testName`, `status`, `durationMs`, `errorMessage`, `stackTrace`

### Not yet built by any feature

> See Open questions - these have a data shape in `project-plan.md` §4 but no
> build-plan item yet:

- `Integration` - `organizationId`, `projectId` (nullable), `type`, `configuration` (JSON, never plaintext credentials)
- `Webhook` / `WebhookEvent` - outbound event delivery with retry (`attempts`, `lastAttemptAt`)
- `SavedFilter` - `projectId`, `userId`, `entityType`, `filter` (JSON), `isShared`

## Tech stack

- **Next.js / React / TypeScript** - frontend
- **Tailwind CSS / shadcn/ui** - frontend styling
- **Spring Boot / Java 21** - backend REST API
- **PostgreSQL** - system of record, all tenant data
- **Flyway** - versioned schema migrations
- **Redis** - cache/session/rate-limit layer only, never system of record
- **Kafka** - async processing and event fan-out, introduced when needed
- **OpenSearch** - search, introduced once Postgres search isn't enough
- **S3 / MinIO** - object storage for attachments, evidence, automation artifacts
- **Keycloak / OIDC** - authentication, issues the JWT Spring Security validates
- **Docker Compose** - local dev; **Kubernetes** - production
- **GitHub Actions** - CI/CD
- **OpenTelemetry / Prometheus / Grafana / Loki** - observability

## Monetization

> TODO - not addressed in `project-plan.md` yet. Decide before scope depends
> on it (e.g. seat-based tiers gating features).

## UI/UX

Enterprise dashboard tool - dense data-grid heavy, functional-first,
comparable to Zephyr/Jira. Planned routes (frontend structure, not yet
scaffolded): `/login`, `/dashboard`, `/projects`, `/requirements`,
`/test-cases`, `/test-suites`, `/test-plans`, `/test-cycles`, `/executions`,
`/defects`, `/releases`, `/reports`, `/automation`, `/settings`.

> TODO - no explicit color/tone/visual direction yet. Consider `/prototype`
> once ready to nail down the actual look.

## Deployment

Local: Docker Compose (frontend, backend, postgres, redis, kafka, opensearch,
minio, keycloak). Production: Kubernetes behind a load balancer/ingress,
managed Postgres/Redis/Kafka/object storage preferred over self-hosted. Cloud
named only generically as AWS/Azure/GCP.

> TODO - no specific host, env vars, build/start commands, or health-check
> path named yet. Neither app is scaffolded, so these can't be verified yet
> either - see `AGENTS.md`.

## Open questions

- `Integration`/`Webhook`/`SavedFilter` have a data shape in `project-plan.md`
  §4 but no build-plan feature builds them yet. Likely post-MVP (the original
  source material treats integrations as v1.1+), but confirm and either add a
  build-plan item or explicitly defer.
- Monetization, UI/UX visual direction, and deployment target are all `> TODO`
  in `project-plan.md` - none block starting `/feature 1`, but resolve before
  they'd affect scope (e.g. before `/prototype` or `/release`).
- Frontend and backend apps are not scaffolded yet. `/implement` will need
  real apps to build into - scaffold both (or ask me to) before or during the
  first feature.
