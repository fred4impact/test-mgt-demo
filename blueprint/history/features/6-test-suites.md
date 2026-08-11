# Feature: Test suites

**From build-plan:** feature 6
**Status:** complete

## Goal

Let a project group test cases into suites - a nested structure
(`TestSuite`) plus an ordered membership join (`TestSuiteCase`). This maps
to two entities, but every pattern needed already exists in this codebase:
`TestSuite` is structurally identical to `TestFolder` (5a, nested via
self-referencing `parentId`), and `TestSuiteCase` combines `TestCaseTag`'s
join-with-same-project-validation shape (5d) with `TestStep`'s
server-assigned sequencing (5b). Nothing here is a new pattern - reuse,
not design.

## In scope

- `TestSuite` JPA entity + Flyway migration (`V18__test_suites.sql`):
  `projectId`, `name`, `parentId` (nullable, self-referencing FK to
  `test_suites.id`) - identical shape to `TestFolder`
- `TestSuiteCase` JPA entity + Flyway migration
  (`V19__test_suite_cases.sql`): `suiteId`, `testCaseId`, `sortOrder`
  (server-assigned, 1-based, from submission/insertion order, same
  technique as `TestStep.stepNumber`), unique on `(suite_id, test_case_id)`
- `TestSuiteRepository`, `TestSuiteCaseRepository`
- A dedicated `testsuite` module (its own controller) for the catalog -
  mirrors `testfolder`'s pattern (5a)
- REST endpoints, nested under `/api/v1/projects/{projectId}/test-suites`:
  create, get by id, list - protected, project must belong to caller's
  org (existing pattern); creating a nested suite validates `parentId`,
  when given, belongs to the *same project* (reusing 5a's exact
  `TestFolderRepository.findByIdAndProjectId`-style check, applied to
  `TestSuiteRepository`)
- REST endpoints, nested under the existing suite controller
  (`/api/v1/projects/{projectId}/test-suites/{suiteId}/cases`): add a
  test case to a suite, remove, list a suite's test cases (ordered by
  `sortOrder`) - protected; adding validates the test case belongs to the
  *same project* as the suite (same-project cross-entity check, fifth
  instance of this pattern after 5a/5b/5c/5d)
- Minimal protected frontend page at `/projects/[projectId]/test-suites`:
  create form (name only) + list - mirrors `/projects/[projectId]/tags`'s
  pattern from 5d (catalog-only frontend, join stays API-only)

## Out of scope

- Adding/removing a suite's test cases from the frontend. Same reasoning
  as every join-table UI decision since 5c: the test case list still
  renders plain text, not links, so there's nowhere to reach a *specific*
  test case's suite membership from yet
- Suite update/delete/move (re-parenting), and reordering test cases
  within a suite after they're added. Same no-update precedent as every
  catalog entity so far (`Team`, `Role`, `Requirement`, `TestFolder`,
  `Tag`) - and the same structural consequence 5a already established:
  since a suite can only ever be created pointing at an *already-existing*
  parent and there's no move endpoint, cycles in the suite hierarchy are
  structurally impossible without extra guarding
- Suite depth limits. Nothing in the plan specifies one, same as 5a
- Nested/recursive listing (a suite's test cases *and* its child suites'
  test cases together). `GET .../cases` returns only that one suite's
  direct membership; rolling up a subtree is a reporting concern, not
  something this feature builds
- Filtering or searching suites or their contents. Feature 7 (Search &
  filters)'s job, same deferral every listing endpoint since `Requirement`
  has made

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestSuite + TestSuiteCase entities, migrations,
  repositories** - `TestSuite` (`projectId`, `name`, `parentId`
  nullable), `TestSuiteCase` (`suiteId`, `testCaseId`, `sortOrder`),
  unique `(suite_id, test_case_id)`. `V18__test_suites.sql`,
  `V19__test_suite_cases.sql`. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 19 rows (V1-V19).
- [x] **Step 2 - Protected, project-scoped TestSuite endpoints with
  parent-suite validation** - `POST /api/v1/projects/{projectId}/test-suites`
  (`name` required via Bean Validation, `parentId` optional), `GET
  /api/v1/projects/{projectId}/test-suites/{id}`, `GET
  /api/v1/projects/{projectId}/test-suites`. The project must belong to
  the caller's org (404 if not). When `parentId` is given, it must
  resolve to a `TestSuite` in the *same* project (404 if it belongs to a
  different project, or doesn't exist at all). *Done when:*
  `flyway_schema_history` unchanged at 19 rows (Step 1 already added both
  migrations); create a root suite -> 201; create a nested suite with
  `parentId` set to the first -> 201; `GET` list includes both, each with
  its correct `parentId`; `GET` by id returns one of them; `POST` missing
  `name` -> 400; `POST` with a `parentId` from a suite inserted directly
  under a *different* project (same org) -> 404; `POST` with a random
  non-existent `parentId` -> 404; `POST` to a project inserted under a
  different org -> 404; no token -> 401.
- [x] **Step 3 - TestSuiteCase add/remove/list endpoints** - `POST
  /api/v1/projects/{projectId}/test-suites/{suiteId}/cases`
  (`testCaseId` required via Bean Validation), `DELETE
  /api/v1/projects/{projectId}/test-suites/{suiteId}/cases/{testCaseId}`,
  `GET /api/v1/projects/{projectId}/test-suites/{suiteId}/cases`
  (ordered by `sortOrder`). The suite must belong to the caller's
  org/project (404 if not). Adding validates the test case belongs to
  the *same project* as the suite (404 if it belongs to a different
  project, or doesn't exist at all). `sortOrder` is computed server-side
  as the count of existing entries in that suite plus one - never
  client-supplied. Adding an already-present test case -> 409. Removing
  an absent one -> 404. *Done when:* `flyway_schema_history` unchanged at
  19 rows; add a test case to a suite -> 201, `sortOrder: 1`; add a
  second test case -> 201, `sortOrder: 2`; `GET .../cases` returns both
  in `sortOrder` order; add the first test case again -> 409; add a test
  case inserted directly under a *different* project (same org) -> 404;
  add a random UUID as `testCaseId` -> 404; remove an absent test case ->
  404; remove the first test case -> 204, no longer in the list; `POST`
  missing `testCaseId` -> 400; no token -> 401.
- [x] **Step 4 - Minimal protected frontend: test suite catalog
  create/list** - `/projects/[projectId]/test-suites` page, same
  redirect-if-unauthenticated pattern as `/projects/[projectId]/tags`,
  form (name only) POSTs to the backend, shows the result and existing
  suites. *Done when:* browser-driven - unauthenticated visit redirects
  to sign-in; signed in, creating a suite shows it in the re-fetched
  list.

## Files / areas

- `backend/.../testsuite/entity/TestSuite.java`, `TestSuiteCase.java`
- `backend/.../testsuite/repository/TestSuiteRepository.java`, `TestSuiteCaseRepository.java`
- `backend/.../testsuite/dto/CreateTestSuiteRequest.java`, `TestSuiteDto.java`, `AddTestSuiteCaseRequest.java`, `TestSuiteCaseDto.java`
- `backend/.../testsuite/mapper/TestSuiteMapper.java`
- `backend/.../testsuite/service/TestSuiteService.java`, `TestSuiteCaseService.java`
- `backend/.../testsuite/controller/TestSuiteController.java`
- `backend/src/main/resources/db/migration/V18__test_suites.sql`, `V19__test_suite_cases.sql`
- `frontend/services/testsuites.ts`, `frontend/actions/testsuites.ts`, `frontend/app/projects/[projectId]/test-suites/`

## Data / contracts

**Load-bearing:**
- `TestSuiteDto { id, projectId, parentId, name, createdAt }` - same
  shape discipline as `TestFolderDto`.
- `TestSuiteCaseDto { testCaseId, key, title, sortOrder, addedAt }` -
  denormalized with the test case's `key`/`title` (not a bare id), same
  precedent as `TeamMemberDto` and `TestCaseTag`'s `TagDto` response -
  a "list a suite's cases" response is immediately useful without a
  second lookup.
- No confirmed downstream consumer yet (unlike `Requirement`/`TestCase`,
  which feed Traceability explicitly in the data model) - `TestSuite`/
  `TestSuiteCase` are not referenced by any later entity in
  `project-overview.md`'s data model. Shapes are still locked for
  consistency, not because a specific future feature is known to depend
  on them.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 1-3, including a same-org-
different-project parent-suite attempt and a same-org-different-project
test-case-add attempt) and browser evidence (Step 4), matching every
prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId`, and
  `TestCaseRepository.findByIdAndProjectId` directly.
- `TestSuiteRepository` needs a `findByIdAndProjectId` method (for both
  the parent-suite check in Step 2 and the suite-ownership check in Step
  3) - copy `TestFolderRepository`'s exact shape, don't reinvent it.
- Step 2 is a near-verbatim copy of 5a's `TestFolder` build - same entity
  shape, same validation, same done-whens with s/folder/suite/. Don't
  over-think it; reuse the pattern directly.
- Step 3's `sortOrder` assignment is a near-verbatim copy of 5b's
  `TestStep.stepNumber` assignment (`count of existing rows in the
  parent + 1`, computed in the service, never trusted from the client).
- `ConflictException` for the already-present case (Step 3), same as
  every other join-table duplicate-attach case in this build.
- No frontend for `TestSuiteCase` this feature - resist adding one
  "while you're in there," same reasoning as 5c/5d.

## Build notes (from implementation)

This feature built cleanly against the spec, exactly as predicted by the
Goal section: every pattern was reused verbatim from 5a (TestFolder
schema/validation), 5b (server-assigned sequencing), and 5d
(join-with-same-project-validation, catalog-only frontend). No new design
decisions surfaced during the build.

- One process correction, not a code bug: the mapper's first draft
  referenced `TestSuiteCaseDto` before that type existed (a Step 3
  concern accidentally written during Step 2). Caught via IDE
  diagnostics before running anything and removed, keeping Step 2's diff
  correctly scoped to `TestSuite` only.
- All three isolation proofs (same-org sibling-project parent-suite
  rejection, same-org sibling-project test-case rejection on add, and
  cross-org project rejection) were run with real inserted fixtures, not
  just asserted from the code, matching every prior feature's discipline.
  Creating the cross-project test-case fixture required first creating a
  folder and a test case in the sibling project (WEBAPP), reusing 5a's
  and 5b's endpoints directly rather than inserting rows by hand.
- All fixture rows (sibling-project suites, test cases, folders,
  cross-org organizations/users/projects) were deleted after each proof.
