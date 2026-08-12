# Feature: Test cycles

**From build-plan:** feature 10a
**Status:** complete

## Goal

Let a project create test cycles under a test plan, each assigned to a
release, build, and environment (build-plan's own wording: "create cycles
under a plan, assign build/environment"). A `TestCycle` is the container
10b's test case selection attaches to, and feature 11 (Test execution)
ultimately runs through. This sub-feature is the container only - no test
case selection here, that's 10b.

## In scope

- `TestCycle` JPA entity + Flyway migration (`V26__test_cycles.sql`):
  `projectId`, `testPlanId` (required, `NOT NULL REFERENCES test_plans(id)`
  - a cycle is created "under a plan" per the build-plan wording), `releaseId`
  (required, same reasoning as `TestPlan.releaseId`/`Build.releaseId`),
  `buildId` (required - "assign build" per build-plan wording),
  `environmentId` (required - "assign environment"), `name`, `status`
  (default `'ACTIVE'` - unlike `TestPlan`'s `'DRAFT'`, a cycle represents an
  active test-run window you create when you're ready to start testing, not
  a future plan), `ownerId` (server-derived from the JWT, same pattern as
  `TestPlan.ownerId`), `startDate`/`endDate` (both nullable `DATE`, the
  "date range" the data model calls for)
- `TestCycleRepository`, with `findByIdAndProjectId` (same-project lookup)
  and `findByProjectIdOrderByCreatedAtAsc`
- A dedicated `testcycle` module (its own controller)
- REST endpoints, nested under `/api/v1/projects/{projectId}/test-cycles`:
  create, get by id, list - protected, project must belong to caller's org
  (existing pattern)
- **Four independent same-project validations on create**: `testPlanId`,
  `releaseId`, `buildId`, and `environmentId` must each resolve to an
  entity in the *same project* (reusing `TestPlanRepository`/
  `ReleaseRepository`/`BuildRepository`/`EnvironmentRepository`'s existing
  `findByIdAndProjectId` methods - all four already exist from 8a/8b/8c/9,
  no new repository methods needed) - 404 if any doesn't resolve
- Minimal protected frontend page at `/projects/[projectId]/test-cycles`:
  create form (name + four selects: test plan, release, build,
  environment) + a dense table list (Name, Test Plan, Release, Build,
  Environment, Status, Start Date, End Date - joined client-side the same
  way Builds/Test Plans already join release names)
- An "Test Cycles" link-card added to the project hub
  (`/projects/[projectId]/page.tsx`), matching the established pattern

## Out of scope

- `TestCycleCase` (adding/removing/browsing test cases into a cycle).
  Explicitly 10b - this sub-feature only builds the cycle container.
- Any assignment of testers. `assigneeId` lives on `TestCycleCase`
  (10b's table), not `TestCycle` itself.
- `TestExecution` (recording pass/fail/blocked/skipped). Explicitly
  feature 11 - a cycle exists before anything runs against it.
- Test cycle update/delete. Same no-update precedent as every catalog
  entity so far.
- Enforcing a status workflow (e.g. `ACTIVE` -> `COMPLETED` transitions).
  Plain string field, same lightweight precedent as every other status
  field.
- Cross-filtering the four selects against each other (e.g. only showing
  builds that belong to the selected release). Every other multi-select
  form in this app (`Build`, `TestPlan`) lists all options in the project
  with no interdependent filtering - `TestCycle`'s four selects follow the
  same simple pattern. Smarter filtering is a UI polish concern for later,
  not required to prove the feature.
- Filtering `TestCycle` lists, or adding cycles to feature 7's search/filter
  set. Not implied by this sub-feature.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestCycle entity, migration, repository** - `TestCycle`
  (`projectId`, `testPlanId`/`releaseId`/`buildId`/`environmentId` all NOT
  NULL FKs, `name` NOT NULL, `status` default `'ACTIVE'`, `ownerId` NOT
  NULL, `startDate`/`endDate` nullable). `V26__test_cycles.sql`. *Done
  when:* app starts cleanly under `ddl-auto: validate`,
  `flyway_schema_history` has 26 rows (V1-V26).
- [x] **Step 2 - Protected, project-scoped TestCycle endpoints** - `POST
  /api/v1/projects/{projectId}/test-cycles` (`name`, `testPlanId`,
  `releaseId`, `buildId`, `environmentId` all required via Bean
  Validation; `status`/`startDate`/`endDate` optional; `ownerId` always
  set server-side, never from the request body), `GET
  /api/v1/projects/{projectId}/test-cycles/{id}`, `GET
  /api/v1/projects/{projectId}/test-cycles`. Each of the four FKs
  validated same-project independently. *Done when:*
  `flyway_schema_history` unchanged at 26 rows; valid `POST` with all
  required fields (each referencing a real same-project entity) -> 201,
  `status: "ACTIVE"`, `ownerId` matches the caller; `POST` missing any one
  of `name`/`testPlanId`/`releaseId`/`buildId`/`environmentId` -> 400 (four
  separate proofs, one per field); `POST` with a `testPlanId` from a
  *different* project (same org) -> 404 (and the same proof repeated for
  `releaseId`, `buildId`, `environmentId` - four separate cross-project
  isolation proofs, not one assumed to cover all four); `POST` with any
  random non-existent id for any of the four -> 404; `GET` list includes
  it; `GET` by id returns it; `POST` to a project under a different org ->
  404; no token -> 401.
- [x] **Step 3 - Minimal protected frontend: test cycle catalog
  create/list + hub card** - `/projects/[projectId]/test-cycles` page,
  same redirect-if-unauthenticated pattern as every other project
  sub-page; fetches test plans, releases, builds, and environments in
  parallel to populate the four selects and to join display names into
  the table; if any of the four lists is empty, the create form is
  replaced with a message naming exactly which prerequisite(s) are
  missing (e.g. "Create a test plan and a build first before creating a
  test cycle.") rather than submitting with an empty select; project hub
  gains a "Test Cycles" link-card. *Done when:* browser-driven -
  unauthenticated visit redirects to sign-in; signed in with at least one
  test plan/release/build/environment already created, creating a test
  cycle shows it in the re-fetched table with all four joined names and
  its default `ACTIVE` status; with any prerequisite missing, the
  fallback message names it correctly; the project hub shows a working
  "Test Cycles" link.

## Files / areas

- `backend/.../testcycle/entity/TestCycle.java`
- `backend/.../testcycle/repository/TestCycleRepository.java`
- `backend/.../testcycle/dto/CreateTestCycleRequest.java`, `TestCycleDto.java`
- `backend/.../testcycle/mapper/TestCycleMapper.java`
- `backend/.../testcycle/service/TestCycleService.java`
- `backend/.../testcycle/controller/TestCycleController.java`
- `backend/src/main/resources/db/migration/V26__test_cycles.sql`
- `frontend/services/testcycles.ts`, `frontend/actions/testcycles.ts`,
  `frontend/app/projects/[projectId]/test-cycles/`
- `frontend/app/projects/[projectId]/page.tsx` (adds the Test Cycles card)

## Data / contracts

**Load-bearing:**
- `TestCycleDto { id, projectId, testPlanId, releaseId, buildId,
  environmentId, name, status, ownerId, startDate, endDate, createdAt }` -
  10b's `TestCycleCase.cycleId` and feature 11's `TestExecution.cycleId`
  both reference `TestCycle.id` directly. Don't change this shape once
  those land.
- All four FK fields (`testPlanId`/`releaseId`/`buildId`/`environmentId`)
  are required and immutable (no update endpoint) - any later feature
  reading `TestCycleDto` can rely on all four always being present, never
  `null`.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verify with `curl` output (Steps 1-2, including all four same-project
validations and all four missing-field 400 cases) and browser evidence
(Step 3), matching every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId`,
  `TestPlanRepository.findByIdAndProjectId`,
  `ReleaseRepository.findByIdAndProjectId`,
  `BuildRepository.findByIdAndProjectId`, and
  `EnvironmentRepository.findByIdAndProjectId` directly - all four already
  exist, don't add new lookup methods.
- `ownerId` follows `TestPlan.ownerId`'s exact pattern: set from
  `userService.resolveOrProvisionUser(jwt).getId()` in the service layer,
  never accepted as a request field.
- The four same-project checks in the service are mechanical repeats of
  the exact same pattern (`xRepository.findByIdAndProjectId(id,
  project.getId()).orElseThrow(...)`) - don't design a generic/shared
  validation helper for this, four explicit calls is clearer and matches
  how every prior multi-FK check in this codebase has been written inline.
- Follow `frontend/lib/badges.ts`'s `statusBadgeClasses` for the table's
  status column - `ACTIVE` renders success, everything else neutral, same
  as every other status column.
- The project hub's `SECTIONS` array gains one more entry
  (`{ href: "test-cycles", label: "Test Cycles", icon: "Cy" }`) - same
  icon-tile pattern as the other 8 entries.
- Ship this page already themed on arrival (dense table, `shadow-card`
  form, `bg-accent` button) using the now-established conventions - no
  follow-up re-theme fix needed, matching 8c's precedent.

## Build notes (from implementation)

Built cleanly against the spec - the four-way cross-entity validation
transferred directly from established patterns, just repeated four times
instead of once.

- All 16 endpoint cases in Step 2's done-when were proven with real `curl`
  output against a freshly restarted backend: 5 missing-field proofs, 2
  random-nonexistent-id proofs, 4 *independent* cross-project isolation
  proofs (a sibling project's own full set of test plan/release/build/
  environment inserted via real API calls, one FK swapped at a time),
  cross-org project 404, list/get/no-token. Every fixture was cleaned up
  after.
- Step 3's frontend verification exercised both branches of the
  "prerequisites" logic, not just the happy path: a real project with all
  four prerequisites got a full create-and-list proof, and a sibling
  project missing all four got the fallback-message proof.
- One thing caught and fixed after the initial review, before completing:
  the missing-prerequisites message joined 4 items with repeated "and"
  ("a test plan and a release and a build and an environment"), which
  reads badly at that length. Fixed to standard list grammar ("a test
  plan, a release, a build, and an environment") - caught by actually
  triggering the fallback against a real project missing everything,
  not assumed correct from the code.
- This closes out the container half of build-plan item 10; 10b (browsing
  the test case repository and selecting cases into a cycle) is next and
  unblocked now that `TestCycle` exists.
