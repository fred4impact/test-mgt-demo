# Feature: Test folders

**From build-plan:** feature 5a
**Status:** complete

## Goal

Let a project organize its test cases into a nested folder hierarchy.
`TestFolder` is the container feature 5b's `TestCase` will live inside via
`folderId` - this feature only builds the folders themselves, empty of
content, since test cases don't exist yet.

## In scope

- `TestFolder` JPA entity + Flyway migration (`V12__test_folders.sql`):
  `projectId`, `name`, `parentId` (nullable, self-referencing FK to
  `test_folders.id`, null means a root-level folder)
- `TestFolderRepository`
- A dedicated `testfolder` module (its own controller), matching the
  `requirement` module's pattern from feature 4
- REST endpoints, nested under
  `/api/v1/projects/{projectId}/test-folders`: create, get by id, list -
  protected; the project must belong to the caller's org (reusing
  `ProjectRepository.findByIdAndOrganizationId`, same as every prior
  project-scoped feature); creating a nested folder validates that
  `parentId`, when given, references a folder that belongs to the *same
  project*

## Out of scope

- Any frontend page. Browsing an empty folder tree with no test cases
  inside it yet has little standalone value - the natural place for a
  folder picker/tree view is feature 5b, where creating or browsing a test
  case needs one anyway. This is the same call already made for 3c
  (`ProjectMember`): skip the UI until there's a real reason to look at
  the data
- Folder update, delete, or move (re-parenting). Same no-update precedent
  as `Team` (3a), `Role` (3b), `Requirement` (4). Because folders can only
  be created pointing at an *already-existing* parent, and there is no
  move/update endpoint, a folder can never be re-pointed after creation -
  so cycles in the hierarchy are structurally impossible without any extra
  cycle-detection logic being needed
- Folder depth limits or a maximum nesting rule - nothing in the plan
  specifies one, so none is invented
- Listing folders as a tree structure server-side. `GET
  .../test-folders` returns a flat list with each folder's `parentId`;
  building a tree view from that is a frontend concern, deferred to 5b
  alongside the rest of the UI

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - TestFolder entity, migration, repository** - `TestFolder`
  (`projectId`, `name`, `parentId` nullable). `V12__test_folders.sql`.
  *Done when:* app starts cleanly under `ddl-auto: validate`,
  `flyway_schema_history` has 12 rows (V1-V12).
- [x] **Step 2 - Protected, project-scoped REST endpoints with
  parent-folder validation** - `POST
  /api/v1/projects/{projectId}/test-folders` (`name` required via Bean
  Validation, `parentId` optional), `GET
  /api/v1/projects/{projectId}/test-folders/{id}`, `GET
  /api/v1/projects/{projectId}/test-folders`. The project must belong to
  the caller's org (404 if not, existing pattern). When `parentId` is
  given, it must resolve to a `TestFolder` in the *same* project (404 if
  it belongs to a different project, or doesn't exist at all - same
  "don't distinguish wrong-scope from missing" philosophy as every prior
  cross-entity check). *Done when:* `flyway_schema_history` is unchanged
  at 12 rows (Step 1 already added the migration); create a root folder
  (no `parentId`) -> 201; create a second folder with `parentId` set to
  the first -> 201; `GET` list includes both, each with its correct
  `parentId`; `GET` by id returns one of them; `POST` missing `name` ->
  400; `POST` with a `parentId` from a folder inserted directly under a
  *different* project (but the same org) -> 404; `POST` with a random
  non-existent `parentId` -> 404; `POST` to a project inserted under a
  different org -> 404; no token -> 401.

## Files / areas

- `backend/.../testfolder/entity/TestFolder.java`
- `backend/.../testfolder/repository/TestFolderRepository.java`
- `backend/.../testfolder/dto/CreateTestFolderRequest.java`, `TestFolderDto.java`
- `backend/.../testfolder/mapper/TestFolderMapper.java`
- `backend/.../testfolder/service/TestFolderService.java`
- `backend/.../testfolder/controller/TestFolderController.java`
- `backend/src/main/resources/db/migration/V12__test_folders.sql`

## Data / contracts

**Load-bearing:**
- `TestFolderDto { id, projectId, parentId, name, createdAt }` - feature
  5b's `TestCase.folderId` will reference `TestFolder.id` directly; this
  shape must not change once 5b is built against it.
- Same-project parent validation pattern (a self-referencing FK checked
  against the *creating request's own project*, not the caller's org
  directly) - the first feature where the cross-entity check is against a
  sibling of the same type rather than a different entity type. Later
  nested structures (`TestSuite.parentId` per the data model) are
  expected to follow the identical pattern.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output, including a same-org-different-project
parent attempt and a nonexistent-parent attempt, matching every prior
feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`, and
  `ProjectRepository.findByIdAndOrganizationId` directly - a test folder's
  org scoping is entirely inherited through its project, there is no
  `organizationId` column on `TestFolder` itself (matches the data model:
  only `projectId` and `parentId` are listed).
- The parent-folder check needs a repository method scoped by *both* id
  and `projectId` (`findByIdAndProjectId`), not a bare `findById` - a
  `parentId` from a folder in a sibling project within the *same*
  organization must still be rejected. This is a narrower check than the
  cross-org checks in 3a/3c/4 (same org, wrong project), so don't reuse
  those methods by mistake.
- No frontend this sub-feature - resist the urge to add one "while you're
  in there." It lands with 5b.

## Build notes (from implementation)

This feature built cleanly against the spec with no surprises. Both
distinguishing checks called out in the spec were proven with real
fixtures, not just implemented:

- A folder was created in a sibling project (`WEBAPP`) within the same
  org, then used as `parentId` in a create request against `TMP` -
  correctly rejected with 404 "Parent folder not found", confirming the
  `findByIdAndProjectId` scoping (not a bare org-level check) actually
  does the narrower job it was designed for.
- A project inserted directly under a different organization was also
  rejected with 404, consistent with every prior feature's isolation
  proof.
- Both sets of fixtures (the sibling-project folder and the cross-org
  organization/user/project) were deleted after their proofs.
