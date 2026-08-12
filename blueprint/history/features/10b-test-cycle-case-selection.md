# Feature: Test cycle case selection

**From build-plan:** feature 10b
**Status:** complete

## Goal

Let a project browse its test case repository and select cases into a test
cycle, optionally assigning a tester per case (build-plan's own wording).
This is the actual capability you asked for: "browse the test repository...
to select test cases... to add to the [cycle]." It's also the first
nested detail page this app has - every prior entity has been a flat
project-level list; a test cycle now gets its own
`/projects/[projectId]/test-cycles/[cycleId]` page.

## In scope

- `TestCycleCase` JPA entity + Flyway migration
  (`V27__test_cycle_cases.sql`): `cycleId` (`NOT NULL REFERENCES
  test_cycles(id)`), `testCaseId` (`NOT NULL REFERENCES test_cases(id)`),
  `assigneeId` (nullable - "optionally assigning a tester"),
  `sortOrder` (auto-incremented, same pattern as `TestSuiteCase.sortOrder`:
  `count of existing rows for this cycle + 1`). This mirrors
  `TestSuiteCase`'s exact shape (entity, repository, service structure)
  plus `assigneeId` - `TestSuiteCase`'s add/remove/list pattern is the
  direct template.
- `TestCycleCaseRepository`: `findByCycleIdOrderBySortOrderAsc`,
  `findByCycleIdAndTestCaseId`, `existsByCycleIdAndTestCaseId`,
  `countByCycleId` (identical method shapes to `TestSuiteCaseRepository`)
- REST endpoints nested under
  `/api/v1/projects/{projectId}/test-cycles/{cycleId}/cases`: `POST` (add
  - `testCaseId` required, `assigneeId` optional), `DELETE /{testCaseId}`
  (remove), `GET` (list) - same three-endpoint shape as
  `TestSuiteController`'s `/cases` sub-resource
- Validation on add: `testCaseId` must resolve to a `TestCase` in the same
  project (404 if not, reusing `TestCaseRepository.findByIdAndProjectId`);
  if `assigneeId` is provided, it must be a real project member (404 if
  not, reusing `ProjectMemberRepository.existsByProjectIdAndUserId` from
  feature 3c); adding a `testCaseId` already in the cycle is a 409
  conflict (same precedent as `TestSuiteCase`'s duplicate-add handling)
- `getTestCycle(accessToken, projectId, cycleId)` added to
  `frontend/services/testcycles.ts` (the `GET .../test-cycles/{id}`
  endpoint already exists from 10a, just unused by the frontend so far)
- `listProjectMembers(accessToken, projectId)` added to a new
  `frontend/services/projectmembers.ts` (the `GET .../members` endpoint
  already exists from feature 3c, also unused by the frontend so far -
  needed here to show assignee names and populate the assignee picker)
- New nested page `/projects/[projectId]/test-cycles/[cycleId]`: cycle
  header (name, status), a "Selected test cases" table (Key, Title,
  Assignee name, Remove button), and a "Add test cases" section reusing
  feature 7's `TestCaseFilterForm` + `listTestCases` to browse the full
  repository - each row not already in the cycle gets an assignee `<select>`
  (defaulting to "Unassigned") and an "+ Add" button; rows already in the
  cycle show an "Added" label instead
- The test cycles list page (`/projects/[projectId]/test-cycles`) links
  each row's name to its new detail page

## Out of scope

- Reassigning a tester after add, or changing `sortOrder` (drag-to-reorder).
  No update endpoint, same no-update precedent as everywhere else - to
  change an assignment, remove and re-add. This is a real limitation but
  matches how every other join table in this app already works
  (`TestSuiteCase` has no update either).
- `TestExecution` (recording pass/fail/blocked/skipped against a selected
  case). Explicitly feature 11 - this feature only builds the selection,
  not what runs against it. Feature 11 will read `test_cycle_cases` to
  know which cases are in scope for a cycle, but nothing here creates an
  execution record.
- Bulk add (select many cases at once and add them in one action). One
  row, one add action, matching the granularity of every other join-table
  interaction in this app (`TestSuiteCase` is one-at-a-time too).
- Editing the assignee picker's options beyond what `GET .../members`
  already returns. No new project-membership UI - this reuses the
  existing endpoint from 3c, doesn't build a members page.
- Any change to `TestCaseFilterForm` or `listTestCases` - reused exactly
  as feature 7 built them, no new filter fields.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestCycleCase entity, migration, repository** -
  `TestCycleCase` (`cycleId`, `testCaseId` both NOT NULL FKs,
  `assigneeId` nullable, `sortOrder` NOT NULL, `createdAt` only - no
  `updatedAt`, matching `TestSuiteCase`'s exact shape).
  `V27__test_cycle_cases.sql`. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 27 rows (V1-V27).
- [x] **Step 2 - Protected test cycle case endpoints** - `POST
  /api/v1/projects/{projectId}/test-cycles/{cycleId}/cases`
  (`testCaseId` required, `assigneeId` optional), `DELETE
  .../cases/{testCaseId}`, `GET .../cases`. *Done when:*
  `flyway_schema_history` unchanged at 27 rows; valid `POST` with a real
  same-project `testCaseId` -> 201, `sortOrder: 1`; a second `POST` with a
  different `testCaseId` -> 201, `sortOrder: 2`; `POST` with a
  `testCaseId` from a different project (same org) -> 404; `POST` with a
  real `assigneeId` (an actual project member) -> 201, `assigneeId`
  present; `POST` with an `assigneeId` that isn't a project member -> 404;
  `POST` the same `testCaseId` twice -> 409 on the second; `GET` list
  returns both added cases in `sortOrder`; `DELETE` an added case -> 204,
  and it no longer appears in `GET`; `DELETE` a `testCaseId` never added
  -> 404; `POST`/`DELETE`/`GET` to a `cycleId` from a different project
  (same org) -> 404; no token -> 401.
- [x] **Step 3 - Cycle detail page: header + selected cases + remove** -
  `getTestCycle` added to `services/testcycles.ts`; new
  `/projects/[projectId]/test-cycles/[cycleId]/page.tsx` fetches the
  cycle, its already-selected cases, and project members (for assignee
  name display); renders cycle name/status header, a "Selected test
  cases" table (Key, Title, Assignee name or "Unassigned", Remove
  button); the test cycles list page's rows link to this new page. *Done
  when:* browser-driven - unauthenticated visit redirects to sign-in;
  clicking a cycle from the list page lands on its detail page showing
  the right name/status; a case added via `curl` in Step 2's testing
  appears in the table with the right assignee name; clicking Remove on a
  case removes it from the table (and a page reload confirms it's gone,
  not just hidden client-side).
- [x] **Step 4 - Browse repository & add cases** - the detail page gains
  an "Add test cases" section reusing `TestCaseFilterForm` (and
  `listTestFolders`/`listTestCases` exactly as feature 7's Test Cases
  page already calls them) to browse/filter the project's full test case
  repository; each row not already in the cycle shows an assignee
  `<select>` (options from the already-fetched project members, plus
  "Unassigned") and an "+ Add" button; rows already in the cycle show
  "Added" instead of the add controls. *Done when:* browser-driven -
  the repository browse section shows real test cases with working
  filters (same query params feature 7 already proved); adding a case
  with no assignee selected shows it as "Unassigned" in the Selected
  table; adding a case with an assignee selected shows the right name;
  the just-added case's browse row switches to "Added" after the page
  reloads; a case already in the cycle never shows duplicate add controls.

## Files / areas

- `backend/.../testcyclecase/entity/TestCycleCase.java`
- `backend/.../testcyclecase/repository/TestCycleCaseRepository.java`
- `backend/.../testcyclecase/dto/AddTestCycleCaseRequest.java`, `TestCycleCaseDto.java`
- `backend/.../testcyclecase/mapper/TestCycleCaseMapper.java`
- `backend/.../testcyclecase/service/TestCycleCaseService.java`
- `backend/.../testcycle/controller/TestCycleController.java` (gains the
  `/cases` sub-resource endpoints - same controller as 10a, matching how
  `TestSuiteController` owns both `TestSuite` and `TestSuiteCase`
  endpoints)
- `backend/src/main/resources/db/migration/V27__test_cycle_cases.sql`
- `frontend/services/testcycles.ts` (adds `getTestCycle`)
- `frontend/services/projectmembers.ts` (new)
- `frontend/actions/testcyclecases.ts` (new - add/remove actions)
- `frontend/app/projects/[projectId]/test-cycles/[cycleId]/page.tsx` (new)
- `frontend/app/projects/[projectId]/test-cycles/page.tsx` (rows become links)

## Data / contracts

- `TestCycleCaseDto { testCaseId, key, title, assigneeId, sortOrder,
  addedAt }` - mirrors `TestSuiteCaseDto`'s shape plus `assigneeId`. Not
  strictly load-bearing (no future entity stores a `testCycleCaseId` FK),
  but feature 11's `TestExecution` will check for a matching
  `(cycleId, testCaseId)` pair in `test_cycle_cases` before allowing
  execution - the *existence* of that pairing, not this DTO's shape, is
  what feature 11 depends on.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verify with `curl` output (Steps 1-2, including the same-project
`testCaseId` check, the project-member `assigneeId` check, and the
duplicate-add 409) and browser evidence (Steps 3-4), matching every prior
feature.

## Notes for the AI

- `TestSuiteCaseService`/`TestSuiteCaseRepository`/`TestSuiteCase`
  (`backend/.../testsuite/`) are the direct template for this feature's
  backend - same method names, same add/remove/list shape, same
  sortOrder-by-count pattern, same duplicate-add `ConflictException`.
  Copy the pattern, don't redesign it.
- `assigneeId` validation reuses
  `ProjectMemberRepository.existsByProjectIdAndUserId` (already exists
  from feature 3c) - don't add a new repository method.
- Per-row "add" and "remove" actions in Steps 3-4 don't need
  `useActionState`/client components - follow `AppNav.tsx`'s pattern of a
  plain `<form action={...}>` with an inline or imported server action,
  since these are simple fire-and-reload actions with no client-side
  pending/error UI needed (a rare failure surfaces via Next's default
  error boundary, which is an acceptable simplification for this
  feature - don't build custom per-row error handling).
- Assignee display name: the DTO returns `assigneeId` only (a raw UUID) -
  join it to a display name client-side against the already-fetched
  project members list, same pattern already used for release/build/plan
  name joins throughout this app. Don't add a server-side join in the
  service layer.
- The "Add test cases" section's filter form is `TestCaseFilterForm`
  reused verbatim (same component, same props) - don't fork or duplicate
  it.
- Cross-reference "already in this cycle" by building a `Set<string>` of
  `testCaseId`s from the already-fetched selected-cases list, checked
  per row while rendering the repository browse table.

## Build notes (from implementation)

Built cleanly against the spec - `TestSuiteCase`'s exact structure was the
direct template and transferred without any redesign, plus `assigneeId`
and its project-member validation.

- All 9 endpoint cases in Step 2's done-when were proven with real `curl`
  output against a freshly restarted backend, using real fixtures (self
  as a project member, three real test cases in a real folder). One
  proof was initially written wrong and caught before being accepted: a
  "bad assigneeId" test used a nonexistent `testCaseId` too, so it only
  proved the `testCaseId` check fired first, not that the assignee check
  works at all. Fixed by isolating the check with a real, not-yet-added
  `testCaseId` paired with a bogus `assigneeId`, and confirmed the failed
  add left no partial row behind.
- This is the first nested detail page in the app
  (`/test-cycles/[cycleId]`), and the per-row add/remove actions proved
  out a new-to-this-codebase but Next.js-native pattern: server actions
  partially applied via `.bind()` directly in a `<form action={...}>`,
  no client component or `useActionState` needed for either action.
- The full round-trip was proven in the browser, not just curl: browsed
  the real repository with feature 7's exact filter form, added one case
  with no assignee (showed "Unassigned"), added a second with a real
  assignee (showed the correct name), and confirmed both immediately
  showed "Added" in the browse table with no duplicate add controls.
- This closes out build-plan item 10 (Test cycles) entirely - 10a and 10b
  both done, so the parent item gets checked off too. Feature 11 (Test
  execution) is next and now has everything it needs: cycles exist, and
  cases are selected into them via `test_cycle_cases`.
