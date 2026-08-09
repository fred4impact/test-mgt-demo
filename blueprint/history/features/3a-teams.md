# Feature: Teams

**From build-plan:** feature 3a
**Status:** complete

## Goal

Let an organization group users into teams. Follows the same protected,
org-scoped pattern feature 2 (Projects) established, with one new wrinkle:
adding a member means cross-checking that the target user belongs to the
*same* organization as the team - the first time this project validates a
relationship between two different org-scoped entities, not just one
resource against the caller's own org.

## In scope

- `Team` JPA entity + Flyway migration (`V5__teams.sql`): `organizationId`
  FK, `name`, `description`
- `TeamMember` JPA entity + Flyway migration (`V6__team_members.sql`):
  `teamId` FK, `userId` FK, unique on `(teamId, userId)`
- `TeamRepository`, `TeamMemberRepository`
- REST endpoints for Team: create, get by id, list - protected + org-scoped,
  same pattern as `ProjectController`
- REST endpoints for TeamMember: add a member, remove a member, list
  members - protected; adding a member validates the target user is in the
  same organization as the team
- Minimal protected frontend page to create a team (membership management
  stays API-only for now, no dedicated UI - matches how 1b/2 kept their
  frontends minimal)

## Out of scope

- Any role on team membership - `TeamMember` is a plain join, no `roleId`.
  Role assignment happens via `ProjectMember` in 3c, not here.
- Team update/delete
- No uniqueness constraint on team name within an org - the data model
  doesn't specify one; not inventing one
- A dedicated frontend UI for managing team membership - API-only, testable
  via `curl`
- Auto-adding the team creator as a member - creation and membership are
  separate explicit actions

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Team + TeamMember entities, migrations, repositories** -
  `Team` (`organizationId`, `name`, `description`), `TeamMember`
  (`teamId`, `userId`, unique `(teamId, userId)`). `V5__teams.sql`,
  `V6__team_members.sql`. *Done when:* app starts cleanly under
  `ddl-auto: validate`, `flyway_schema_history` has 6 rows (V1-V6).
- [x] **Step 2 - Protected, org-scoped Team REST endpoints** - `POST
  /api/v1/teams`, `GET /api/v1/teams/{id}`, `GET /api/v1/teams`; same
  auth/scoping pattern as `ProjectController` (JWT required, filtered by
  the resolved user's `organizationId`); Bean Validation on `name`.
  *Done when:* valid `POST` -> 201; `GET` list includes it; `POST` missing
  `name` -> 400; a team from a different org (inserted directly via SQL,
  same technique as feature 2's isolation proof) -> 404 on direct `GET`,
  excluded from list; no token -> 401.
- [x] **Step 3 - TeamMember endpoints (add/remove/list)** - `POST
  /api/v1/teams/{teamId}/members` (`userId`, Bean Validation `@NotNull`),
  `DELETE /api/v1/teams/{teamId}/members/{userId}`, `GET
  /api/v1/teams/{teamId}/members`. Adding a member whose `organizationId`
  doesn't match the team's -> 404 (same "don't confirm cross-tenant
  existence" philosophy as feature 2's isolation proof, not a 400 - a user
  in another org is treated as not found, not as invalid input). Adding an
  existing member again -> 409. Removing a non-member -> 404. *Done when:*
  add the seeded test user to a team -> 200/201, appears in the member
  list; add the same user again -> 409; add a user inserted directly under
  a different org -> 404; remove a non-member -> 404; `POST` with a missing
  `userId` -> 400.
- [x] **Step 4 - Minimal protected frontend: create a team** - a
  `/teams/new` page, same redirect-if-unauthenticated pattern as
  `/projects/new`, form (name, description) POSTs to the backend, shows
  the result and existing teams. *Done when:* browser-driven -
  unauthenticated visit redirects to sign-in; signed in as the test user,
  creating a team shows it in a re-fetched list.

## Files / areas

- `backend/.../team/entity/` (`Team`, `TeamMember`), `team/repository/`, `team/dto/`, `team/mapper/`, `team/service/`, `team/controller/`
- `backend/src/main/resources/db/migration/V5__teams.sql`, `V6__team_members.sql`
- `frontend/services/teams.ts`, `frontend/actions/teams.ts`, `frontend/app/teams/new/`
- `frontend/app/page.tsx` - added a "Teams" nav link

## Data / contracts

**Load-bearing:**
- `TeamDto { id, organizationId, name, description, createdAt }`
- `TeamMemberDto { userId, email, firstName, lastName, joinedAt }` - denormalized with user details (not just a raw `userId`), matching how `MeDto` already denormalizes `organizationName` rather than exposing a bare id - a "list members" response is more useful with names attached.
- Cross-entity org validation pattern (team's org must match member's org, checked before insert) - the first instance of validating a relationship *between* two org-scoped entities, not just one resource against the caller's own org. Later features (e.g. adding a user to a project) will need the same check.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off -
verification rode on `curl` output (Steps 2-3, including a cross-org
membership attempt) and browser evidence (Step 4), matching every prior
feature.

## Notes for the AI

- Reuse `GlobalExceptionHandler`, `UserService.resolveOrProvisionUser`, and
  the `ProjectController`/`ProjectService` pattern directly - this feature
  is structurally almost identical to feature 2 plus one new join table.
- The cross-org membership check (Step 3) is the one genuinely new piece of
  logic here - it was tested, not just implemented (see Build notes).
- 404 (not 400) for cross-org membership attempts, consistent with feature
  2's isolation philosophy - don't confirm a user exists to a caller
  outside their tenant.

## Build notes (from implementation)

This feature built cleanly against the spec with no surprises - the pattern
established in feature 2 (Projects) transferred directly. Worth recording
what *was* deliberately proven rather than assumed:

- **Fresh-process discipline continued.** Every backend restart this
  session was checked against `ps -o etime` before trusting its output,
  following the false-positive from earlier in feature 2's implementation.
- **Cross-org membership validation was tested with real inserted data**,
  not just code review: a real user row was inserted directly into Postgres
  under Widgets Inc, and the add-member endpoint was confirmed to reject it
  with 404 - the same standard applied to feature 2's tenant-isolation
  proof, now applied to a relationship *between* two org-scoped entities
  rather than one resource against the caller's org.
- Test fixtures (the cross-org team and the cross-org user) were deleted
  after each proof, keeping the seed data clean for the next feature.
