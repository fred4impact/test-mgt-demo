# Fix: Race-safe execution auto-create + deduplicate lookup chain

**Type:** Fix
**Fixes:** F-01, F-02

## The problem

`TestExecutionService`'s get-or-create pattern (`list()`, `resolveExecution()`,
`createExecution()`, `createExecutionStep()`) is a non-atomic check-then-insert:
find the row, and if missing, insert one. Two concurrent requests that both find
nothing will both attempt an INSERT; the loser hits the
`UNIQUE (cycle_id, test_case_id)` / `UNIQUE (execution_id, test_step_id)`
constraint and throws an uncaught `DataIntegrityViolationException`, which falls
through `GlobalExceptionHandler` to Spring's default error body (also breaking
the app's own consistent error-shape contract) as a raw 500.

This isn't theoretical: 11b's execution page fires `getTestExecution` and
`listExecutionSteps` via `Promise.all` on every page load - two backend calls
that both auto-create rows for the same execution the first time a fresh test
case's execution page opens. The cycle detail page's bulk `list()` races the
same way whenever two testers view the same cycle concurrently. Reproduced
empirically during `/audit`: 8 pairs of concurrent `GET`s against a fresh test
case produced 2 `HTTP 500`s (`duplicate key value violates unique constraint
"test_executions_cycle_id_test_case_id_key"` / `"execution_steps_..."` in the
backend log).

Separately, `getById()`, `update()`, and `resolveExecution()` each inline the
identical ~15-line project -> cycle -> cycleCase -> execution lookup chain.
`resolveExecution()` was written specifically so `listSteps`/`updateStep`
wouldn't need a third copy, but `getById`/`update` were never migrated to use
it, so the exact duplication it was meant to prevent still exists.

## The fix

- In `createExecution()` and `createExecutionStep()`, catch
  `DataIntegrityViolationException` around the `save()` call and re-fetch via
  `findByCycleIdAndTestCaseId` / `findByExecutionIdAndTestStepId` instead of
  letting it propagate. The unique constraint guarantees the row exists once
  the concurrent winner commits, so this makes get-or-create self-healing
  without a broader locking or transaction change.
- Have `getById()` and `update()` call the existing private `resolveExecution()`
  helper instead of inlining their own copy of the same four lookups. No
  behavior change - same lookups, same 404s, just one copy instead of three.
- Must not change any response shape, status code, or existing behavior for
  the non-racing path - this is a defensive/deduplication fix, not a feature
  change. `update()`'s status/timing logic after resolving the execution stays
  exactly as-is.

## Build steps

- [x] **Step 1 - Make get-or-create race-safe** - catch
  `DataIntegrityViolationException` in `createExecution()` and
  `createExecutionStep()`, re-fetching the row on conflict instead of
  throwing. *Done when:* re-running the same concurrent-request probe from the
  audit (8 pairs of parallel `GET`s against a fresh test case's execution and
  steps endpoints) produces zero `500`s, all `200`s, and every response for a
  given row shares the same `id`.
- [x] **Step 2 - Deduplicate the lookup chain** - `getById()` and `update()`
  call `resolveExecution()` instead of inlining the four lookups. *Done when:*
  build passes; a quick regression curl pass confirms `getById`/`update`
  still 404 correctly for a bad `cycleId`/`testCaseId` and still succeed for
  a valid one, matching 11a's original behavior.

## Files / areas

- `backend/src/main/java/com/testmgmt/platform/testexecution/service/TestExecutionService.java`

## Verify

- Backend build (`./mvnw clean package -DskipTests`).
- The concurrent-request reproduction from the audit, re-run after the fix,
  showing 0/16 failures instead of 2/16.
- A short regression curl pass on `getById`/`update` (valid case, bad cycleId,
  bad testCaseId) to confirm Step 2 didn't change behavior.

## Findings

### race-safe-execution-auto-create/F-01 [P1] closed - Concurrent auto-create requests crash with an unhandled 500

**File:** backend/src/main/java/com/testmgmt/platform/testexecution/service/TestExecutionService.java:788, :859
**Found:** 2026-08-12 by /audit (scope: current, features 11a+11b, commits e76be89..4de3828)
**Why it matters:** `list()`, `resolveExecution()` (used by `getById`/`update`/`listSteps`/`updateStep`), `createExecution()`, and `createExecutionStep()` all follow a check-then-insert get-or-create pattern with no transaction boundary, locking, or duplicate-key handling. Two concurrent requests that both find no existing row will both attempt an INSERT; the loser hits the `UNIQUE (cycle_id, test_case_id)` / `UNIQUE (execution_id, test_step_id)` constraint and throws `DataIntegrityViolationException`, which `GlobalExceptionHandler` has no handler for, so it falls through to Spring's default error body (breaking the app's documented consistent error shape too) with a raw 500.

  This is not a contrived scenario: the execution page built in 11b (`frontend/app/.../executions/[testCaseId]/page.tsx`) fires `getTestExecution` and `listExecutionSteps` via `Promise.all` on every page load - two backend calls that both auto-create rows tied to the same execution the very first time a fresh test case's execution page is opened. The cycle detail page's `listTestExecutions` (bulk `list()`) has the identical race whenever two testers view the same cycle concurrently. Reproduced empirically: 8 pairs of concurrent `GET` requests against a fresh test case's execution/steps endpoints produced 2 `HTTP 500`s, with the backend log confirming `duplicate key value violates unique constraint "test_executions_cycle_id_test_case_id_key"` and `"execution_steps_execution_id_test_step_id_key"`.

**Suggested fix:** In `createExecution()` and `createExecutionStep()`, catch `DataIntegrityViolationException` around the `save()` call and re-fetch via the existing `findByCycleIdAndTestCaseId` / `findByExecutionIdAndTestStepId` lookup instead of propagating - the unique constraint guarantees the row now exists once the concurrent insert wins. Keeps the get-or-create semantics self-healing without a broader locking change.
**Resolution:** Fixed via `fix/race-safe-execution-auto-create` Step 1 - both `createExecution()` and `createExecutionStep()` now catch `DataIntegrityViolationException` and re-fetch. Re-ran the exact reproduction from the audit (8 pairs of concurrent `GET`s against a fresh test case's execution/steps endpoints): 16/16 `200`s (down from 2/16 `500`s), all responses per row converged on the same `id`, and the backend log shows the same underlying DB-level constraint collisions now caught as `WARN` instead of surfacing as unhandled `ERROR`-level exceptions. **Closed by /audit re-examination (2026-08-12, scope: current)**: additionally verified the bulk `list()` path named in the original finding (8 concurrent `GET`s against the cycle's execution list for a fresh test case) - 8/8 `200`s, all converging on the same execution `id`, zero unhandled-exception log entries. Confirmed no @Transactional wraps the service, so the catch-and-retry isn't undermined by transaction poisoning. Diff reviewed line-by-line; no new defect introduced.

### race-safe-execution-auto-create/F-02 [P2] closed - Execution lookup chain duplicated three times

**File:** backend/src/main/java/com/testmgmt/platform/testexecution/service/TestExecutionService.java:733, :752, :842
**Found:** 2026-08-12 by /audit (scope: current, features 11a+11b, commits e76be89..4de3828)
**Why it matters:** `getById()`, `update()`, and the new `resolveExecution()` (added in 11b for `listSteps`/`updateStep`) each inline the identical ~15-line project -> cycle -> cycleCase -> execution lookup chain. `resolveExecution()` was written specifically to avoid a third copy of this chain but `getById`/`update` were left un-migrated, so the duplication it was meant to prevent exists anyway. A future change to the lookup chain (e.g. an added scope check) now needs to be made in three places.
**Suggested fix:** Have `getById()` and `update()` call the existing `resolveExecution()` private helper instead of inlining their own copy of the same four lookups.
**Resolution:** Fixed via `fix/race-safe-execution-auto-create` Step 2 - `getById()`/`update()` now call `resolveExecution()`; the four-lookup chain exists in exactly one place. Regression-verified: valid case, testCaseId-not-in-cycle 404, and cross-project cycleId 404 all confirmed unchanged for both endpoints. **Closed by /audit re-examination (2026-08-12, scope: current)**: confirmed via diff that `getById`/`update` no longer inline the lookup chain, `list()`'s structurally-different bulk variant was correctly left untouched (not part of the original duplication), and no unused imports or dead code resulted from the consolidation.
