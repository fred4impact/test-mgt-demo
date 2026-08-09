# Feature: Roles & permissions catalog

**From build-plan:** feature 3b
**Status:** complete

## Goal

Give an organization a set of named roles it can attach permissions to, plus
a global permission catalog to attach from. This lays the ground for feature
3c, which will assign a `(user, project, role)` triple via `ProjectMember` -
`Role` (and its shape) is a load-bearing contract for that feature, so it
needs to be locked correctly here. Enforcing these permissions on actual
endpoints is explicitly out of scope for the whole "Teams & roles" build-plan
item (see its build-plan line) - this feature only builds the catalog and
the assignment surface, not the enforcement.

## In scope

- `Permission` JPA entity + Flyway migration that both creates the table and
  seeds a starter catalog (`V7__permissions.sql`) - global, not org-scoped,
  per the data model in `project-overview.md`
- `Role` JPA entity + migration (`V8__roles.sql`): `organizationId`, `name`,
  `systemRole` (bool, defaults false)
- `RolePermission` JPA entity + migration (`V9__role_permissions.sql`): join
  of `roleId` + `permissionId`, unique on the pair
- `PermissionRepository`, `RoleRepository`, `RolePermissionRepository`
- `GET /api/v1/permissions` - list the catalog; protected, no org filter
  (the catalog is global)
- REST endpoints for Role: create, get by id, list - protected + org-scoped,
  same pattern as `TeamController`
- REST endpoints for RolePermission: attach a permission to a role, detach,
  list a role's permissions - protected; attaching validates the role
  belongs to the caller's org (permissions themselves have no org to check
  against, since the catalog is global)
- Minimal protected frontend page to create a role (permission
  attach/detach stays API-only, no dedicated UI - matches how 3a kept team
  membership API-only)

## Out of scope

- **Enforcing any permission on any endpoint.** This feature builds the
  catalog and the role/permission data; nothing reads it to gate access yet.
  That is explicitly a separate later concern per the build-plan line for 3c.
- Role update/delete - same precedent as `Team` in 3a (no update/delete
  there either)
- Auto-provisioning default/system roles when an organization is created.
  `systemRole` is a settable flag on manual creation, not a trigger - wiring
  that up is a bigger, separate concern (what the default set even is) that
  the plan doesn't specify
- Permission codes for modules that don't exist yet (test cases, defects,
  executions, etc.). The seeded catalog covers only what's actually built so
  far (organizations, projects, teams, roles); later features add their own
  permission codes in their own migrations when those modules land, not
  invented speculatively now
- A dedicated frontend UI for attaching/detaching permissions - API-only,
  testable via `curl`, matching 3a's membership-management precedent

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Permission entity, seeded migration, repository, list
  endpoint** - `Permission` (`code` unique, `description`). `V7__permissions.sql`
  creates the table and seeds it with: `ORG_ADMIN`, `PROJECT_ADMIN`,
  `PROJECT_VIEW`, `TEAM_MANAGE`, `ROLE_MANAGE`. `GET /api/v1/permissions`
  returns the full catalog for any authenticated user (no org scoping - the
  catalog is global). *Done when:* app starts cleanly under `ddl-auto:
  validate`, `flyway_schema_history` has 7 rows (V1-V7); `GET
  /api/v1/permissions` with a valid token returns the 5 seeded rows; no
  token -> 401.
- [x] **Step 2 - Role entity, migration, repository, org-scoped REST
  endpoints** - `Role` (`organizationId`, `name`, `systemRole` default
  `false`). `V8__roles.sql`. `POST /api/v1/roles`, `GET /api/v1/roles/{id}`,
  `GET /api/v1/roles`; same auth/scoping pattern as `TeamController` (JWT
  required, filtered by the resolved user's `organizationId`); Bean
  Validation on `name`. *Done when:* `flyway_schema_history` has 8 rows;
  valid `POST` -> 201 with `systemRole: false` when omitted; `GET` list
  includes it; `POST` missing `name` -> 400; a role from a different org
  (inserted directly via SQL, same technique as 3a's isolation proof) -> 404
  on direct `GET`, excluded from list; no token -> 401.
- [x] **Step 3 - RolePermission entity, migration, attach/detach/list
  endpoints** - `RolePermission` (`roleId`, `permissionId`, unique on the
  pair). `V9__role_permissions.sql`. `POST
  /api/v1/roles/{roleId}/permissions` (`permissionId`, Bean Validation
  `@NotNull`), `DELETE /api/v1/roles/{roleId}/permissions/{permissionId}`,
  `GET /api/v1/roles/{roleId}/permissions`. The role must belong to the
  caller's org (404 if not, same "don't confirm cross-tenant existence"
  philosophy as 3a) - permission ids are looked up directly since the
  catalog is global, a nonexistent `permissionId` -> 404. Attaching an
  already-attached permission -> 409. Detaching a permission that isn't
  attached -> 404. *Done when:* `flyway_schema_history` has 9 rows; attach a
  seeded permission to an owned role -> 200/201, appears in the role's
  permission list; attach the same permission again -> 409; attach to a
  role inserted under a different org -> 404; attach a random UUID as
  `permissionId` -> 404; detach an unattached permission -> 404; `POST`
  with a missing `permissionId` -> 400.
- [x] **Step 4 - Minimal protected frontend: create a role** - a
  `/roles/new` page, same redirect-if-unauthenticated pattern as
  `/teams/new`, form (name, systemRole checkbox) POSTs to the backend,
  shows the result and existing roles. *Done when:* browser-driven -
  unauthenticated visit redirects to sign-in; signed in as the test user,
  creating a role shows it in a re-fetched list.

## Files / areas

- `backend/.../permission/entity/Permission.java`, `permission/repository/`, `permission/dto/`, `permission/controller/`
- `backend/.../role/entity/` (`Role`, `RolePermission`), `role/repository/`, `role/dto/`, `role/mapper/`, `role/service/`, `role/controller/`
- `backend/src/main/resources/db/migration/V7__permissions.sql`, `V8__roles.sql`, `V9__role_permissions.sql`
- `frontend/services/roles.ts`, `frontend/actions/roles.ts`, `frontend/app/roles/new/`
- `frontend/app/page.tsx` - add a "Roles" nav link

## Data / contracts

**Load-bearing:**
- `PermissionDto { id, code, description }`
- `RoleDto { id, organizationId, name, systemRole, createdAt }` - feature
  3c's `ProjectMember.roleId` will reference `Role.id` directly, so this
  shape (and in particular that `id` is the stable UUID PK, not `code`)
  must not change once 3c is built against it.
- Cross-entity org validation pattern for attach: only the role's org is
  checked against the caller's org, not the permission's (permissions have
  no `organizationId` - the catalog is global by design, unlike 3a's
  team/user check which validated two org-scoped entities against each
  other).

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 1-3, including a cross-org role
attempt and a cross-org attach attempt) and browser evidence (Step 4),
matching every prior feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`, and
  the `TeamController`/`TeamService`/`TeamMemberService` pattern directly -
  `Role` mirrors `Team`, and `RolePermission` mirrors `TeamMember` except the
  "other side" of the join (`Permission`) is global instead of org-scoped, so
  it only needs an existence check, not an org-match check.
- Don't invent permission codes beyond the five seeded in Step 1. If a
  later feature needs a new code, it adds its own migration for it.
- 404 (not 400) for a cross-org role or an unknown `permissionId` in Step 3,
  consistent with 3a's isolation philosophy.

## Build notes (from implementation)

- **Real bug found and fixed in Step 2**: `CreateRoleRequest.systemRole` was
  originally a primitive `boolean`. Jackson rejected any request that
  omitted the field (`Cannot map null into type boolean`), even though the
  spec's own example (`POST` with just `name`) relies on the default.
  Fixed by switching to the `Boolean` wrapper type with a
  `systemRoleOrDefault()` helper that treats a missing/null value as
  `false`. This is the kind of primitive-vs-wrapper Jackson gotcha that
  only surfaces by actually sending the request, not by reading the code.
- **`/check` re-verified every done-when fresh** against the already-running
  app (backend at schema v9, a clean frontend dev-server restart) rather
  than relying on evidence captured during `/implement`: both cross-org
  proofs (a role under a different org, and attaching to that role) were
  re-run with new fixture rows and cleaned up after; the browser flow was
  re-run with console-error and failed-request monitoring added, both
  clean.
