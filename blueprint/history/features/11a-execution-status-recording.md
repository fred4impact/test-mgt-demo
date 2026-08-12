# Feature: Execution status recording

**From build-plan:** feature 11a
**Status:** complete

## Goal

Let a tester record the overall result (pass/fail/blocked/skipped/etc.) of
a test case within a test cycle. This is the first slice of feature 11
(Test execution) - the platform's headline feature - and the first entity
in this app that genuinely needs a real update endpoint: a tester starts a
case, works through it, and revises its status as they go. Every prior
entity's no-update precedent was a deliberate simplification; this one is a
deliberate, necessary exception.

## In scope

- `TestExecution` JPA entity + Flyway migration (`V28__test_executions.sql`):
  `projectId`, `cycleId` (`NOT NULL REFERENCES test_cycles(id)`),
  `testCaseId` (`NOT NULL REFERENCES test_cases(id)`), `assigneeId`
  (nullable - inherited from the matching `TestCycleCase.assigneeId` at
  creation, not independently settable here), `environmentId`/`buildId`
  (both `NOT NULL` - inherited from the parent `TestCycle`, which already
  pins both; not independently selectable per execution), `status` (a real
  Java enum this time, not a freeform string - the data model explicitly
  names a fixed set: `NOT_RUN | IN_PROGRESS | PASSED | FAILED | BLOCKED |
  SKIPPED | NOT_APPLICABLE`, default `NOT_RUN` - the first status field in
  this app validated against a closed set instead of accepted as any
  string), `startedAt`/`completedAt`/`durationMs` (all nullable,
  auto-managed - see below), `actualResult`/`comment` (both nullable text).
  A `UNIQUE (cycle_id, test_case_id)` constraint backs the one-execution-
  per-case-per-cycle invariant at the database level, not just in code.
- `TestExecutionRepository`: `findByCycleIdAndTestCaseId`,
  `findByCycleId` (ordered to match the cycle's case list)
- A dedicated `testexecution` module (its own controller)
- REST endpoints, nested under
  `/api/v1/projects/{projectId}/test-cycles/{cycleId}/executions`, all
  three **auto-creating the row if it doesn't exist yet** (an idempotent
  get-or-create, not a separate `POST`): `GET` (list - one row per case
  currently in the cycle, `TestCycleCase` is the source of truth for which
  cases exist), `GET /{testCaseId}` (get one), `PUT /{testCaseId}`
  (update `status` required, `actualResult`/`comment` optional - **the
  update endpoint**). `testCaseId` must already be a `TestCycleCase` in
  this cycle (404 if not, reusing the check `TestCycleCaseRepository`
  already supports).
- Auto-managed timing on `PUT`: if `startedAt` is still null, set it to
  now; if the incoming `status` is a terminal one (`PASSED`, `FAILED`,
  `BLOCKED`, `SKIPPED`, `NOT_APPLICABLE`), set `completedAt` to now and
  compute `durationMs` from `startedAt`. Never clears `completedAt`/
  `durationMs` once set, even if status moves backward (e.g. back to
  `IN_PROGRESS` for a retest) - the prior completion stays as history
  until a new terminal status overwrites it.
- The test cycle detail page's "Selected test cases" table
  (`/projects/[projectId]/test-cycles/[cycleId]`) gains an "Execution"
  column: a status badge linking to a new nested page
  `/test-cycles/[cycleId]/executions/[testCaseId]`
- The new execution page: test case key/title, current status badge, a
  form (status `<select>` of the seven fixed values, `actualResult`
  textarea, `comment` textarea) that `PUT`s an update

## Out of scope

- `ExecutionStep` (per-step results). Explicitly 11b - this feature
  records one overall status per test case, not per step. The new
  execution page is exactly where 11b adds its steps section.
- `Attachment` (file uploads on an execution). Explicitly 11c.
- Reassigning `assigneeId` on an execution independent of the
  `TestCycleCase` it came from. Inherited once at auto-creation, no
  separate reassignment endpoint - matches 10b's own no-reassignment
  scope cut for the same reason (remove-and-re-add the `TestCycleCase` to
  change who's assigned, same limitation carried through).
- Any change to `environmentId`/`buildId` per execution. Both come from
  the parent `TestCycle` and are fixed for every execution in it - no
  override.
- Bulk status updates (marking many cases at once). One case, one `PUT`,
  matching the granularity of 10b's one-row-at-a-time interactions.
- A cycle-level pass/fail summary (the "83% COMPLETE" donut/segmented-bar
  pattern from the reference screenshots). Real value, but a separate,
  additive concern once individual executions exist to summarize - not
  needed to prove this feature, and risks scope creep into what's really
  item 14 (Reporting)'s job. Flagged as a good next small feature once
  11a-c land, not bundled here.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestExecution entity, migration, repository** -
  `TestExecution` with a real `TestExecutionStatus` Java enum (`NOT_RUN`,
  `IN_PROGRESS`, `PASSED`, `FAILED`, `BLOCKED`, `SKIPPED`,
  `NOT_APPLICABLE`), all fields per the In scope list,
  `UNIQUE (cycle_id, test_case_id)`. `V28__test_executions.sql`. *Done
  when:* app starts cleanly under `ddl-auto: validate`,
  `flyway_schema_history` has 28 rows (V1-V28).
- [x] **Step 2 - Protected, auto-creating execution endpoints** - `GET
  .../executions` (list, auto-creates any missing rows for the cycle's
  current `TestCycleCase`s first), `GET .../executions/{testCaseId}`
  (get, auto-creates if missing), `PUT .../executions/{testCaseId}`
  (update `status`/`actualResult`/`comment`, auto-creates first if
  missing, then applies the timing logic). *Done when:*
  `flyway_schema_history` unchanged at 28 rows; `GET` list on a cycle
  with 2 `TestCycleCase`s and zero prior executions -> 200, two rows,
  both `status: "NOT_RUN"`, both now persisted (a second `GET` returns
  the same two `id`s, proving idempotent creation); `GET` one for a
  `testCaseId` not in this cycle -> 404; `PUT` with `status: "PASSED"` on
  a fresh case -> 200, `startedAt` and `completedAt` both set,
  `durationMs` present and non-negative; `PUT` with `status:
  "IN_PROGRESS"` on a fresh case -> 200, `startedAt` set,
  `completedAt`/`durationMs` still null; `PUT` with an invalid status
  string (not one of the seven) -> 400; `PUT` to a `testCaseId` not in
  this cycle -> 404; a second `PUT` moving an already-`PASSED` execution
  back to `IN_PROGRESS` -> 200, `completedAt`/`durationMs` from the first
  `PUT` are unchanged (not cleared); `assigneeId` on a fresh execution
  matches the `TestCycleCase`'s `assigneeId` when one was set, null when
  it wasn't; `environmentId`/`buildId` match the parent `TestCycle`'s;
  `PUT`/`GET` to a `cycleId` from a different project (same org) -> 404;
  no token -> 401.
- [x] **Step 3 - Execution column on the cycle detail page** - the
  "Selected test cases" table gains an "Execution" column: a status
  badge (reusing `statusBadgeClasses`, extended if needed for the new
  terminal statuses) linking to
  `/test-cycles/[cycleId]/executions/[testCaseId]`. *Done when:*
  screenshot shows the new column with real statuses (auto-created as
  `NOT_RUN` for cases with no prior execution); clicking a status badge
  navigates to the execution page.
- [x] **Step 4 - Execution page: view + update status** - new
  `/projects/[projectId]/test-cycles/[cycleId]/executions/[testCaseId]`
  page: fetches (and thereby auto-creates, per Step 2) the execution,
  shows the test case key/title, current status badge, and a form
  (status select, actual result textarea, comment textarea) that submits
  via `PUT`. *Done when:* browser-driven - unauthenticated visit
  redirects to sign-in; visiting a fresh case's execution page shows
  `NOT_RUN`; submitting `PASSED` with an actual result and comment shows
  the updated status and both text fields on reload; going back to the
  cycle detail page shows the same updated status badge in the table.

## Files / areas

- `backend/.../testexecution/entity/TestExecution.java`,
  `TestExecutionStatus.java` (enum)
- `backend/.../testexecution/repository/TestExecutionRepository.java`
- `backend/.../testexecution/dto/UpdateTestExecutionRequest.java`, `TestExecutionDto.java`
- `backend/.../testexecution/mapper/TestExecutionMapper.java`
- `backend/.../testexecution/service/TestExecutionService.java`
- `backend/.../testexecution/controller/TestExecutionController.java`
- `backend/src/main/resources/db/migration/V28__test_executions.sql`
- `frontend/services/testexecutions.ts`, `frontend/actions/testexecutions.ts`
- `frontend/app/projects/[projectId]/test-cycles/[cycleId]/page.tsx`
  (adds the Execution column)
- `frontend/app/projects/[projectId]/test-cycles/[cycleId]/executions/[testCaseId]/page.tsx` (new)
- `frontend/lib/badges.ts` (extended if the seven execution statuses need
  colors beyond what `statusBadgeClasses`'s existing ACTIVE/neutral rule
  covers - see Notes)

## Data / contracts

**Load-bearing:**
- `TestExecutionDto { id, projectId, cycleId, testCaseId, assigneeId,
  environmentId, buildId, status, startedAt, completedAt, durationMs,
  actualResult, comment, createdAt, updatedAt }` - 11b's
  `ExecutionStep.executionId` references `TestExecution.id` directly.
  Don't change this shape once 11b is built.
- `status` is one of exactly seven fixed values - 11b and any later
  reporting feature can rely on this closed set, not arbitrary strings.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verify with `curl` output (Step 2, including the idempotent-creation
proof, the invalid-status 400, and the non-clearing-completedAt proof) and
browser evidence (Steps 3-4), matching every prior feature.

## Notes for the AI

- This is the first entity in the app with a real update endpoint. Don't
  reach for the "no update" precedent here - it doesn't apply. Do keep
  everything else about the pattern consistent (same-project scoping,
  `GlobalExceptionHandler`, DTO-not-entity responses).
- `statusBadgeClasses` (`frontend/lib/badges.ts`) currently only
  distinguishes `ACTIVE` (success) from everything else (neutral) - that
  rule doesn't fit seven execution statuses well. Extend it with an
  execution-aware branch (`PASSED`->success, `FAILED`->danger,
  `BLOCKED`/`SKIPPED`->warning, `NOT_RUN`/`IN_PROGRESS`/
  `NOT_APPLICABLE`->neutral) rather than forking a new helper - keep it
  one function, one import site, same as today.
- The get-or-create logic belongs in `TestExecutionService`, called by
  all three endpoints (`list`, `getById`, `update`) - one private
  `ensureExists(cycleId, testCaseId)` helper, not three copies of the
  same creation logic.
- `durationMs` computation: `Duration.between(startedAt,
  completedAt).toMillis()` once both are `Instant`s - no manual epoch
  math.
- Reuse `TestCycleCaseRepository.findByCycleIdAndTestCaseId` (already
  exists from 10b) both to confirm the `testCaseId` belongs to this
  cycle and to read its `assigneeId` for inheritance at creation time.
- Reuse `TestCycleRepository.findByIdAndProjectId` (10a) to read the
  parent cycle's `buildId`/`environmentId` for inheritance.
