# Feature: Test cases & steps

**From build-plan:** feature 5b
**Status:** complete

## Goal

Let a project author test cases inside its folders, each made up of
ordered steps - the core artifact the whole platform exists to execute
(per `project-overview.md`, "Test execution (6) is the headline feature").
This feature builds create-and-read only; editing and version history are
5c's job, tags are 5d's.

## In scope

- `TestCase` JPA entity + Flyway migration (`V13__test_cases.sql`):
  `projectId`, `folderId`, `key`, `title`, `priority` (nullable),
  `severity` (nullable), `status` (default `'ACTIVE'`), `testType`
  (nullable), `automationStatus` (nullable), `ownerId`, unique on
  `(project_id, key)`
- `TestStep` JPA entity + Flyway migration (`V14__test_steps.sql`):
  `testCaseId`, `stepNumber` (server-assigned, 1-based, from submission
  order), `action`, `testData` (nullable), `expectedResult` (nullable)
- `TestCaseRepository`, `TestStepRepository`
- A dedicated `testcase` module (its own controller)
- REST endpoints, nested under `/api/v1/projects/{projectId}/test-cases`:
  create (with an optional nested list of steps), get by id (returns
  steps), list - protected; the project must belong to the caller's org
  (existing pattern); `folderId` must resolve to a `TestFolder` in the
  *same* project (reusing 5a's `TestFolderRepository.findByIdAndProjectId`
  check and its exact "wrong project or missing both 404" philosophy)
- Server-generated sequential `key` (`{projectKey}-1`, `{projectKey}-2`,
  ...), same pattern as `Requirement` (4) - computed from a count of
  existing test cases in the project, a **separate sequence from
  `Requirement`'s** (see Data/contracts - this is a deliberate, documented
  consequence of the established per-entity-type pattern, not a defect)
- `ownerId` defaults to the creating user, same precedent as `Project` and
  `Requirement`
- Minimal protected frontend page at `/projects/[projectId]/test-cases`:
  a folder picker (with a bare inline "create a folder" fallback when
  none exist yet, since 5a shipped no frontend of its own), a create form
  (folder, title, and dynamic step rows: action/testData/expectedResult),
  and a list of existing test cases with their keys

## Out of scope

- `releaseId`. Same deferral as `Requirement` (4) - `Release` doesn't
  exist until build-plan item 8. Feature 8 adds it via its own migration
  to both tables when it lands
- Test case or test step update/delete, and any reordering of steps after
  creation. This is explicitly 5c's job (`TestCaseVersion` snapshotting
  needs an update path to snapshot *around*, which doesn't exist yet)
- Tags (`Tag`/`TestCaseTag`). Explicitly 5d
- Enforcing fixed values for `priority`, `severity`, `status`, `testType`,
  or `automationStatus`. All plain nullable strings, client-supplied where
  applicable, no invented enums - same precedent as `Requirement`'s
  `priority`/`status`
- Filtering or searching test cases (by folder, status, anything). `GET
  .../test-cases` returns every test case in the project, unfiltered -
  build-plan item 7 (Search & filters) owns this explicitly
- Full folder management on the frontend. The inline folder-creation
  fallback is bare (name only, no parent/nesting) just so the test case
  page isn't a dead end on a fresh project; real folder browsing and
  nested creation stay API-only, per 5a's own precedent

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestCase + TestStep entities, migrations, repositories** -
  `TestCase` (`projectId`, `folderId`, `key`, `title`, `priority`,
  `severity`, `status` default `'ACTIVE'`, `testType`, `automationStatus`,
  `ownerId`), `TestStep` (`testCaseId`, `stepNumber`, `action`,
  `testData`, `expectedResult`). `V13__test_cases.sql`,
  `V14__test_steps.sql`. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 14 rows (V1-V14).
- [x] **Step 2 - Protected, project-scoped REST endpoints with
  folder validation and nested step creation** - `POST
  /api/v1/projects/{projectId}/test-cases` (`folderId` and `title`
  required via Bean Validation, `priority`/`severity`/`testType`/
  `automationStatus` optional, `steps` an optional list of
  `{action (required), testData, expectedResult}`), `GET
  /api/v1/projects/{projectId}/test-cases/{id}` (includes `steps` in
  submission order), `GET /api/v1/projects/{projectId}/test-cases`
  (includes `steps` too - one consistent DTO everywhere, no separate
  summary shape). *Done when:* `flyway_schema_history` is unchanged at
  14 rows (Step 1 already added both migrations); create with only
  `folderId`+`title` -> 201, key `{projectKey}-1`, `status: "ACTIVE"`,
  `steps: []`; create with 2 steps -> 201, steps have `stepNumber` 1 and
  2 in submission order with the right `action`/`testData`/
  `expectedResult`; a second test case in the same project -> key
  `{projectKey}-2` (a **separate sequence from `Requirement`'s** - if a
  requirement `{projectKey}-1` already exists in the same project, the
  first test case is still `{projectKey}-1`, not `-2`, since it's counted
  from the `test_cases` table alone); `GET` by id returns the steps;
  `GET` list includes both test cases; `POST` missing `title` -> 400;
  `POST` missing `folderId` -> 400; `POST` with a step missing `action`
  -> 400; `POST` with a `folderId` from a folder inserted directly under
  a *different* project (same org) -> 404; `POST` to a project inserted
  under a different org -> 404; no token -> 401.
- [x] **Step 3 - Minimal protected frontend: folder picker + create/list
  test cases** - `/projects/[projectId]/test-cases` page, same
  redirect-if-unauthenticated pattern as `/projects/[projectId]/requirements`;
  fetches folders via the (until now unused) `GET .../test-folders` from
  5a - if none exist, shows a bare "create a folder" mini-form (name
  only) instead of the test case form; once a folder exists, shows the
  test case form (folder select, title, add/remove step rows) and the
  list of existing test cases with their keys and step counts. *Done
  when:* browser-driven - unauthenticated visit redirects to sign-in; on
  a project with no folders, the inline folder-creation form appears and
  creating one reveals the test case form; selecting the folder, adding
  two steps, and submitting shows the new test case with its
  auto-generated key and step count in the re-fetched list.

## Files / areas

- `backend/.../testcase/entity/TestCase.java`, `TestStep.java`
- `backend/.../testcase/repository/TestCaseRepository.java`, `TestStepRepository.java`
- `backend/.../testcase/dto/CreateTestCaseRequest.java`, `CreateTestStepRequest.java`, `TestCaseDto.java`, `TestStepDto.java`
- `backend/.../testcase/mapper/TestCaseMapper.java`
- `backend/.../testcase/service/TestCaseService.java`
- `backend/.../testcase/controller/TestCaseController.java`
- `backend/src/main/resources/db/migration/V13__test_cases.sql`, `V14__test_steps.sql`
- `frontend/services/testfolders.ts`, `frontend/actions/testfolders.ts` - new, 5a shipped no frontend
- `frontend/services/testcases.ts`, `frontend/actions/testcases.ts`, `frontend/app/projects/[projectId]/test-cases/`

## Data / contracts

**Load-bearing:**
- `TestCaseDto { id, projectId, folderId, key, title, priority, severity, status, testType, automationStatus, ownerId, createdAt, steps: TestStepDto[] }`,
  `TestStepDto { id, stepNumber, action, testData, expectedResult }` -
  feature 5c updates against this shape (and needs the pre-update state to
  snapshot into `TestCaseVersion`); feature 6 (Test suites) and 13
  (Traceability) reference `TestCase.id`. Don't change this shape once 5c
  is built against it.
- **Per-entity-type key sequencing, not a shared project-wide counter.**
  `TestCase.key` and `Requirement.key` are computed independently (each
  counts only its own table), so `{projectKey}-1` can legitimately exist
  as both a `Requirement` and a `TestCase` in the same project
  simultaneously. This was flagged as an open question in 4's spec
  ("expected to follow the identical pattern") and is now resolved
  explicitly here: identical *computation*, independent *sequences*, not
  deduplicated across entity types. `Defect` (12) should follow the same
  resolved convention.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 1-2, including a folder from a
sibling project) and browser evidence (Step 3), matching every prior
feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId`, and 5a's
  `TestFolderRepository.findByIdAndProjectId` directly - the folder
  validation here is the exact same check 5a itself used for
  `parentId`, just applied to `TestCase.folderId` instead.
- Compute `key` and each step's `stepNumber` inside the service method,
  never trust a client-supplied value for either.
- Steps are created in the same transaction as their parent `TestCase` -
  don't split step creation into a separate endpoint in this sub-feature;
  that only becomes necessary once 5c adds editing.
- The frontend's inline folder-creation fallback calls the *existing*
  5a backend endpoint (`POST .../test-folders`) - no backend change
  needed for it, only new frontend `services`/`actions` files, since 5a
  deliberately shipped none.

## Build notes (from implementation)

- **Real bug found and fixed in Step 2**: the first draft computed each
  `TestStep.stepNumber` via `stepRequests.indexOf(stepRequest)`. Since
  `CreateTestStepRequest` is a record (value equality), two steps with
  identical `action`/`testData`/`expectedResult` would both resolve to
  the same index, producing duplicate `stepNumber`s and tripping the
  unique constraint on `(test_case_id, step_number)`. Caught by reasoning
  about the code before running it, fixed with a plain indexed loop.
- **A test-script false negative, not an app bug**: the first Step 3
  browser verification reported the test case creation as failed
  (`HAS_TITLE`/`HAS_KEY`/`HAS_STEP_COUNT` all false). Root cause was the
  verification script's own `waitForLoadState("networkidle")` firing and
  screenshotting before the server action's response landed, not a
  problem in the app. A debug run with request/response logging showed
  the `POST` actually succeeded and both test cases existed correctly.
  Re-ran with `waitForSelector` on the expected result text instead of a
  fixed wait, which passed cleanly. Test fixtures from both runs were
  deleted before the final clean pass.
- **Unrelated pre-existing gap surfaced mid-session, left out of scope**:
  the frontend's Keycloak access token is captured once at sign-in
  (`frontend/auth.ts`'s `jwt` callback) with no refresh-token rotation.
  A long-running dev session outlives the token's short lifespan, and
  every backend-calling page then fails identically (`listProjects` and
  `listRoles` both failed the same way, confirming it's session-wide, not
  feature-specific). Diagnosed and explained to the user; not fixed here
  since it's a cross-cutting concern well outside 5b's scope. Worth a
  dedicated `/fix` later.
