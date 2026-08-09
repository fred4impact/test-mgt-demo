# Feature: Organization & user model

**From build-plan:** feature 1b
**Status:** complete

## Goal

Stand up the tenant foundation: `Organization` and `User` as real, persisted
entities, with REST endpoints to create and read organizations, and a minimal
frontend form to create the first one. Everything after this feature (teams,
projects, everything tenant-owned) hangs off `organization_id` - this is where
that column starts existing for real.

## In scope

- `Organization` JPA entity + Flyway migration (`V2__organizations.sql`) +
  `OrganizationRepository`
- `User` JPA entity + Flyway migration (`V3__users.sql`, FK to
  `organizations`) + `UserRepository` - **schema only, no REST endpoint** (see
  Out of scope)
- REST endpoints for Organization: create, get by id, list
- Bean Validation on the create request (name and slug required)
- A basic global exception handler (`@ControllerAdvice`) producing the
  `coding-standards.md` error shape (`timestamp`, `status`, `code`, `message`,
  `path`, `errors[]`) for validation failures, not-found, and duplicate slug -
  this is the app's first error handler, later features extend it rather than
  reinventing it
- Minimal frontend page/form (name + slug) that calls the create endpoint and
  shows the result

## Out of scope

- **User REST endpoints (create/get)** - a real `User` row needs an
  authenticated Keycloak identity (`externalAuthId`); that only exists once
  1c wires login. 1b only gets the entity, migration, and repository so 1c's
  login-triggered provisioning has a table to write to.
- Multi-tenant query scoping enforcement - `coding-standards.md`'s rule
  ("every query scoped by the authenticated user's `organization_id` from the
  JWT") can't be enforced yet since there's no JWT/Spring Security until 1c.
  1b's endpoints are intentionally open/unauthenticated for now.
- Organization update/delete, and any org listing UI polish beyond "show what
  you created"
- RBAC, roles, permissions (feature 2)
- Auto-generating a slug from the name - the form asks for both explicitly,
  no slugify logic
- Keycloak wiring (feature 1c)

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Organization + User entities, migrations, repositories** -
  `Organization` entity (`name`, `slug` unique, `description`, `status`,
  default `"ACTIVE"`) and `User` entity (`organizationId` FK, `externalAuthId`
  nullable+unique, `email`, `firstName`, `lastName`, `status`) under
  `organization/` and `user/` module packages per `coding-standards.md`.
  `V2__organizations.sql`, `V3__users.sql`. *Done when:* `./mvnw
  spring-boot:run` starts cleanly (entity mappings match the migrated schema
  under `ddl-auto: validate` - a mismatch fails startup loudly), and
  `flyway_schema_history` has 3 rows (V1, V2, V3).
- [x] **Step 2 - Organization REST endpoints + validation + error handling** -
  `POST /api/v1/organizations` (name, slug), `GET
  /api/v1/organizations/{id}`, `GET /api/v1/organizations`; `OrganizationDto`
  (id, name, slug, status, createdAt); `@ControllerAdvice` mapping validation
  failures to 400, missing id to 404, duplicate slug to 409 - all in the
  standard error shape. *Done when:* `curl -X POST` with valid body returns
  201 + the DTO; `curl GET` by the returned id returns the same org; `curl
  GET` the list includes it; `curl -X POST` with no `name` returns 400 with a
  populated `errors[]`; posting the same slug twice returns 409.
- [x] **Step 3 - Minimal frontend: create an organization** - a page with a
  form (name, slug) that POSTs to the backend and displays the created
  organization (id, name, slug). *Done when:* filling and submitting the form
  in the browser creates a real organization, confirmed by re-fetching `GET
  /api/v1/organizations` and seeing it there.

## Files / areas

- `backend/src/main/java/com/testmgmt/platform/organization/` - entity, repository, controller, service, dto, mapper, validation
- `backend/src/main/java/com/testmgmt/platform/user/` - entity, repository only
- `backend/src/main/java/com/testmgmt/platform/common/` - shared `@ControllerAdvice` / error response DTO (first use, later features reuse it)
- `backend/src/main/resources/db/migration/V2__organizations.sql`, `V3__users.sql`
- `frontend/services/organizations.ts`, `frontend/actions/organizations.ts`, `frontend/app/organizations/new/` - the create-organization page

## Data / contracts

**Load-bearing:**
- `OrganizationDto { id: UUID, name: string, slug: string, status: string, createdAt: string }` - later features (teams, projects) reference `organizationId`; this response shape is the pattern subsequent DTOs follow.
- Error response shape (`timestamp, status, code, message, path, errors[]`) - the one `@ControllerAdvice` all later features extend, not reinvent.
- `users` table schema (`organization_id`, `external_auth_id`, `email`, `first_name`, `last_name`, `status`) - feature 1c's login provisioning writes here directly; don't change these column names without updating that plan.
- API base path convention: `/api/v1/...` on each controller (no global context-path configured) - keep new controllers consistent with this.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
verification rides on `curl` output (Step 2) and browser evidence (Step 3),
not an automated suite. If you want real coverage on the validation/mapping
logic this feature introduces, run `/tests` first to add a runner - out of
scope for this feature itself.

## Notes for the AI

- `ddl-auto: validate` means entity field mappings must exactly match the
  Flyway migration's column names/types or the app fails to start - this is
  intentional (catches drift), not a bug to work around.
- Don't add a User creation endpoint no matter how tempting "just for
  completeness" - it's explicitly deferred to 1c for the reason stated above.
- Keep the `@ControllerAdvice` in `common/` minimal (three cases: validation,
  not-found, conflict) - don't pre-build handling for error types no endpoint
  can produce yet.
- Organization `status` defaults to `"ACTIVE"` as a plain string for now; the
  spec doesn't define a formal enum of allowed values, so don't invent one -
  a later feature can formalize it if needed.

## Build notes (from implementation)

- **Stale process gotcha (Step 1):** a leftover backend process from an
  earlier manual demo was still holding port 8080 with old code (no
  Organization/User entities). It answered the health check and made the
  first verification attempt look successful when it wasn't actually testing
  the new code. Caught by checking which PID was really bound to the port and
  re-running clean. Worth remembering: a passing health check alone doesn't
  prove *which* process answered it.
- **Ephemeral Playwright (Step 3):** the frontend form's done-when is
  behavioral (fill, submit, observe). No browser automation tool was
  installed in the project, and `coding-standards.md` says not to add
  Playwright silently for an unrelated feature. Used it ephemerally instead -
  installed in the session scratchpad only, never touching
  `frontend/package.json` or its lockfile - to get real browser proof
  (screenshot + DOM assertion) without adding a project dependency this
  feature didn't ask for.
- Server Actions (not client-side `fetch`) were used for the create-org form
  specifically to avoid a CORS setup that a client-component approach would
  have needed - matches `coding-standards.md`'s existing guidance to prefer
  Server Actions for form submissions.
