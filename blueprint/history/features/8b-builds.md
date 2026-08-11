# Feature: Builds

**From build-plan:** feature 8b
**Status:** complete

## Goal

Let a project record builds (CI artifacts / deployable versions), each tied
to a `Release`. This is the second of the three 8x sub-items (`Release` from
8a, `Environment` still to come as 8c), and it's a dependency of feature 10
(Test cycles): `TestCycle.buildId` needs `Build` to exist first.

## In scope

- `Build` JPA entity + Flyway migration (`V23__builds.sql`): `projectId`,
  `releaseId` (required, `NOT NULL REFERENCES releases(id)`), `name`,
  `version` (nullable), `branch` (nullable), `commitSha` (nullable), `status`
  (default `'ACTIVE'` - a build represents something already produced, so it
  starts usable, matching `Project`/`Requirement`/`TestCase`'s precedent
  rather than `Release`'s `'PLANNED'` default, since a release is a future
  target and a build is an artifact that already exists once recorded)
- `BuildRepository`, with `findByIdAndProjectId` (same-project lookup, same
  shape as `ReleaseRepository`'s from 8a) and
  `findByProjectIdOrderByCreatedAtAsc`
- A dedicated `build` module (its own controller)
- REST endpoints, nested under `/api/v1/projects/{projectId}/builds`: create,
  get by id, list - protected, project must belong to caller's org (existing
  pattern)
- `releaseId` on create must resolve to a `Release` in the *same project*
  (same-project cross-entity check, same technique 8a used for
  `Requirement.releaseId`/`TestCase.releaseId`) - 404 if not
- Minimal protected frontend page at `/projects/[projectId]/builds`: create
  form (name + a release picker, since `releaseId` is required here, unlike
  8a's optional retrofit fields which had no form UI at all) + list showing
  each build's name, status, and release
- A "Builds" link-card added to the project hub
  (`/projects/[projectId]/page.tsx`, built in 17a), matching that feature's
  own stated intent to add a card per section as it ships

## Out of scope

- `Environment`. Explicitly 8c.
- `TestCycle`. Explicitly feature 10 - this feature only builds the artifact
  record, not what runs against it.
- Build update/delete. Same no-update precedent as every catalog entity so
  far (`Team`, `Role`, `Requirement`, `TestFolder`, `Tag`, `TestSuite`,
  `Release`).
- Enforcing a build status workflow (e.g. `ACTIVE` -> `DEPRECATED`
  transitions). `status` is a plain string field, same lightweight precedent
  as every other `status` field in this build - no state machine, no enum
  validation.
- Any FK from other entities *to* `Build` beyond what already exists in the
  data model (`TestCycle.buildId`, `Defect.buildId`, `AutomationRun.buildId`).
  Those are the other entities' job when they're built (items 10, 12, 16) -
  `Build` itself doesn't need to know about them.
- Filtering `Build` lists by `releaseId` or status, or adding builds to
  feature 7's search/filter set. Not implied by this feature.
- Changing a build's `releaseId` after creation, or reassigning it. No update
  endpoint exists at all (see above), so this falls out naturally.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Build entity, migration, repository** - `Build` (`projectId`,
  `releaseId` NOT NULL FK to `releases(id)`, `name`, `version`/`branch`/
  `commitSha` nullable, `status` default `'ACTIVE'`). `V23__builds.sql`.
  *Done when:* app starts cleanly under `ddl-auto: validate`,
  `flyway_schema_history` has 23 rows (V1-V23).
- [x] **Step 2 - Protected, project-scoped Build endpoints** - `POST
  /api/v1/projects/{projectId}/builds` (`name` and `releaseId` required via
  Bean Validation, `version`/`branch`/`commitSha`/`status` optional), `GET
  /api/v1/projects/{projectId}/builds/{id}`, `GET
  /api/v1/projects/{projectId}/builds`. `releaseId` must resolve to a
  `Release` in the same project (404 if not - same technique as 8a's
  `Requirement`/`TestCase` retrofit). *Done when:* `flyway_schema_history`
  unchanged at 23 rows; valid `POST` with `name` + a real same-project
  `releaseId` -> 201, `status: "ACTIVE"`; `POST` with `version`/`branch`/
  `commitSha` set -> 201, all present in the response; `POST` missing `name`
  -> 400; `POST` missing `releaseId` -> 400; `POST` with a `releaseId` from a
  release inserted directly under a *different* project (same org) -> 404;
  `POST` with a random non-existent `releaseId` -> 404; `GET` list includes
  it; `GET` by id returns it; `POST` to a project inserted under a different
  org -> 404; no token -> 401.
- [x] **Step 3 - Minimal protected frontend: build catalog create/list + hub
  card** - `/projects/[projectId]/builds` page, same
  redirect-if-unauthenticated pattern as `/projects/[projectId]/releases`;
  create form has `name` and a `<select>` populated from the project's
  releases (reuse `listReleases` from `services/releases.ts`), POSTs to the
  backend; list shows each build's name, status, and release name (join
  client-side against the already-fetched releases list, no new backend
  endpoint needed); project hub (`/projects/[projectId]/page.tsx`) gains a
  "Builds" link-card alongside the existing sections. *Done when:*
  browser-driven - unauthenticated visit redirects to sign-in; signed in
  with at least one release already created, creating a build against that
  release shows it (with its default `ACTIVE` status and the release's
  name) in the re-fetched list; if the project has zero releases yet, the
  form clearly shows there's nothing to select rather than submitting an
  empty `releaseId`; the project hub shows a working "Builds" link.

## Files / areas

- `backend/.../build/entity/Build.java`
- `backend/.../build/repository/BuildRepository.java`
- `backend/.../build/dto/CreateBuildRequest.java`, `BuildDto.java`
- `backend/.../build/mapper/BuildMapper.java`
- `backend/.../build/service/BuildService.java`
- `backend/.../build/controller/BuildController.java`
- `backend/src/main/resources/db/migration/V23__builds.sql`
- `frontend/services/builds.ts`, `frontend/actions/builds.ts`,
  `frontend/app/projects/[projectId]/builds/`
- `frontend/app/projects/[projectId]/page.tsx` (adds the Builds card)

## Data / contracts

**Load-bearing:**
- `BuildDto { id, projectId, releaseId, name, version, branch, commitSha,
  status, createdAt }` - item 10 (`TestCycle.buildId`), item 12
  (`Defect.buildId`), and item 16 (`AutomationRun.buildId`) all reference
  `Build.id` directly. Don't change this shape once those land.
- `releaseId` is required and immutable (no update endpoint) - any later
  feature reading `BuildDto.releaseId` can rely on it always being present,
  never `null`, unlike `Requirement.releaseId`/`TestCase.releaseId` from 8a
  which are optional.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verify with `curl` output (Steps 1-2, including the same-project retrofit
validation and the missing-`releaseId` 400 case) and browser evidence (Step
3), matching every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId` directly.
- Reuse `ReleaseRepository.findByIdAndProjectId` (already exists from 8a) for
  the `releaseId` same-project validation - don't add a second method that
  does the same thing.
- `CreateBuildRequest.releaseId` should be a required field (`@NotNull`),
  unlike 8a's `releaseId` retrofit fields which were optional - this is the
  one meaningful shape difference from 8a's pattern, called out so it isn't
  copy-pasted as optional by mistake.
- Step 3's release `<select>` reuses `listReleases(accessToken, projectId)`
  from `frontend/services/releases.ts` (built in 8a) - no new backend
  endpoint needed to populate it.
- The project hub's `SECTIONS` array (in
  `app/projects/[projectId]/page.tsx`, built in 17a) gains one more entry
  (`{ href: "builds", label: "Builds" }`) - a one-line addition, not a hub
  redesign.
- No date parsing involved (`Build` has no date fields), so this feature is
  simpler than 8a on that axis.

## Build notes (from implementation)

Built cleanly against the spec, no code surprises - every pattern (same-project
cross-entity validation via `ReleaseRepository.findByIdAndProjectId`,
required-vs-optional `releaseId` distinction, hub card addition) transferred
directly from 8a and 17a.

- All 10 endpoint cases in Step 2's done-when were proven with real `curl`
  output against a freshly restarted backend, including two genuine isolation
  proofs: a `releaseId` from a sibling project (same org) and a `POST` to a
  project row inserted under a genuinely different organization (both fixtures
  built directly via `psql`, both cleaned up after). An earlier attempt at the
  cross-org proof produced a false 404 because the fixture insert had silently
  rolled back (a `NOT NULL` violation on a later statement in the same `psql
  -c` batch rolled back the whole implicit transaction) - re-inserted with a
  valid `owner_id` to get a genuine proof, not a nonexistent-row coincidence.
- One Keycloak access-token expiry was hit mid-verification (tokens are
  short-lived) and surfaced as an unexpected `401` on a case that had
  previously passed - not a bug, just re-issued a fresh token and reran.
- The frontend browser verification caught a bug in the *test script*, not
  the app: `AppNav`'s "Sign out" button also has `type="submit"` and renders
  earlier in the DOM than the create-build form's submit button, so an
  unscoped `page.click('button[type="submit"]')` selector clicked "Sign out"
  instead, producing a false failure (form submission appeared to silently
  log the user out). Fixed by scoping the selector to the form. Worth keeping
  in mind for any future Playwright script on an authenticated page, since
  every such page now carries a nav with its own `type="submit"` button.
