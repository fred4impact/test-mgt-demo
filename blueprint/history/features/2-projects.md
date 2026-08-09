# Feature: Projects

**From build-plan:** feature 2
**Status:** complete

## Goal

Let an organization create and manage projects - the container everything
else (requirements, test cases, releases, cycles...) will eventually live
under. This is also the **first feature that enforces real tenant
isolation**: 1b's Organization endpoints were deliberately left open since
no JWT existed yet; that infra now exists (1c), so Projects gets built
correctly from the start - protected, scoped by the authenticated user's
organization.

## In scope

- `Project` JPA entity + Flyway migration (`V4__projects.sql`):
  `organizationId` FK, `key`, `name`, `status`, `ownerId` FK to users -
  unique on `(organizationId, key)`
- `ProjectRepository`
- REST endpoints for Project: create, get by id, list - **all protected**
  (require a valid JWT) and **scoped to the current user's organization**,
  resolved the same way `/api/v1/me` does (JWT `sub` -> `User` ->
  `organizationId`)
- `ownerId` is never client-supplied - it's always the authenticated user
  creating the project
- Refactor `UserService` to expose the lookup-or-provision logic as a
  reusable `resolveOrProvisionUser(Jwt): User`, so both `MeController` and
  the new `ProjectController` share it instead of duplicating
- Minimal protected frontend page to create a project

## Out of scope

- **`ProjectMember` (project membership + role assignment).** It needs
  `Role`, which doesn't exist until feature 3 (Teams & roles - this is the
  reordering decision made before writing this spec). Feature 3 builds
  `Team`, `Role`, `Permission`, `RolePermission`, *and* `ProjectMember`
  together, since role assignment only makes sense once roles exist.
- Project update/delete
- Retroactively protecting 1b's Organization endpoints - still not this
  feature's call to make
- Project settings, custom fields, archiving

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Project entity, migration, repository** - `Project`
  (`organizationId`, `key`, `name`, `status` default `"ACTIVE"`, `ownerId`),
  unique `(organization_id, key)`. `V4__projects.sql`. *Done when:* app
  starts cleanly under `ddl-auto: validate`, `flyway_schema_history` has 4
  rows (V1-V4).
- [x] **Step 2 - Protected, org-scoped Project REST endpoints** -
  `POST /api/v1/projects`, `GET /api/v1/projects/{id}`, `GET
  /api/v1/projects`; all require a valid JWT; every query filtered by the
  resolved user's `organizationId`; `ownerId` set server-side from the JWT,
  never from the request body; Bean Validation on `key`/`name` (required,
  matching 1b's `CreateOrganizationRequest` pattern); duplicate `(org, key)`
  -> 409 via the existing `GlobalExceptionHandler`. *Done when:* with the
  seeded test user's token (Acme Corp), `POST` with a valid body creates a
  project (201); `GET` list includes it; `POST` missing `key` or `name` ->
  400 with populated `errors[]`; posting the same key twice -> 409; a
  project **inserted directly into Postgres under a different organization**
  (Widgets Inc) does **not** appear in the list and returns 404 on direct
  `GET` by id - proving tenant isolation without needing a second real
  Keycloak identity; no token -> 401.
- [x] **Step 3 - Minimal protected frontend: create a project** - a
  `/projects/new` page, redirects unauthenticated visitors to sign-in (same
  pattern as `/profile`), form (key, name) POSTs to the backend with the
  session's access token, shows the result. *Done when:* browser-driven -
  unauthenticated visit redirects to sign-in; signed in as the test user,
  creating a project shows it in a re-fetched list.

## Files / areas

- `backend/.../project/entity/`, `project/repository/`, `project/dto/`, `project/mapper/`, `project/service/`, `project/controller/`
- `backend/.../user/service/UserService.java` - refactored to expose `resolveOrProvisionUser(Jwt): User`
- `backend/.../common/security/SecurityConfig.java` - not modified; `/api/v1/projects/**` falls under the existing `anyRequest().authenticated()` default, confirmed rather than changed
- `backend/src/main/resources/db/migration/V4__projects.sql`
- `frontend/services/projects.ts`, `frontend/actions/projects.ts`, `frontend/app/projects/new/`
- `frontend/app/page.tsx` - added a "Projects" nav link

## Data / contracts

**Load-bearing:**
- `ProjectDto { id, organizationId, key, name, status, ownerId, createdAt }`
- **The tenant-scoping pattern established here** - resolve `User` from the
  JWT, filter every query by `user.getOrganizationId()` - is THE pattern
  every future org-scoped resource (requirements, test cases, releases...)
  must follow. This is the first feature to actually prove
  `coding-standards.md`'s "most important invariant" works, not just state
  it.
- `resolveOrProvisionUser(Jwt): User` on `UserService` - reused by every
  future protected endpoint that needs "who is this and what org are they
  in," not just Projects.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Step 2, including the direct-SQL
tenant-isolation check) and browser evidence (Step 3), matching the pattern
from every prior feature.

## Notes for the AI

- Don't invent a way to test cross-org isolation with a second real Keycloak
  login - the auto-provisioning rule from 1c assigns every new login to the
  first organization regardless, so a second Keycloak user wouldn't land in
  a different org anyway. Inserting a project directly into Postgres under
  a different org and proving the API excludes it is the correct, sufficient
  proof for this feature.
- Reuse `GlobalExceptionHandler` and the existing error shape - don't build
  a second one.
- `ownerId` is server-derived, never trust a client-supplied value for it.

## Build notes (from implementation)

- **Reorder decision, made before writing the spec (not during it).** The
  build-plan originally had "Teams & roles" (RBAC assignment) before
  "Projects," but `ProjectMember.roleId` needs `Role`, which only exists in
  the Teams & roles feature. Swapped the order: Projects (this feature)
  needs no `Role`, so it goes first; `ProjectMember` moves into the Teams &
  roles feature alongside `Role`, where role assignment can actually mean
  something. `build-plan.md` items 2 and 3 were swapped accordingly.
- **Tenant isolation was proven, not just implemented.** A project was
  inserted directly into Postgres under a different organization (Widgets
  Inc) via `psql`, then confirmed the Acme-Corp-scoped API excluded it from
  both the list and direct-by-id lookup (404, not 403 - deliberately avoids
  confirming the resource exists to a caller outside its tenant). Cleaned up
  the test fixture afterward.
- The `ecomms (DEL-APP)` project visible in the Step 3 screenshot was
  created by the user directly against the running app between turns, not
  part of this feature's build - confirmed with them before archiving.
