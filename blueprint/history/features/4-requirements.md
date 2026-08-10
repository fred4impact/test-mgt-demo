# Feature: Requirements

**From build-plan:** feature 4
**Status:** complete

## Goal

Let a project owner create and manage requirements within a project - the
first entity in the traceability chain (Requirement -> TestCase ->
TestExecution -> Defect) that later features (5, 13) build on. This is
also the first entity with its own project-scoped auto-generated key
(`{projectKey}-1`, `{projectKey}-2`, ...), the pattern later entities
(`TestCase`, `Defect`) reuse per the data model.

## In scope

- `Requirement` JPA entity + Flyway migration (`V11__requirements.sql`):
  `projectId`, `key`, `title`, `status` (default `'ACTIVE'`), `priority`
  (nullable), `ownerId`, unique on `(project_id, key)`
- `RequirementRepository`
- A dedicated `requirement` module (its own controller, not nested inside
  `ProjectController`) - unlike `ProjectMember`, `Requirement` is a
  standalone domain entity that later features attach to, not a join table
- REST endpoints, nested under `/api/v1/projects/{projectId}/requirements`:
  create, get by id, list - protected; the project must belong to the
  caller's org (reusing `ProjectRepository.findByIdAndOrganizationId`, same
  as every prior feature's project-scoping check)
- Server-generated sequential `key` (`{projectKey}-1`, `{projectKey}-2`,
  ...) - not client-supplied, unlike `Project.key`
- `ownerId` defaults to the creating user, not client-supplied (matching
  how `Project.ownerId` already works - no client override exists there
  either)
- Minimal protected frontend page at `/projects/[projectId]/requirements`:
  create form (title only) + list, plus a "Requirements" link added next
  to each project in the existing `/projects/new` list so there's a real
  path to it

## Out of scope

- `releaseId`. The data model lists it, but `Release` doesn't exist yet
  (build-plan item 8, several features away). Adding a column with no
  table to reference is meaningless, so it's deferred - same call already
  made for 3b's permission catalog (`don't invent schema for modules that
  don't exist yet`). Feature 8 adds `release_id` via its own migration
  when `Release` lands
- Requirement update/delete - same no-update precedent as `Team` (3a) and
  `Role` (3b)
- Enforcing any status workflow (e.g. draft -> active -> archived
  transitions). `status` is a plain string field, same lightweight
  precedent as `Project.status` - no state machine, no enum validation
- Enforcing any fixed `priority` values. Nullable free-text field, client
  optional, no enum - nothing in the plan specifies the actual value set
- Linking a requirement to a test case (`RequirementTestCase`). That join
  needs `TestCase` to exist first (build-plan item 5), so it belongs to a
  later feature, most likely 13 (Traceability) or wherever the join is
  first needed
- Concurrent-create key collisions. The sequential key is computed as
  `count(existing requirements in this project) + 1`; the DB's unique
  constraint on `(project_id, key)` prevents two requirements from ever
  actually sharing a key, but a rare simultaneous double-create could
  surface as a 500 rather than a friendly retry. Acceptable for an
  internal MVP tool; not adding locking or a sequence table for this

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Requirement entity, migration, repository** -
  `Requirement` (`projectId`, `key`, `title`, `status` default `'ACTIVE'`,
  `priority` nullable, `ownerId`), unique `(project_id, key)`.
  `V11__requirements.sql`. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 11 rows (V1-V11).
- [x] **Step 2 - Protected, project-scoped REST endpoints with
  server-generated keys** - `POST /api/v1/projects/{projectId}/requirements`
  (`title` required via Bean Validation, `status`/`priority` optional),
  `GET /api/v1/projects/{projectId}/requirements/{id}`, `GET
  /api/v1/projects/{projectId}/requirements`. The project must belong to
  the caller's org (404 if not, existing `ProjectController` pattern - a
  requirement never stores its own org, it inherits scoping through its
  project). Key is computed server-side as `{project.key}-{n}`, `n` being
  the 1-based count of existing requirements in that project plus one.
  `ownerId` is always the creating user. *Done when:* `flyway_schema_history`
  is unchanged at 11 rows (this step adds no migration); valid `POST` with
  only `title` -> 201 with key `{projectKey}-1`, `status: "ACTIVE"`,
  `ownerId` equal to the caller; a second `POST` in the same project ->
  key `{projectKey}-2`; `GET` list includes both, in key order; `GET` by
  id returns one of them; `POST` missing `title` -> 400; `POST` to a
  project inserted directly under a different org -> 404; `GET` by id for
  a project under a different org -> 404; no token -> 401.
- [x] **Step 3 - Minimal protected frontend: create and list requirements
  for a project** - `/projects/[projectId]/requirements` page, same
  redirect-if-unauthenticated pattern as `/teams/new`, form (title only)
  POSTs to the backend, shows the result and existing requirements with
  their keys; add a "Requirements" link next to each project in the
  existing `/projects/new` list so the page is reachable from the UI.
  *Done when:* browser-driven - unauthenticated visit to
  `/projects/{id}/requirements` redirects to sign-in; signed in, clicking
  a project's "Requirements" link from `/projects/new` navigates there;
  creating a requirement with just a title shows it with its
  auto-generated key in the re-fetched list.

## Files / areas

- `backend/.../requirement/entity/Requirement.java`
- `backend/.../requirement/repository/RequirementRepository.java`
- `backend/.../requirement/dto/CreateRequirementRequest.java`, `RequirementDto.java`
- `backend/.../requirement/mapper/RequirementMapper.java`
- `backend/.../requirement/service/RequirementService.java`
- `backend/.../requirement/controller/RequirementController.java`
- `backend/src/main/resources/db/migration/V11__requirements.sql`
- `frontend/services/requirements.ts`, `frontend/actions/requirements.ts`, `frontend/app/projects/[projectId]/requirements/`
- `frontend/app/projects/new/page.tsx` - add a "Requirements" link per project

## Data / contracts

**Load-bearing:**
- `RequirementDto { id, projectId, key, title, status, priority, ownerId, createdAt }` -
  feature 5's `TestCase` and eventually the `RequirementTestCase`
  traceability join will reference `Requirement.id`; this shape and the
  key format (`{projectKey}-{n}`) should not change once those are built
  against it.
- Server-generated sequential key pattern (`{projectKey}-{n}`, computed
  from a per-project count, enforced unique via `(project_id, key)`) -
  the first feature to establish this; `TestCase` and `Defect` (data model
  fields `key` on both) are expected to follow the identical pattern when
  their features land.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 1-2, including a cross-org
project attempt) and browser evidence (Step 3), matching every prior
feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`, and
  `ProjectRepository.findByIdAndOrganizationId` directly - a requirement's
  org scoping is entirely inherited through its project, there is no
  `organizationId` column on `Requirement` itself (matches the data
  model: only `projectId` is listed).
- Give `Requirement` its own module (`requirement/`), not endpoints bolted
  onto `ProjectController` - unlike `ProjectMember` in 3c, this is a
  standalone entity with its own growing feature surface ahead of it
  (folders, tags, versions in feature 5; traceability in 13).
- Compute the key inside the service method that creates the requirement,
  after resolving the project (need `project.getKey()`), not in the
  controller or a DTO default.
- No `releaseId` column at all yet - don't add a nullable column "for
  later"; feature 8 adds it via its own migration when `Release` exists.

## Build notes (from implementation)

- **Infrastructure hiccup, not a code bug**: at the start of Step 1, the
  Spring Boot app failed to start with `Connection to localhost:5432
  refused`. Root cause was the Docker daemon itself not running (likely a
  machine/Docker Desktop restart since the prior session), not a schema
  or code problem - confirmed via `docker-compose ps` returning nothing.
  Restarted Docker Desktop, ran `docker-compose up -d`. The Postgres
  container reattached to its original volume (`created 27 hours ago`,
  not fresh), confirmed by `flyway_schema_history` reading 10 rows
  (matching the state at the end of feature 3c) before `V11` was applied.
  No data was lost; this was purely an environment restart, not a defect.
- **Same restart also cleared the session's ephemeral Playwright
  scratchpad** (`/tmp` cleanup). Reinstalled Playwright fresh into the
  scratchpad only, per the established ephemeral-install convention -
  never added to `frontend/package.json`.
- The server-generated sequential key (`{projectKey}-{n}`) worked exactly
  as designed on the first attempt: `TMP-1`, `TMP-2` via `curl`, then
  `TMP-3` via the browser flow in the same project, confirming the count-
  based generation is stable across both request paths.
