# Feature: Project membership & role assignment

**From build-plan:** feature 3c
**Status:** complete

## Goal

Let an organization assign a user to a project with a specific role via
`ProjectMember`, completing the `(user, project, role)` triple the data
model calls for. `Project` already exists (feature 2) and `Role` already
exists (feature 3b); this feature only adds the join between them. Once
this ships, build-plan item 3 ("Teams & roles") is fully complete.
Enforcing these permissions on any endpoint is explicitly a separate later
concern per the build-plan line for this item - this feature only builds
the assignment, not the enforcement.

## In scope

- `ProjectMember` JPA entity + Flyway migration (`V10__project_members.sql`):
  `projectId`, `userId`, `roleId`, unique on `(project_id, user_id)`
- `ProjectMemberRepository`
- REST endpoints, nested under the existing `ProjectController`: add a
  member with a role, remove a member, list a project's members -
  protected; adding validates both that the target user and the target
  role belong to the caller's organization (two separate cross-org checks,
  since `Project`, `User`, and `Role` are each independently org-scoped)

## Out of scope

- Changing a member's role on an existing membership. `ProjectMember` has
  no update endpoint, matching the no-update/no-delete-of-the-parent
  precedent from 3a (`Team`) and 3b (`Role`) - to change a role, remove and
  re-add the membership
- **Enforcing any permission on any endpoint.** This feature only builds
  the assignment data; nothing reads `ProjectMember.roleId` to gate access
  yet. Explicitly deferred per the build-plan line for this item
- A user holding more than one role on the same project simultaneously -
  the unique constraint on `(project_id, user_id)` means one membership row
  per pair, which is what the data model's join describes
- Any frontend UI. Unlike 3a and 3b, this feature adds no new top-level
  entity with its own "create" page - `Project` already has one from
  feature 2. Membership is API-only, same precedent as 3a's team
  membership and 3b's permission attachment

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - ProjectMember entity, migration, repository** -
  `ProjectMember` (`projectId`, `userId`, `roleId`, unique
  `(project_id, user_id)`). `V10__project_members.sql`. *Done when:* app
  starts cleanly under `ddl-auto: validate`, `flyway_schema_history` has 10
  rows (V1-V10).
- [x] **Step 2 - Add/remove/list member endpoints with dual cross-org
  validation** - `POST /api/v1/projects/{projectId}/members` (`userId`,
  `roleId`, both Bean Validation `@NotNull`), `DELETE
  /api/v1/projects/{projectId}/members/{userId}`, `GET
  /api/v1/projects/{projectId}/members`. The project must belong to the
  caller's org (404 if not, existing `ProjectController` pattern). Adding
  a member checks, independently: the target user belongs to the caller's
  org (404 if not, same technique as 3a's `TeamMemberService`) and the
  target role belongs to the caller's org (404 if not, same technique as
  3b's `RoleService`). Adding an existing `(project, user)` pair -> 409.
  Removing a non-member -> 404. Response is denormalized with user and
  role details, not bare ids (matching `TeamMemberDto`'s precedent).
  *Done when:* `flyway_schema_history` is unchanged at 10 rows (this step
  adds no migration); add the seeded test user with a real role to a project -> 200/201, appears in the member
  list with user and role details; add the same pair again -> 409; add a
  user inserted directly under a different org -> 404; add a role
  inserted directly under a different org -> 404; remove a non-member ->
  404; `POST` with a missing `userId` or `roleId` -> 400; no token -> 401.

## Files / areas

- `backend/.../project/entity/ProjectMember.java`
- `backend/.../project/repository/ProjectMemberRepository.java`
- `backend/.../project/dto/AddProjectMemberRequest.java`, `ProjectMemberDto.java`
- `backend/.../project/service/ProjectMemberService.java`
- `backend/.../project/controller/ProjectController.java` - add the three nested endpoints
- `backend/src/main/resources/db/migration/V10__project_members.sql`

## Data / contracts

**Load-bearing:**
- `ProjectMemberDto { userId, email, firstName, lastName, roleId, roleName, joinedAt }` -
  denormalized the same way `TeamMemberDto` is, so a "list members" response
  is immediately useful without a second lookup.
- Dual cross-org validation pattern for a three-way join (`Project` owns
  the relationship, `User` and `Role` are each checked independently
  against the caller's org derived from the project). This is the first
  feature with two independent cross-entity checks in a single write path;
  later features that join more org-scoped entities together follow the
  same shape.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output, including two separate cross-org
attempts (user and role), matching every prior feature in this build-plan
item.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`,
  `ProjectRepository.findByIdAndOrganizationId`, and
  `RoleRepository.findByIdAndOrganizationId` directly - every lookup this
  feature needs already exists from features 2, 3a, and 3b.
- Don't add a `RoleRepository`/`UserRepository` org check only for one side
  and skip the other - both `userId` and `roleId` need their own
  independent org validation, since either one, and not the other, could
  belong to a different org.
- 404 (not 400) for either cross-org case, consistent with 3a and 3b's
  isolation philosophy.
- No new frontend work - do not add a `/projects/[id]/members` page; that's
  explicitly out of scope here.

## Build notes (from implementation)

- **Spec bug found and fixed mid-implementation**: Step 2's original
  done-when claimed `flyway_schema_history` would reach 11 rows after this
  step. That's wrong - Step 2 adds no migration (the `project_members`
  table came from `V10` in Step 1), so the count correctly stays at 10.
  Caught by actually checking the row count against the real database
  rather than trusting the written spec, and fixed in the spec text before
  marking the step done.
- **The two cross-org checks were proven independently, not just
  together.** The cross-org role check was proven using the same user who
  was already a project member (added earlier in the same test run) with a
  role from a different org - this only works because the service checks
  role membership *before* the duplicate-pair check, so the test actually
  exercised the role branch rather than silently passing on the earlier
  409 path. Both fixture rows (a cross-org user and a cross-org role) were
  deleted after the proof.
