# Feature: Releases

**From build-plan:** feature 8a
**Status:** complete

## Goal

Let a project create releases, and honor two explicit promises made
earlier in this build: feature 4's spec said "`Release` doesn't exist yet
... feature 8 adds `release_id` via its own migration when it lands," and
5b's spec repeated the same commitment for `TestCase`. This feature is
where those two deferred columns finally land, tied directly to `Release`
existing for the first time.

## In scope

- `Release` JPA entity + Flyway migration (`V20__releases.sql`):
  `projectId`, `name`, `version` (nullable), `status` (default
  `'PLANNED'` - a release starts planned, unlike `Project`/`Requirement`/
  `TestCase` which default `'ACTIVE'` since those are usable immediately),
  `startDate`, `releaseDate` (both nullable `DATE`)
- `ReleaseRepository`
- A dedicated `release` module (its own controller)
- REST endpoints, nested under `/api/v1/projects/{projectId}/releases`:
  create, get by id, list - protected, project must belong to caller's
  org (existing pattern)
- **Retrofit `release_id` onto `Requirement`** (`V21__requirements_release_id.sql`,
  `ALTER TABLE requirements ADD COLUMN release_id UUID REFERENCES
  releases(id)`, nullable): add the field to the entity, `RequirementDto`,
  and `CreateRequirementRequest` (optional); when provided, validate it
  resolves to a `Release` in the *same project* (same-project cross-entity
  check, same shape as every prior one in this build)
- **Retrofit `release_id` onto `TestCase`** (`V22__test_cases_release_id.sql`,
  same shape): add the field to the entity, `TestCaseDto`,
  `CreateTestCaseRequest`, and `UpdateTestCaseRequest` (all optional, and
  for update, `null` means "don't touch" per 5c's already-established
  partial-update convention); same same-project validation on both create
  and update
- Minimal protected frontend page at `/projects/[projectId]/releases`:
  create form (name only, the rest of the fields are optional and skipped
  by the minimal form, same precedent as `Tag`/`TestSuite`) + list

## Out of scope

- `Build` and `Environment`. Explicitly 8b and 8c
- Release update/delete. Same no-update precedent as every catalog entity
  so far (`Team`, `Role`, `Requirement`, `TestFolder`, `Tag`, `TestSuite`)
- Enforcing a release status workflow (e.g. `PLANNED` -> `IN_PROGRESS` ->
  `RELEASED` transitions). `status` is a plain string field, same
  lightweight precedent as every other `status` field in this build - no
  state machine, no enum validation
- Adding a `releaseId` selector to the existing Requirements or Test Cases
  frontend create/edit forms. This feature adds the *capability*
  (schema + API); wiring it into the existing forms is a UI-only follow-up
  that can happen anytime without needing another backend change - not
  bundling it here keeps this feature's diff focused on the actual promise
  being fulfilled (the column existing and being usable via the API)
- Filtering `Requirement`/`TestCase` lists by `releaseId`. Feature 7
  (Search & filters) is already merged; extending its filter set is a
  separate, later concern, not implied by this feature
- Any FK from `Release` back to a build/cycle/plan. Those references are
  the *other* entities' job when they're built (`TestPlan.releaseId`,
  `Build.releaseId` in 8b, etc.) - `Release` itself doesn't need to know
  about them

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Release entity, migration, repository** - `Release`
  (`projectId`, `name`, `version` nullable, `status` default `'PLANNED'`,
  `startDate`/`releaseDate` nullable `DATE`). `V20__releases.sql`. *Done
  when:* app starts cleanly under `ddl-auto: validate`,
  `flyway_schema_history` has 20 rows (V1-V20).
- [x] **Step 2 - Protected, project-scoped Release endpoints** - `POST
  /api/v1/projects/{projectId}/releases` (`name` required via Bean
  Validation, `version`/`status`/`startDate`/`releaseDate` optional),
  `GET /api/v1/projects/{projectId}/releases/{id}`, `GET
  /api/v1/projects/{projectId}/releases`. *Done when:*
  `flyway_schema_history` unchanged at 20 rows (Step 1 already added the
  migration); valid `POST` with only `name` -> 201, `status: "PLANNED"`;
  `POST` with `version`/dates set -> 201, all present in the response;
  `GET` list includes it; `GET` by id returns it; `POST` missing `name`
  -> 400; `POST` to a project inserted under a different org -> 404; no
  token -> 401.
- [x] **Step 3 - Retrofit releaseId onto Requirement** -
  `V21__requirements_release_id.sql` adds nullable `release_id` to
  `requirements`; `Requirement` entity, `RequirementDto`,
  `CreateRequirementRequest` gain `releaseId`; when provided at create
  time, it must resolve to a `Release` in the same project (404 if not,
  same technique as `folderId`'s same-project check). *Done when:*
  `flyway_schema_history` has 21 rows; `POST .../requirements` with a
  valid `releaseId` -> 201, `releaseId` present in the response; `POST`
  with no `releaseId` -> 201, `releaseId: null` (unchanged default
  behavior); `POST` with a `releaseId` from a release inserted directly
  under a *different* project (same org) -> 404; `POST` with a random
  non-existent `releaseId` -> 404.
- [x] **Step 4 - Retrofit releaseId onto TestCase** - same shape as Step
  3, applied to `TestCase`: `V22__test_cases_release_id.sql`, entity,
  `TestCaseDto`, `CreateTestCaseRequest`, and `UpdateTestCaseRequest`
  (partial-update semantics: omitted `releaseId` on `PUT` leaves the
  existing value untouched, matching 5c's convention for every other
  field). *Done when:* `flyway_schema_history` has 22 rows; `POST
  .../test-cases` with a valid `releaseId` -> 201, present in response;
  `PUT .../test-cases/{id}` with a valid `releaseId` -> 200, updates it;
  a subsequent `PUT` with no `releaseId` field at all -> 200, the
  previously-set `releaseId` is still there (untouched); `POST`/`PUT`
  with a `releaseId` from a different project (same org) -> 404; `POST`
  with a random non-existent `releaseId` -> 404.
- [x] **Step 5 - Minimal protected frontend: release catalog
  create/list** - `/projects/[projectId]/releases` page, same
  redirect-if-unauthenticated pattern as `/projects/[projectId]/tags`,
  form (name only) POSTs to the backend, shows the result and existing
  releases with their status. *Done when:* browser-driven -
  unauthenticated visit redirects to sign-in; signed in, creating a
  release shows it (with its default `PLANNED` status) in the re-fetched
  list.

## Files / areas

- `backend/.../release/entity/Release.java`
- `backend/.../release/repository/ReleaseRepository.java`
- `backend/.../release/dto/CreateReleaseRequest.java`, `ReleaseDto.java`
- `backend/.../release/mapper/ReleaseMapper.java`
- `backend/.../release/service/ReleaseService.java`
- `backend/.../release/controller/ReleaseController.java`
- `backend/src/main/resources/db/migration/V20__releases.sql`, `V21__requirements_release_id.sql`, `V22__test_cases_release_id.sql`
- `backend/.../requirement/entity/Requirement.java`, `dto/RequirementDto.java`, `dto/CreateRequirementRequest.java`, `service/RequirementService.java` - add `releaseId`
- `backend/.../testcase/entity/TestCase.java`, `dto/TestCaseDto.java`, `dto/CreateTestCaseRequest.java`, `dto/UpdateTestCaseRequest.java`, `service/TestCaseService.java` - add `releaseId`
- `frontend/services/releases.ts`, `frontend/actions/releases.ts`, `frontend/app/projects/[projectId]/releases/`

## Data / contracts

**Load-bearing:**
- `ReleaseDto { id, projectId, name, version, status, startDate, releaseDate, createdAt }` -
  8b's `Build.releaseId` references `Release.id` directly, as does this
  feature's own `Requirement.releaseId`/`TestCase.releaseId`. Don't
  change this shape once 8b is built against it.
- `RequirementDto`/`TestCaseDto` both gain a `releaseId` field - this is
  additive (existing consumers ignore new fields), but any future
  feature reading these DTOs can now rely on `releaseId` being present
  (possibly `null`).

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 1-4, including the two
same-project retrofit validation attempts) and browser evidence (Step 5),
matching every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId` directly.
- `ReleaseRepository` needs a `findByIdAndProjectId` method for the two
  retrofit steps' same-project validation - same shape as
  `TestFolderRepository`'s, `TagRepository`'s, `TestSuiteRepository`'s.
- Steps 3 and 4 are mechanical: they follow the *exact* same-project
  validation pattern `folderId` already uses in both `RequirementService`
  (well - `Requirement` doesn't have `folderId`, so use `TestCase`'s
  `folderId` check as the template) and `TestCaseService`. Don't
  redesign the validation approach, copy it.
- Step 4's `UpdateTestCaseRequest` change must preserve the "null means
  don't touch" convention 5c established - `releaseId` is exactly one
  more field in that same partial-update object, not a special case.
- No date parsing surprises: `startDate`/`releaseDate` are plain
  `LocalDate` (`yyyy-MM-dd`), Jackson handles this natively via the
  `JavaTimeModule` already confirmed present (5c's build notes) - no
  custom (de)serializer needed.
- No frontend wiring for `releaseId` on the Requirements/Test Cases
  pages - out of scope, don't add it "while you're in there."

## Build notes (from implementation)

This feature built cleanly against the spec with no code surprises -
every pattern (Jackson `LocalDate` handling, same-project cross-entity
validation, `Boolean`/optional-field precedents from 4/5b/5c) transferred
directly. The main value of this feature was fulfilling two prior
commitments correctly, which was proven, not just implemented:

- Both retrofit steps' same-project validation was tested on the exact
  scenario each promise implied: a release created in a same-org sibling
  project (`WEBAPP`) was rejected with 404 when used as `releaseId` on a
  `Requirement` create (Step 3) and separately on both a `TestCase`
  create *and* a `TestCase` update (Step 4) - three independent proofs of
  the same invariant, not one proof assumed to cover all three write
  paths.
- The partial-update interaction was specifically exercised: set
  `releaseId` via `PUT`, then issue a second `PUT` with no `releaseId`
  field at all, and confirm the previously-set value survived untouched -
  this is exactly the kind of interaction between an old convention (5c's
  "null means don't touch") and a new field that's easy to silently
  break by treating the new field as a special case; it wasn't.
- All fixture rows (sibling-project releases) were deleted after each
  proof.
