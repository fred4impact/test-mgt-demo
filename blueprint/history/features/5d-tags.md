# Feature: Tags

**From build-plan:** feature 5d
**Status:** complete

## Goal

Let a project maintain its own tag catalog and attach tags to test cases -
the last sub-item of build-plan item 5 (Test case repository). Once this
ships, item 5 itself is fully complete.

## In scope

- `Tag` JPA entity + Flyway migration (`V16__tags.sql`): `projectId`,
  `name`, unique on `(project_id, name)`
- `TestCaseTag` JPA entity + Flyway migration (`V17__test_case_tags.sql`):
  `testCaseId`, `tagId`, unique on `(test_case_id, tag_id)`
- `TagRepository`, `TestCaseTagRepository`
- A dedicated `tag` module (its own controller) for the catalog - mirrors
  `Role`'s pattern (3b): a project/org-scoped catalog the client creates
  into, not a seeded global one like `Permission`
- REST endpoints, nested under `/api/v1/projects/{projectId}/tags`:
  create, list - protected, project must belong to caller's org (existing
  pattern); duplicate `name` in the same project -> 409
- REST endpoints, nested under the existing `TestCaseController`
  (`/api/v1/projects/{projectId}/test-cases/{testCaseId}/tags`): attach a
  tag, detach, list a test case's tags - protected; attaching validates
  the tag belongs to the *same project* as the test case (reusing the
  same-project cross-entity check pattern 5a/5b/5c established), and that
  the test case itself belongs to the caller's org/project (existing
  `TestCaseRepository.findByIdAndProjectId`)
- Minimal protected frontend page at `/projects/[projectId]/tags`: create
  form (name only) + list of existing tags - mirrors `/projects/[projectId]/requirements`'s
  pattern from feature 4, since the tag catalog itself (unlike attaching a
  tag to a specific test case) is standalone and reachable the same way

## Out of scope

- Attaching/detaching tags to a test case from the frontend. Same reason
  as 5c's editing UI: the test case list (5b) renders plain text, not
  links, so there is nowhere to reach a *specific* test case's tag
  management from yet. API-only, same precedent as every join-table UI
  decision in this build (3a's `TeamMember`, 3b's `RolePermission`, 3c's
  `ProjectMember`)
- Tag update/delete, and detaching-then-deleting-the-catalog-entry
  cascades. Same no-update/no-delete-of-the-parent precedent as `Team`
  (3a), `Role` (3b), `Requirement` (4), `TestFolder` (5a)
- Case-insensitive or fuzzy tag-name matching. Uniqueness is a plain
  exact-string constraint on `(project_id, name)` - nothing in the plan
  asks for smarter matching, and inventing it risks surprising behavior
  (e.g. "Bug" vs "bug" silently colliding) that's easy to get wrong
- Tag colors, categories, or any metadata beyond `name`. The data model
  lists only `Tag`/`TestCaseTag` as a project-scoped many-to-many; nothing
  else is specified
- Filtering test cases by tag. That is explicitly feature 7 (Search &
  filters)'s job, same deferral `Requirement` and `TestCase` listing
  already made for filtering in general

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Tag + TestCaseTag entities, migrations, repositories** -
  `Tag` (`projectId`, `name`), unique `(project_id, name)`. `TestCaseTag`
  (`testCaseId`, `tagId`), unique `(test_case_id, tag_id)`.
  `V16__tags.sql`, `V17__test_case_tags.sql`. *Done when:* app starts
  cleanly under `ddl-auto: validate`, `flyway_schema_history` has 17 rows
  (V1-V17).
- [x] **Step 2 - Protected, project-scoped Tag catalog endpoints** - `POST
  /api/v1/projects/{projectId}/tags` (`name` required via Bean
  Validation), `GET /api/v1/projects/{projectId}/tags`. *Done when:*
  `flyway_schema_history` is unchanged at 17 rows (Step 1 already added
  both migrations); valid `POST` -> 201; `GET` list includes it; `POST`
  missing `name` -> 400; `POST` with a `name` that already exists in the
  same project -> 409; a tag inserted directly under a different project
  (same org) is excluded from `GET` list for this project; `POST` to a
  project inserted under a different org -> 404; no token -> 401.
- [x] **Step 3 - TestCaseTag attach/detach/list endpoints** - `POST
  /api/v1/projects/{projectId}/test-cases/{testCaseId}/tags` (`tagId`
  required via Bean Validation), `DELETE
  /api/v1/projects/{projectId}/test-cases/{testCaseId}/tags/{tagId}`,
  `GET /api/v1/projects/{projectId}/test-cases/{testCaseId}/tags`. The
  test case must belong to the caller's org/project (404 if not, existing
  `TestCaseRepository.findByIdAndProjectId`). Attaching validates the tag
  belongs to the *same project* (404 if it belongs to a different
  project, same "wrong scope or missing both 404" philosophy as 5a/5b/5c).
  Attaching an already-attached tag -> 409. Detaching an unattached tag ->
  404. *Done when:* `flyway_schema_history` unchanged at 17 rows; attach a
  tag to a test case -> 201, appears in that test case's tag list; attach
  the same tag again -> 409; attach a tag inserted directly under a
  *different* project (same org) -> 404; attach a random UUID as `tagId`
  -> 404; detach an unattached tag -> 404; detach the attached tag -> 204,
  no longer in the list; `POST` missing `tagId` -> 400; no token -> 401.
- [x] **Step 4 - Minimal protected frontend: tag catalog create/list** -
  `/projects/[projectId]/tags` page, same redirect-if-unauthenticated
  pattern as `/projects/[projectId]/requirements`, form (name only)
  POSTs to the backend, shows the result and existing tags. *Done when:*
  browser-driven - unauthenticated visit redirects to sign-in; signed in,
  creating a tag shows it in the re-fetched list.

## Files / areas

- `backend/.../tag/entity/Tag.java`, `TestCaseTag.java`
- `backend/.../tag/repository/TagRepository.java`, `TestCaseTagRepository.java`
- `backend/.../tag/dto/CreateTagRequest.java`, `TagDto.java`, `AddTestCaseTagRequest.java`
- `backend/.../tag/mapper/TagMapper.java`
- `backend/.../tag/service/TagService.java`, `TestCaseTagService.java`
- `backend/.../tag/controller/TagController.java`
- `backend/.../testcase/controller/TestCaseController.java` - add the three nested tag endpoints
- `backend/src/main/resources/db/migration/V16__tags.sql`, `V17__test_case_tags.sql`
- `frontend/services/tags.ts`, `frontend/actions/tags.ts`, `frontend/app/projects/[projectId]/tags/`

## Data / contracts

**Load-bearing:**
- `TagDto { id, projectId, name, createdAt }` - the attach endpoint's
  response and the test-case-tag-list endpoint both return this shape;
  any future feature reading a test case's tags (e.g. a future
  navigation/detail page) depends on it staying stable.
- Cross-entity same-project validation for attach (tag's project checked
  against the test case's project, not the caller's org directly) - the
  fourth instance of this exact pattern (5a's folder `parentId`, 5b/5c's
  `folderId`), now applied to a genuine many-to-many join rather than a
  self-reference or a single FK.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 1-3, including a same-org-
different-project tag attach attempt) and browser evidence (Step 4),
matching every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId`, and
  `TestCaseRepository.findByIdAndProjectId` directly.
- `TagRepository` needs a `findByIdAndProjectId` method for the attach
  validation, same shape as `TestFolderRepository`'s and
  `RoleRepository`'s - don't reuse a bare `findById`.
- Nest the `TestCaseTag` endpoints on the existing `TestCaseController`,
  not a new controller - same precedent as `ProjectMember` nesting on
  `ProjectController` (3c) and `RolePermission` nesting on
  `RoleController` (3b): a join table's endpoints live with the entity
  they're "under" in the URL, not with the entity on the other side of
  the join.
- `ConflictException` (already used for duplicate `Project.key`) is the
  right exception for both the duplicate tag-name case (Step 2) and the
  already-attached case (Step 3) - don't introduce a new exception type.
- This is the last sub-item of build-plan item 5 - once `/complete` runs,
  item 5's parent checkbox should be checked too, since 5a/5b/5c/5d will
  all be done.

## Build notes (from implementation)

This feature built cleanly against the spec with no code surprises - every
pattern reused from 5a/5b/5c/3b/3c transferred directly with no adaptation
bugs. Both distinguishing checks called out in the spec were proven with
real fixtures, not just implemented:

- A tag created directly in a same-org sibling project was confirmed
  excluded from the owning project's catalog list (Step 2), and separately
  confirmed rejected with 404 when used as a `tagId` on an attach request
  against a test case in a different project (Step 3) - two distinct
  proofs of the same underlying same-project invariant, at the list level
  and the write level.
- Duplicate-name (`409`) and already-attached (`409`) were verified as
  genuinely separate code paths, both correctly using the existing
  `ConflictException`.
- All fixture rows (sibling-project tags, cross-org organizations/users/
  projects) were deleted after each proof.

This closes out build-plan item 5 (Test case repository) in full:
5a (Test folders), 5b (Test cases & steps), 5c (Test case editing &
version history), and 5d (Tags) are all complete.
