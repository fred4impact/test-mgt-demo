# Feature: Environments

**From build-plan:** feature 8c
**Status:** complete

## Goal

Let a project record environments (e.g. Staging, QA, Production) it tests
against. This is the last of the three 8x sub-items (`Release` from 8a,
`Build` from 8b) and, together with `Build`, the final dependency of
feature 10 (Test cycles): `TestCycle.environmentId` needs `Environment` to
exist first. Unlike `Release`/`Build`, `Environment` is standalone - no
`releaseId`, no cross-entity reference at all.

## In scope

- `Environment` JPA entity + Flyway migration (`V25__environments.sql`):
  `projectId`, `name` (required), `type` (nullable - free-text like
  `Release.status`/`Build.status`, e.g. `"STAGING"`/`"PRODUCTION"`, no enum
  validation, same lightweight precedent as every status-like field so
  far), `url` (nullable)
- `EnvironmentRepository`, with `findByIdAndProjectId` (same-project
  lookup, same shape as every other project-scoped repository) and
  `findByProjectIdOrderByCreatedAtAsc`
- A dedicated `environment` module (its own controller)
- REST endpoints, nested under `/api/v1/projects/{projectId}/environments`:
  create, get by id, list - protected, project must belong to caller's org
  (existing pattern)
- Minimal protected frontend page at `/projects/[projectId]/environments`:
  create form (name, type, url) + list
- An "Environments" link-card added to the project hub
  (`/projects/[projectId]/page.tsx`), matching the pattern 8b and 9 already
  established

## Out of scope

- `TestCycle`. Explicitly feature 10 - this feature only builds the
  environment record, not what runs against it.
- Environment update/delete. Same no-update precedent as every catalog
  entity so far (`Team`, `Role`, `Requirement`, `TestFolder`, `Tag`,
  `TestSuite`, `Release`, `Build`, `TestPlan`).
- Any status/health field or connectivity check (e.g. pinging `url` to
  confirm the environment is reachable). `url` is a plain stored string,
  nothing validates or dereferences it.
- Any FK from other entities *to* `Environment` beyond what already exists
  in the data model (`TestCycle.environmentId`, `Defect.environmentId`,
  `AutomationRun.environmentId`). Those are the other entities' job when
  they're built (items 10, 12, 16) - `Environment` itself doesn't need to
  know about them.
- Any cross-entity validation on create. Unlike `Build`/`TestPlan`,
  `Environment` has no `releaseId` or other reference to validate - create
  only needs the existing project-scope check.
- Filtering `Environment` lists, or adding environments to feature 7's
  search/filter set. Not implied by this feature.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Environment entity, migration, repository** - `Environment`
  (`projectId`, `name` NOT NULL, `type` nullable, `url` nullable).
  `V25__environments.sql`. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 25 rows (V1-V25).
- [x] **Step 2 - Protected, project-scoped Environment endpoints** - `POST
  /api/v1/projects/{projectId}/environments` (`name` required via Bean
  Validation, `type`/`url` optional), `GET
  /api/v1/projects/{projectId}/environments/{id}`, `GET
  /api/v1/projects/{projectId}/environments`. *Done when:*
  `flyway_schema_history` unchanged at 25 rows; valid `POST` with only
  `name` -> 201; `POST` with `type`/`url` set -> 201, both present in the
  response; `POST` missing `name` -> 400; `GET` list includes it; `GET` by
  id returns it; `POST` to a project inserted under a different org -> 404;
  no token -> 401.
- [x] **Step 3 - Minimal protected frontend: environment catalog
  create/list + hub card** - `/projects/[projectId]/environments` page,
  same redirect-if-unauthenticated pattern as every other project
  sub-page; create form has `name`, `type`, `url` (all in the form, only
  `name` required) POSTs to the backend; list renders as a dense table
  (Name, Type, URL - matching Requirements'/Releases' table pattern, not
  a card/chip grid, since 3 real columns of data is tabular, not
  label-like the way `Tag`/`TestSuite` are) with nullable `type`/`url`
  shown as `"-"`; project hub (`/projects/[projectId]/page.tsx`) gains an
  "Environments" link-card. *Done when:* browser-driven - unauthenticated
  visit redirects to sign-in; signed in, creating an environment (with
  and without optional fields) shows it correctly in the re-fetched
  table, nullable fields rendering as `"-"`; the project hub shows a
  working "Environments" link.

## Files / areas

- `backend/.../environment/entity/Environment.java`
- `backend/.../environment/repository/EnvironmentRepository.java`
- `backend/.../environment/dto/CreateEnvironmentRequest.java`, `EnvironmentDto.java`
- `backend/.../environment/mapper/EnvironmentMapper.java`
- `backend/.../environment/service/EnvironmentService.java`
- `backend/.../environment/controller/EnvironmentController.java`
- `backend/src/main/resources/db/migration/V25__environments.sql`
- `frontend/services/environments.ts`, `frontend/actions/environments.ts`,
  `frontend/app/projects/[projectId]/environments/`
- `frontend/app/projects/[projectId]/page.tsx` (adds the Environments card)

## Data / contracts

**Load-bearing:**
- `EnvironmentDto { id, projectId, name, type, url, createdAt }` - feature
  10's `TestCycle.environmentId`, feature 12's `Defect.environmentId`, and
  feature 16's `AutomationRun.environmentId` all reference `Environment.id`
  directly. Don't change this shape once those land.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verify with `curl` output (Step 2) and browser evidence (Step 3), matching
every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId` directly.
- This is the simplest of the three 8x sub-items - no cross-entity
  validation at all, since `Environment` has no `releaseId` or other FK on
  create. Don't add one that isn't in the data model.
- The project hub's `SECTIONS` array (in
  `app/projects/[projectId]/page.tsx`) gains one more entry
  (`{ href: "environments", label: "Environments", icon: "Ev" }`) -
  follow the exact same icon-tile card pattern already used for the other
  7 entries, and match the dense/card/chip theming conventions already
  established (`frontend/lib/badges.ts`, `bg-accent-soft`/`text-accent`
  icon tiles, `shadow-card` on the create form) rather than shipping this
  new page unstyled - it should look consistent with every other project
  sub-page on arrival, not need a follow-up re-theme fix like 8b/9 did.

## Build notes (from implementation)

Built cleanly against the spec - the simplest of the three 8x sub-items,
exactly as predicted: no cross-entity validation logic at all, since
`Environment` has no `releaseId` or other FK on create.

- All 7 endpoint cases in Step 2's done-when were proven with real `curl`
  output against a freshly restarted backend, including a genuine cross-org
  isolation proof (a fresh org/user/project inserted via `psql`, cleaned up
  after).
- The frontend shipped styled on arrival as intended - no follow-up
  re-theme fix needed, unlike 8b (Builds) and 9 (Test plans) which were
  built before the theme existed and needed a separate pass later. Verified
  with a real signed-in flow: created one environment with just a name and
  a second with type + URL, both rendering correctly in the dense table
  with nullable fields as `"-"`.
- This closes out build-plan item 8 (Releases, builds & environments)
  entirely - all three sub-items (8a, 8b, 8c) are now complete, so the
  parent item gets checked off too.
- `TestCycle`'s two prerequisites (`Build` from 8b, `Environment` from this
  feature) both now exist, unblocking feature 10 (Test cycles).
