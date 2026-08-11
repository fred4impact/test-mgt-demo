# Feature: Test plans

**From build-plan:** feature 9
**Status:** complete

## Goal

Let a project create test plans, each scoped to a `Release` (build-plan's own
wording: "create test plans scoped to a release"). A `TestPlan` is the
container feature 10 (Test cycles) will hang cycles off of, and feature 11
(Test execution) ultimately runs through. Unlike feature 10, this one has no
dependency on `Build`/`Environment` (8b/8c) - it only references `Release`.

## In scope

- `TestPlan` JPA entity + Flyway migration (`V24__test_plans.sql`):
  `projectId`, `releaseId` (required, `NOT NULL REFERENCES releases(id)` -
  unlike `Requirement`/`TestCase`'s optional `releaseId` from 8a, a plan is
  *scoped to* a release by definition, same reasoning as `Build.releaseId`
  from 8b), `name`, `status` (default `'DRAFT'` - a plan starts as a draft
  before cycles run under it, distinct from `Release`'s `'PLANNED'` and
  every other catalog entity's `'ACTIVE'`), `ownerId` (server-derived from
  the JWT, never client-supplied - same pattern as
  `Requirement.ownerId`/`Project.ownerId`), `startDate`/`endDate` (both
  nullable `DATE`)
- `TestPlanRepository`, with `findByIdAndProjectId` (same-project lookup,
  same shape as `ReleaseRepository`'s) and `findByProjectIdOrderByCreatedAtAsc`
- A dedicated `testplan` module (its own controller)
- REST endpoints, nested under `/api/v1/projects/{projectId}/test-plans`:
  create, get by id, list - protected, project must belong to caller's org
  (existing pattern)
- `releaseId` on create must resolve to a `Release` in the *same project*
  (same-project cross-entity check, reusing `ReleaseRepository.findByIdAndProjectId`
  from 8a) - 404 if not
- Minimal protected frontend page at `/projects/[projectId]/test-plans`:
  create form (name + a release picker, since `releaseId` is required) +
  list showing each plan's name, status, and release
- A "Test Plans" link-card added to the project hub
  (`/projects/[projectId]/page.tsx`), alongside the "Builds" card 8b just
  added - matching that same stated intent to add a card per section as it
  ships

## Out of scope

- `TestCycle`. Explicitly feature 10 - this feature only builds the plan
  container, not what runs under it.
- Test plan update/delete. Same no-update precedent as every catalog entity
  so far (`Team`, `Role`, `Requirement`, `TestFolder`, `Tag`, `TestSuite`,
  `Release`, `Build`).
- Enforcing a status workflow (e.g. `DRAFT` -> `ACTIVE` -> `CLOSED`
  transitions). `status` is a plain string field, same lightweight precedent
  as every other `status` field in this build - no state machine, no enum
  validation.
- Assigning test cases to a plan directly. In this data model, test cases
  attach to a *cycle* (`TestCycleCase`, feature 10's job), not the plan
  itself - a plan has no direct test-case membership to build here.
- Any FK from other entities *to* `TestPlan` beyond what already exists in
  the data model (`TestCycle.testPlanId` is feature 10's job).
- Filtering `TestPlan` lists by `releaseId` or status, or adding test plans
  to feature 7's search/filter set. Not implied by this feature.
- `Build`/`Environment` (8b/8c) - a test plan references only `Release`, not
  build or environment; those attach lower down, at the `TestCycle` level
  (`TestCycle.buildId`/`environmentId` per the data model), which is feature
  10's concern.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestPlan entity, migration, repository** - `TestPlan`
  (`projectId`, `releaseId` NOT NULL FK to `releases(id)`, `name`, `status`
  default `'DRAFT'`, `ownerId` NOT NULL, `startDate`/`endDate` nullable).
  `V24__test_plans.sql`. *Done when:* app starts cleanly under `ddl-auto:
  validate`, `flyway_schema_history` has 24 rows (V1-V24).
- [x] **Step 2 - Protected, project-scoped TestPlan endpoints** - `POST
  /api/v1/projects/{projectId}/test-plans` (`name` and `releaseId` required
  via Bean Validation, `status`/`startDate`/`endDate` optional, `ownerId`
  always set server-side from the resolved user, never from the request
  body), `GET /api/v1/projects/{projectId}/test-plans/{id}`, `GET
  /api/v1/projects/{projectId}/test-plans`. *Done when:* `flyway_schema_history`
  unchanged at 24 rows; valid `POST` with `name` + a real same-project
  `releaseId` -> 201, `status: "DRAFT"`, `ownerId` matches the caller;
  `POST` with `startDate`/`endDate` set -> 201, both present in the
  response; `POST` missing `name` -> 400; `POST` missing `releaseId` -> 400;
  `POST` with a `releaseId` from a release inserted directly under a
  *different* project (same org) -> 404; `POST` with a random non-existent
  `releaseId` -> 404; `GET` list includes it; `GET` by id returns it; `POST`
  to a project inserted under a different org -> 404; no token -> 401.
- [x] **Step 3 - Minimal protected frontend: test plan catalog create/list +
  hub card** - `/projects/[projectId]/test-plans` page, same
  redirect-if-unauthenticated pattern as `/projects/[projectId]/releases`;
  create form has `name` and a `<select>` populated from the project's
  releases (reuse `listReleases` from `services/releases.ts`), POSTs to the
  backend; list shows each plan's name, status, and release name (join
  client-side against the already-fetched releases list); project hub
  (`/projects/[projectId]/page.tsx`) gains a "Test Plans" link-card
  alongside the existing six. *Done when:* browser-driven -
  unauthenticated visit redirects to sign-in; signed in with at least one
  release already created, creating a test plan against that release shows
  it (with its default `DRAFT` status and the release's name) in the
  re-fetched list; if the project has zero releases yet, the form clearly
  shows there's nothing to select rather than submitting an empty
  `releaseId`; the project hub shows a working "Test Plans" link.

## Files / areas

- `backend/.../testplan/entity/TestPlan.java`
- `backend/.../testplan/repository/TestPlanRepository.java`
- `backend/.../testplan/dto/CreateTestPlanRequest.java`, `TestPlanDto.java`
- `backend/.../testplan/mapper/TestPlanMapper.java`
- `backend/.../testplan/service/TestPlanService.java`
- `backend/.../testplan/controller/TestPlanController.java`
- `backend/src/main/resources/db/migration/V24__test_plans.sql`
- `frontend/services/testplans.ts`, `frontend/actions/testplans.ts`,
  `frontend/app/projects/[projectId]/test-plans/`
- `frontend/app/projects/[projectId]/page.tsx` (adds the Test Plans card)

## Data / contracts

**Load-bearing:**
- `TestPlanDto { id, projectId, releaseId, name, status, ownerId, startDate,
  endDate, createdAt }` - feature 10's `TestCycle.testPlanId` references
  `TestPlan.id` directly. Don't change this shape once feature 10 is built.
- `releaseId` and `ownerId` are both required and immutable (no update
  endpoint) - any later feature reading `TestPlanDto` can rely on both
  always being present, never `null`.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verify with `curl` output (Steps 1-2, including the same-project validation
and the missing-`releaseId`/missing-`name` 400 cases) and browser evidence
(Step 3), matching every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId`, and
  `ReleaseRepository.findByIdAndProjectId` (already exists from 8a) directly
  - don't add a second same-project lookup method for releases.
- `ownerId` follows `Requirement.ownerId`'s exact pattern: set from
  `userService.resolveOrProvisionUser(jwt).getId()` in the service layer,
  never accepted as a request field.
- `CreateTestPlanRequest.releaseId` is `@NotNull` (required) - same shape
  8b's `CreateBuildRequest.releaseId` just used, not 8a's optional
  retrofits.
- Step 3's release `<select>` reuses `listReleases(accessToken, projectId)`
  from `frontend/services/releases.ts` (built in 8a) - no new backend
  endpoint needed to populate it.
- The project hub's `SECTIONS` array (in
  `app/projects/[projectId]/page.tsx`) gains one more entry
  (`{ href: "test-plans", label: "Test Plans" }`), joining the "Builds"
  entry 8b just added - a one-line addition, not a hub redesign.
- No date parsing surprises: `startDate`/`endDate` are plain `LocalDate`
  (`yyyy-MM-dd`), Jackson handles this natively via the `JavaTimeModule`
  already confirmed present (5c's build notes) - no custom (de)serializer
  needed.

## Build notes (from implementation)

Built cleanly against the spec - every pattern (required `releaseId` +
same-project validation from 8b, server-derived `ownerId` from 4, hub card
addition from 8b) transferred directly, no code surprises.

- All 10 endpoint cases in Step 2's done-when were proven with real `curl`
  output, including genuine cross-project (sibling release, same org) and
  cross-org (fresh org/user/project inserted via `psql`) isolation proofs,
  both cleaned up after. Keycloak access tokens expired mid-verification
  more than once (they are very short-lived in this dev realm) and produced
  a transient false `401` on a case that had previously passed - not a bug,
  just re-issued a fresh token and reran immediately.
- The frontend browser verification passed cleanly on the first attempt,
  reusing the submit-button-scoping fix discovered while verifying 8b
  (`AppNav`'s "Sign out" button also has `type="submit"` and sits earlier in
  the DOM than any page's own submit button).
- This feature has no further build-plan blockers of its own; feature 10
  (Test cycles) remains gated on 8c (Environments) landing first, since
  `TestCycle` needs both `Build` and `Environment` to exist.
