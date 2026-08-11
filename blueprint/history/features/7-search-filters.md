# Feature: Search & filters

**From build-plan:** feature 7
**Status:** complete

## Goal

Let a project's existing `Requirement` and `TestCase` list endpoints be
searched by keyword and filtered by field, instead of always returning
every row. No new entity, no new migration - this extends the two list
endpoints already built (4, 5b) with optional query parameters.

## In scope

- `GET /api/v1/projects/{projectId}/requirements` gains optional query
  params: `q` (case-insensitive substring match against `title` or
  `key`), `status`, `priority` (both exact match against the stored
  value, since neither field has an enforced enum) - all provided
  criteria combine with AND; omitting all params returns every
  requirement, exactly as today (backward compatible)
- `GET /api/v1/projects/{projectId}/test-cases` gains the same `q` plus
  `status`, `priority`, `severity`, `testType`, `automationStatus`,
  `folderId` (all exact match, all AND'd together, all optional)
- Spring Data JPA `Specification`-based filtering
  (`JpaSpecificationExecutor`) for both `RequirementRepository` and
  `TestCaseRepository` - the idiomatic way to compose an arbitrary subset
  of optional predicates without a combinatorial explosion of derived
  query methods
- Frontend: add a search box plus the same filter inputs (plain text,
  matching the backend's no-enum design) to the existing
  `/projects/[projectId]/requirements` and
  `/projects/[projectId]/test-cases` pages - a plain GET form
  (`method="get"`) so filtering re-renders server-side via Next.js
  `searchParams`, no client-side state needed

## Out of scope

- Defects. Build-plan item 7's own line names "test cases/requirements/
  defects", but `Defect` doesn't exist yet (item 12). Same deferral
  precedent already established repeatedly in this build (`Requirement`'s
  `releaseId`, `TestCase`'s `releaseId`, `Permission`'s unbuilt-module
  codes): defect search is added when `Defect` itself is built, not
  invented against a table that doesn't exist. Not a structural blocker
  like the Project/Role dependency in feature 2 was - `Requirement` and
  `TestCase` search work today without `Defect` existing at all
- Searching or filtering `TestFolder`, `TestSuite`, or `Tag` lists. Not
  named in the build-plan line; those stay as-is
- Full-text search (ranking, stemming, fuzzy matching, OpenSearch).
  `q` is a plain case-insensitive `LIKE '%term%'` substring match -
  `project-overview.md`'s own tech stack section says OpenSearch is
  "introduced once Postgres search isn't enough," and nothing in this
  build has hit that threshold yet
- Sorting/pagination. Neither list endpoint has ever paginated; adding
  filters doesn't change that. A future feature can add it when list
  sizes actually warrant it
- Filtering by `ownerId` or date ranges. Not requested by the build-plan
  line ("keyword search and filtering"), and free-form date-range query
  params are a meaningfully different, unrequested feature
- Saving filter combinations. The data model lists a `SavedFilter` shape
  in `project-overview.md`'s "Not yet built by any feature" section with
  no build-plan item - explicitly out of scope until one exists

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Requirement search & filters (backend)** -
  `RequirementRepository extends JpaSpecificationExecutor<Requirement>`;
  a `RequirementSpecifications` helper building `projectId` (always),
  `q` (`title` or `key` `LIKE` case-insensitive), `status`, `priority`
  predicates, combined with `Specification.allOf`/`.and`; `list()` in
  `RequirementService` takes the new optional params and passes them
  through; `RequirementController`'s existing `GET` gains
  `@RequestParam(required = false)` for `q`, `status`, `priority`. *Done
  when:* `GET .../requirements` with no params returns every requirement
  in the project (unchanged from today); `?q=<substring of an existing
  title>` returns only matches, case-insensitively; `?status=ACTIVE`
  returns only that status; `?priority=HIGH` returns only that priority;
  `?q=...&status=...` combined returns only rows matching both; a query
  matching nothing returns `[]`, not an error; no token -> 401 (existing
  behavior, reconfirmed).
- [x] **Step 2 - TestCase search & filters (backend)** - same shape as
  Step 1, applied to `TestCaseRepository`/`TestCaseService`/
  `TestCaseController`: `q` (`title` or `key`), plus `status`,
  `priority`, `severity`, `testType`, `automationStatus`, `folderId`
  (exact match on the id). *Done when:* `GET .../test-cases` with no
  params returns every test case (unchanged); `?q=...` matches
  case-insensitively; each of `status`/`priority`/`severity`/`testType`/
  `automationStatus`/`folderId` filters correctly in isolation; two
  combined (e.g. `?status=ACTIVE&folderId=...`) returns only rows
  matching both; a query matching nothing returns `[]`; no token -> 401.
- [x] **Step 3 - Requirements page: search box + filters (frontend)** -
  a GET form on `/projects/[projectId]/requirements` with a keyword
  input and `status`/`priority` text inputs, submitting to the same URL
  with query params; the page reads `searchParams` and passes them to
  `listRequirements`. *Done when:* browser-driven - typing a keyword that
  matches one existing requirement's title and submitting shows only
  that requirement; clearing the field and resubmitting shows the full
  list again.
- [x] **Step 4 - Test cases page: search box + filters (frontend)** -
  same pattern on `/projects/[projectId]/test-cases`: keyword input plus
  `status`/`priority`/`severity`/`testType`/`automationStatus`/`folderId`
  inputs (the last as a `<select>` populated from the page's existing
  folder list, not free text, since folder ids aren't human-typable).
  *Done when:* browser-driven - filtering by a keyword that matches one
  existing test case's title shows only that one; filtering by an
  existing folder via the select shows only test cases in that folder;
  clearing all filters and resubmitting shows the full list again.

## Files / areas

- `backend/.../requirement/repository/RequirementRepository.java` - add `JpaSpecificationExecutor`
- `backend/.../requirement/specification/RequirementSpecifications.java` - new
- `backend/.../requirement/service/RequirementService.java`, `controller/RequirementController.java` - extend `list`
- `backend/.../testcase/repository/TestCaseRepository.java` - add `JpaSpecificationExecutor`
- `backend/.../testcase/specification/TestCaseSpecifications.java` - new
- `backend/.../testcase/service/TestCaseService.java`, `controller/TestCaseController.java` - extend `list`
- `frontend/services/requirements.ts` - `listRequirements` takes optional filter params
- `frontend/app/projects/[projectId]/requirements/page.tsx` - read `searchParams`, add filter form
- `frontend/services/testcases.ts` - `listTestCases` takes optional filter params
- `frontend/app/projects/[projectId]/test-cases/page.tsx` - read `searchParams`, add filter form

## Data / contracts

No new persisted shape or migration - `RequirementDto`/`TestCaseDto` are
unchanged. The only new contract is the query-string shape itself
(`q`, `status`, `priority`, and for test cases also `severity`,
`testType`, `automationStatus`, `folderId`), which is not load-bearing for
any other feature - it's a read-side filter, not stored data.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 1-2, covering every filter in
isolation, in combination, and the no-params backward-compatibility case)
and browser evidence (Steps 3-4), matching every prior feature. `/check`
re-proved all 17 done-whens fresh against the running app before this
feature was completed.

## Notes for the AI

- Use `Specification.where(...).and(...)` (or `allOf`, depending on the
  Spring Data version already on the classpath - check
  `RoleRepository`/`ProjectRepository`'s imports for the Spring Data JPA
  version in use before picking the exact API) and only add a predicate
  when its corresponding param is non-null/non-blank - an absent filter
  must not narrow the result set at all.
- `q` matching both `title` and `key` needs an `OR` *inside* the overall
  `AND` chain: `(title ILIKE %q% OR key ILIKE %q%) AND status = ... AND
  ...`. Don't flatten this into one big `AND` of everything or the `OR`
  semantics break.
- `projectId` scoping is still mandatory and still the first predicate
  applied, exactly as before - filters narrow *within* a project, they
  never replace the org/project scoping check.
- Do not touch `getById` in either controller - only the list endpoints
  gain query params, per scope.
- Frontend `<select>` for `folderId` (Step 4) reuses the same folder list
  the page's create-test-case form already fetches - don't add a second
  fetch for the same data.

## Build notes (from implementation)

- **Real bug found and fixed in Step 4**: the new `TestCaseFilterForm`'s
  folder `<select>` was first written with `id="folderId"`, colliding
  with `CreateTestCaseForm`'s existing folder select on the same page -
  invalid duplicate HTML ids. This silently broke `<label htmlFor>`
  association and made the verification script's `#folderId` selector
  hit the wrong element, causing the folder filter to appear to work
  (via a false-positive `waitForURL` match on an empty `folderId=`
  param) while actually filtering nothing, and separately caused the
  "Clear" link to fail to render (since `hasFilters` read the empty
  value as falsy). Fixed by renaming the filter select's `id` to
  `filterFolderId`, keeping `name="folderId"` for the query param since
  only `id` needs page-wide uniqueness. Caught by the verification
  script itself failing on the "Clear" step, not by code review.
- Both Spring Data JPA `Specification` API details flagged as
  uncertain in the spec's Notes for the AI were resolved by inspecting
  the actual `spring-data-jpa-4.1.0.jar` classfile via `javap` before
  writing any code: `Specification.where()`/`.and()`/`.allOf()` are all
  present and behave as expected, and `JpaSpecificationExecutor.findAll(Specification, Sort)`
  returns a plain `List<T>` (no pagination wrapper needed).
- `/check` re-ran the entire done-when matrix (7 Requirement claims, 9
  TestCase claims, both frontend flows) fresh against the running app -
  same backend process used across Steps 1-2 (confirmed via `ps` uptime,
  code unchanged since), a newly started frontend dev server - rather
  than relying on evidence captured during `/implement`.
