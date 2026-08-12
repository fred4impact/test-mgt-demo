# Feature: Step-level execution

**From build-plan:** feature 11b
**Status:** complete

## Goal

Let a tester mark each individual test step's result (not just the overall
execution status from 11a) as they work through a case: pass/fail/blocked
per step, with its own actual result and comment. This is the second slice
of feature 11 (Test execution), building directly on 11a's `TestExecution`.

## In scope

- `ExecutionStep` JPA entity + Flyway migration (`V29__execution_steps.sql`):
  `executionId` (`NOT NULL REFERENCES test_executions(id)`), `testStepId`
  (`NOT NULL REFERENCES test_steps(id) ON DELETE CASCADE` - see Notes for
  why cascade), `stepNumber` (denormalized copy of `TestStep.stepNumber` at
  creation time, so steps can be ordered without a join - mirrors
  `TestCycleCase.sortOrder`'s precedent), `status` (reuses the existing
  `TestExecutionStatus` enum from 11a - same seven-value closed set, no
  second enum), default `NOT_RUN`, `actualResult`/`comment` (both nullable
  text). `UNIQUE (execution_id, test_step_id)` backs one-row-per-step at
  the database level.
- `ExecutionStepRepository`: `findByExecutionIdOrderByStepNumberAsc`,
  `findByExecutionIdAndTestStepId`.
- `TestStepRepository` gains `findByIdAndTestCaseId` (small addition,
  needed to confirm a `testStepId` belongs to the case before updating its
  execution step - same shape as every other `findByIdAnd<Parent>Id`
  scoping check in this app).
- REST endpoints, nested under the existing execution route from 11a -
  `/api/v1/projects/{projectId}/test-cycles/{cycleId}/executions/{testCaseId}/steps`,
  both auto-creating rows if missing (same get-or-create pattern as 11a):
  `GET` (list - one row per the test case's current `TestStep`s, in step
  order; ensures the parent `TestExecution` exists first via 11a's logic,
  then ensures one `ExecutionStep` per `TestStep`), `PUT /{testStepId}`
  (update `status` required, `actualResult`/`comment` optional - same
  partial-update convention as 11a: only overwrite a field when the
  request includes it).
- The execution page (`/test-cycles/[cycleId]/executions/[testCaseId]`,
  built in 11a) gains a "Steps" section below the existing overall-status
  form: a table with one row per step showing the step's own content
  (action, test data, expected result - read-only, from the test case)
  plus a per-row status `<select>` and actual-result/comment inputs that
  `PUT` that row's `ExecutionStep` independently, using the same per-row
  `.bind()` server-action pattern as 10b/11a.

## Out of scope

- Auto-computing the overall `TestExecution.status` from its steps'
  results (e.g. all steps `PASSED` -> execution `PASSED`). Real value, but
  a separate rollup rule that deserves its own review, not an implicit
  side effect bolted onto step updates here. The tester still sets the
  overall status directly (11a's form stays as-is). Flagged as a good
  small follow-up once 11b lands.
- Snapshotting step content at execution time. `ExecutionStep` references
  the live `TestStep` by id, not a frozen copy. If a test case's steps are
  edited later (`TestCaseService.update` deletes and recreates all
  `TestStep` rows when `steps` is provided - confirmed in
  `TestCaseService.java:161`), the old `TestStep` rows disappear and
  `ON DELETE CASCADE` takes any `ExecutionStep` history for those old
  step ids with them. This is a deliberate simplification (full snapshot
  versioning already exists for test case *content* via
  `TestCaseVersion`, not execution history) - accepted so editing a test
  case with recorded step executions doesn't throw a 500 from a dangling
  FK. Not fixed here.
- Deleting or reordering execution steps independently. Order always
  mirrors the test case's current step order; no delete endpoint, matching
  every other entity's no-delete precedent in this app.
- Attachments per step. Still 11c's job; the polymorphic `Attachment`
  model from the data plan can point at an `ExecutionStep` id without any
  change needed here, but 11c isn't built yet.
- Bulk step updates (marking many steps at once in one request). One
  step, one `PUT`, matching 11a's own granularity cut.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - ExecutionStep entity, migration, repository** -
  `ExecutionStep` entity (reusing `TestExecutionStatus`), `TestStepRepository.findByIdAndTestCaseId`,
  `ExecutionStepRepository`, `V29__execution_steps.sql` with
  `UNIQUE (execution_id, test_step_id)` and `ON DELETE CASCADE` on the
  `test_step_id` FK. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 29 rows (V1-V29).
- [x] **Step 2 - Protected, auto-creating step endpoints** - `GET
  .../executions/{testCaseId}/steps` (list, auto-creates any missing
  `ExecutionStep`s for the case's current `TestStep`s first, in step
  order), `PUT .../executions/{testCaseId}/steps/{testStepId}` (update
  `status`/`actualResult`/`comment`, auto-creates first if missing).
  *Done when:* `flyway_schema_history` unchanged at 29 rows; `GET` list on
  a fresh execution for a test case with 2 `TestStep`s -> 200, two rows in
  step-number order, both `status: "NOT_RUN"`, both now persisted (a
  second `GET` returns the same two `id`s); `GET` list for a test case
  with zero `TestStep`s -> 200, empty array, not an error; `PUT` with
  `status: "FAILED"` on one step -> 200, that step's
  status/actualResult/comment updated, the *other* step's row unchanged;
  `PUT` to a `testStepId` that belongs to a different test case -> 404;
  `GET`/`PUT` with a `testCaseId` not in this cycle -> 404 (reuses 11a's
  `TestCycleCase` check); `PUT` with an invalid status string -> 400
  (reuses 11a's `HttpMessageNotReadableException` handler); `GET`/`PUT` on
  a `cycleId` from a different project (same org) -> 404 (reuses 11a's
  chain); no token -> 401.
- [x] **Step 3 - Steps section on the execution page** - the execution
  page gains a "Steps" table: step number, action, test data, expected
  result (read-only, from the test case's own steps - already fetched via
  `getTestCase` since 11a), each row with its own status select +
  actual-result/comment inputs and a Save button, `PUT`ing that row only.
  *Done when:* browser-driven - visiting a fresh execution shows all its
  steps as `NOT_RUN`; updating one step's status and result, then
  reloading, shows that step's new value while the other step(s) are
  unchanged.

## Files / areas

- `backend/.../testexecution/entity/ExecutionStep.java`
- `backend/.../testexecution/repository/ExecutionStepRepository.java`
- `backend/.../testcase/repository/TestStepRepository.java` (adds
  `findByIdAndTestCaseId`)
- `backend/.../testexecution/dto/ExecutionStepDto.java`,
  `UpdateExecutionStepRequest.java`
- `backend/.../testexecution/mapper/ExecutionStepMapper.java`
- `backend/.../testexecution/service/TestExecutionService.java` (gains
  `listSteps`/`updateStep` methods plus a private `ensureStepsExist`
  helper - stays in this service rather than a new one, since it shares
  11a's get-or-create chain and is a nested resource of the same
  aggregate)
- `backend/.../testexecution/controller/TestExecutionController.java`
  (adds the nested `/steps` endpoints on the same controller, same
  `@RequestMapping` base)
- `backend/src/main/resources/db/migration/V29__execution_steps.sql`
- `frontend/services/testexecutions.ts` (adds `ExecutionStep` type,
  `listExecutionSteps`, `updateExecutionStep`)
- `frontend/actions/testexecutions.ts` (adds `updateExecutionStepAction`)
- `frontend/app/projects/[projectId]/test-cycles/[cycleId]/executions/[testCaseId]/page.tsx`
  (adds the Steps section)

## Data / contracts

**Load-bearing:**
- `ExecutionStepDto { id, executionId, testStepId, stepNumber, status,
  actualResult, comment, createdAt, updatedAt }` - `status` reuses 11a's
  `TestExecutionStatus` seven-value set, not a new enum. Deliberately
  omits step content (action/test data/expected result): the frontend
  already has that from `getTestCase`'s `steps` array (11a) and matches it
  by `testStepId`, so this DTO isn't a place to duplicate it.
- `Attachment` (11c) can target `entityType: "EXECUTION_STEP"`,
  `entityId: <ExecutionStep.id>` without any change to this shape.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verify with `curl` output (Step 2, including the idempotent-creation proof
and the other-step-unchanged proof) and browser evidence (Step 3), matching
11a.

## Notes for the AI

- Reuse 11a's `TestExecutionStatus` enum for `ExecutionStep.status`. Do
  not create a second status enum - the whole point of making it a closed
  set in 11a was so 11b could rely on it.
- Reuse 11a's existing get-or-create chain (project -> cycle -> cycleCase
  -> execution) as the first half of both new endpoints; the step logic
  only adds one more level (execution -> steps) on top.
- `TestStepRepository.findByTestCaseIdOrderByStepNumberAsc` already exists
  (from feature 5b) - use it to drive auto-creation in step order, don't
  re-sort in the service layer.
- Keep `ExecutionStep.stepNumber` as a denormalized copy, not a live join -
  matches the `TestCycleCase.sortOrder` precedent and keeps the list query
  a single-table read.
- Watch the method-size rule in `coding-standards.md` (functions under ~50
  lines) when adding step logic to `TestExecutionService` - keep
  `ensureStepsExist` a separate small private method rather than inlining
  it into `listSteps`/`updateStep`.
