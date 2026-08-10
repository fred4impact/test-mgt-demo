# Feature: Test case editing & version history

**From build-plan:** feature 5c
**Status:** complete

## Goal

Let a test case be edited (title, folder, priority, severity, status,
test type, automation status, steps), snapshotting its pre-edit state into
`TestCaseVersion` before every change lands - the first update capability
in this codebase. Every prior entity in this build has deliberately shipped
with no update endpoint; this feature is where that pattern is
intentionally broken, because version history has no meaning without an
update path to snapshot around.

## In scope

- `TestCaseVersion` JPA entity + Flyway migration
  (`V15__test_case_versions.sql`): `testCaseId`, `versionNumber`
  (server-assigned, 1-based, count of existing versions for that test
  case plus one), `snapshot` (the pre-edit `TestCase` + its steps,
  serialized to JSON text via Jackson), `changeSummary` (nullable,
  client-supplied), unique on `(test_case_id, version_number)`
- `TestCaseVersionRepository`
- `PUT /api/v1/projects/{projectId}/test-cases/{id}` - **partial update**:
  every field in the request is optional; a field present (non-null)
  overwrites the current value, a field omitted (null) leaves it
  untouched. `steps`, if present at all (even `[]`), fully replaces the
  existing steps (delete-and-reinsert, same technique `create` already
  uses, with fresh server-assigned `stepNumber`s); if omitted, existing
  steps are left alone. Before applying any of it, the current state
  (test case fields + its current steps) is snapshotted into a new
  `TestCaseVersion` row
- `GET /api/v1/projects/{projectId}/test-cases/{id}/versions` - list a
  test case's version history, newest last (by `versionNumber`)
- Same project/org-scoping and `folderId` same-project validation as
  `create` (5b) - reused, not reimplemented

## Out of scope

- Any frontend. Editing needs a way to reach a *specific* test case to
  edit it, and the current test case list (5b) renders plain text, not
  links - there is no test case detail page to hang an edit form off of
  yet. Building one now would either be a throwaway "edit by pasting a
  UUID into a form" page, or would require building real navigation
  first, which the user and I just agreed stays out of scope for now
  (build-plan order holds, navigation is a separate later concern). Same
  call 3c made for `ProjectMember`: no UI until there's a real, reachable
  reason to look at the data
- Reading a specific past version's *full* snapshot back into a live
  preview or diff view. `GET .../versions` returns the raw snapshots;
  rendering or diffing them is a reporting/UI concern, not a data concern
- Restoring/reverting to a prior version. Nothing in the build-plan line
  asks for this, and it is meaningfully more complex (would need its own
  validation, since a restored `folderId` could reference a folder that
  no longer exists) - a candidate for its own future feature if wanted
- Deleting a test case. Still no delete endpoint anywhere in this
  codebase; out of scope for the same reason it always has been
- Optimistic concurrency control (e.g. rejecting an update based on a
  stale `updatedAt`). Two concurrent edits will just each get their own
  version snapshot in whatever order they land - acceptable for an
  internal MVP tool, same spirit as 4's accepted key-collision race

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestCaseVersion entity, migration, repository** -
  `TestCaseVersion` (`testCaseId`, `versionNumber`, `snapshot` text,
  `changeSummary` nullable), unique `(test_case_id, version_number)`.
  `V15__test_case_versions.sql`. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 15 rows (V1-V15).
- [x] **Step 2 - Update endpoint with pre-edit snapshotting, plus version
  history listing** - `PUT /api/v1/projects/{projectId}/test-cases/{id}`
  (all fields optional; `steps`, if present, fully replaces the current
  steps; unrecognized/omitted fields keep their current value). Snapshot
  the pre-edit state (via the existing `TestCaseMapper`/Jackson) into a
  new `TestCaseVersion` before writing any change. `GET
  /api/v1/projects/{projectId}/test-cases/{id}/versions` lists version
  rows for a test case, ordered by `versionNumber`. *Done when:*
  `flyway_schema_history` is unchanged at 15 rows (Step 1 already added
  the migration); `PUT` with only `title` -> 200, title updated,
  `priority`/`steps`/etc. unchanged; `GET .../versions` afterward -> 1
  row, `versionNumber: 1`, snapshot contains the *pre-edit* title (not
  the new one); a second `PUT` with a new `steps` list -> 200, old steps
  gone, new steps present with fresh `stepNumber`s starting at 1; `GET
  .../versions` afterward -> 2 rows in order, the second snapshot's
  steps match what existed *before* this second edit (i.e. the steps
  from the first edit's untouched state, not the brand-new ones); `PUT`
  with a `folderId` from a folder inserted directly under a *different*
  project (same org) -> 404, no version row created (validation runs
  before snapshotting); `PUT` to a project inserted under a different
  org -> 404; `PUT` to a nonexistent test case id in an otherwise valid
  project -> 404; `GET .../versions` on a test case with zero edits ->
  200, empty list; no token -> 401 on both endpoints.

## Files / areas

- `backend/.../testcase/entity/TestCaseVersion.java`
- `backend/.../testcase/repository/TestCaseVersionRepository.java`
- `backend/.../testcase/dto/UpdateTestCaseRequest.java`, `TestCaseVersionDto.java`
- `backend/.../testcase/mapper/TestCaseMapper.java` - add a `TestCaseVersionDto` mapper method
- `backend/.../testcase/service/TestCaseService.java` - add `update` and `listVersions`
- `backend/.../testcase/controller/TestCaseController.java` - add the two new endpoints
- `backend/src/main/resources/db/migration/V15__test_case_versions.sql`

## Data / contracts

**Load-bearing:**
- `TestCaseVersionDto { id, versionNumber, snapshot, changeSummary, createdAt }` -
  `snapshot` is a raw JSON string (the pre-edit `TestCaseDto` shape,
  serialized), not a parsed object - any future feature reading version
  history parses it client-side rather than the backend re-exposing typed
  historical shapes that may not match the *current* `TestCaseDto`.
- Partial-update semantics are now the contract for `TestCase` edits
  specifically: null means "don't touch", present (including empty list
  for `steps`) means "set to this." This is intentionally different from
  every `create` endpoint in the app so far, which requires its core
  fields. Any future update endpoint on another entity should decide
  this explicitly rather than assuming one convention automatically
  applies to the other.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output, including a folder-from-a-different-
project attempt on update and confirming a snapshot's content reflects the
*pre-edit* state, not the post-edit one, matching every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId`,
  `TestCaseRepository.findByIdAndProjectId`, and 5a's
  `TestFolderRepository.findByIdAndProjectId` directly.
- Snapshot **before** any mutation happens, inside the same
  `@Transactional` method - build the snapshot DTO from the entity's
  *current* (pre-edit) state and its *current* steps (loaded before any
  step deletion), serialize with Jackson's `ObjectMapper`
  (`spring-boot-starter-web` already provides one - inject it, don't
  build a new one by hand).
- Validate `folderId` (if present in the request) *before* snapshotting
  or mutating anything - a validation failure must never create a
  version row or touch any data, matching the done-when.
- `versionNumber` is computed exactly like `TestCase.key` and
  `Requirement.key` were: count of existing rows (here, existing
  `TestCaseVersion` rows for this test case) plus one. Same pattern,
  different table.
- No frontend this sub-feature. Resist adding one "while you're in
  there" - there's nowhere in the UI to link an edit form from yet.

## Build notes (from implementation)

Two real bugs were found and fixed during this feature, both specific to
Spring Boot 4.1 / Hibernate 7 behavior this codebase hadn't hit before:

- **`@Lob String` maps to Postgres `oid`, not `text`.** The first draft of
  `TestCaseVersion.snapshot` used `@Lob` on a `String` field, which
  Hibernate mapped to the JDBC `Types#CLOB` / Postgres `oid` large-object
  type. The migration created a plain `TEXT` column, so schema validation
  failed on startup with a type mismatch. `@Lob` isn't needed for a
  JSON-text field of this size; removed it, mapped the field plainly with
  `columnDefinition = "TEXT"` to match.
- **`ObjectMapper` lives at `tools.jackson.databind`, not
  `com.fasterxml.jackson.databind`, on this stack.** Spring Boot 4.1
  ships Jackson 3, which relocated the core/databind packages (confirmed
  via the local Maven repository: `tools/jackson/core/jackson-databind`
  is the actual resolved dependency, not `com/fasterxml/jackson/core`).
  The autoconfigured `ObjectMapper` bean was still injectable and had
  `Instant` serialization working correctly once the import was
  corrected - `writeValueAsString` also throws an unchecked
  `JacksonException` in Jackson 3, so no checked-exception handling was
  needed either.
- **Hibernate flush ordering, not application logic, caused a
  duplicate-key 500** when replacing a test case's steps: queued INSERT
  actions flush before queued DELETE actions within the same Hibernate
  flush, regardless of the order `save()`/`delete()` were called in code.
  The delete-then-recreate step-replacement logic needed an explicit
  `testStepRepository.flush()` between the delete and the new inserts to
  force the old rows out before the new ones landed on the same
  `(test_case_id, step_number)` values. Caught by actually running the
  two-edit sequence end-to-end, not by code review.
- The version-ordering behavior (each snapshot capturing the state
  *right before* that specific edit, not before the very first edit) was
  proven with a real two-edit sequence and inspected field-by-field, not
  just asserted from reading the code.
