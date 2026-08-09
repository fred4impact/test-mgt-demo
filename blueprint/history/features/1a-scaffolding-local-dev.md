# Feature: Project scaffolding & local dev environment

**From build-plan:** feature 1a
**Status:** complete

## Goal

Get both apps into existence and runnable locally, backed by real
infrastructure, so every feature after this one has something to build into.
Neither `frontend/` nor `backend/` exists yet - this is the ground floor.

## In scope

- Scaffold the Next.js frontend (`frontend/`): TypeScript, Tailwind, App Router
- Scaffold the Spring Boot backend (`backend/`): Java 21, Maven, Web +
  Actuator to start
- `docker-compose.yml` at the repo root: Postgres, Redis, Keycloak containers
  (containers only - no app wiring to Keycloak yet, that's 1c)
- Wire the backend to Postgres via Spring Data JPA + Flyway, with a trivial
  first migration proving the pipeline works
- Update `AGENTS.md` Commands with real, verified commands for both apps

## Out of scope

- Keycloak realm/client config and OIDC login flow (feature 1c)
- Organization/User entities, migrations, endpoints (feature 1b)
- Spring Security / JWT validation (feature 1c)
- Kafka, OpenSearch, MinIO/S3 - not needed by any feature yet; add when one
  requires it, per `project-overview.md`'s tech stack notes
- Redis actually being used for anything (cache/session) - container only,
  wire it up when a feature needs caching or sessions
- Unit test runner setup (`/tests`) - out of scope per `coding-standards.md`;
  this feature does not declare a `test` command in `AGENTS.md`, so the
  testing gate stays off
- CI/CD, deployment config (`/ci`, `/release` handle these later)

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Scaffold Next.js frontend** - `create-next-app` in `frontend/`
  with TypeScript, Tailwind, App Router, ESLint. *Done when:* `npm run dev`
  inside `frontend/` serves the default page at `http://localhost:3000`.
- [x] **Step 2 - Scaffold Spring Boot backend** - Maven project in `backend/`,
  Java 21, `spring-boot-starter-web` + `spring-boot-starter-actuator` only
  (no DB dependency yet). Root package `com.testmgmt.platform` (arbitrary,
  load-bearing for 1b's module packages - flag if you want a different name).
  *Done when:* `./mvnw spring-boot:run` inside `backend/` serves
  `GET /actuator/health` returning `{"status":"UP"}` at `http://localhost:8080`.
- [x] **Step 3 - Add docker-compose.yml (Postgres, Redis, Keycloak)** - root
  `docker-compose.yml` with named volumes for Postgres data persistence
  (`docker compose down` shouldn't wipe data). Keycloak container runs
  unconfigured - no realm/client setup yet. *Done when:* `docker compose up -d
  postgres redis keycloak` brings all three up, `docker compose ps` shows all
  healthy/running.
- [x] **Step 4 - Wire backend to Postgres (JPA + Flyway)** - add
  `spring-boot-starter-data-jpa`, PostgreSQL driver, and Flyway to the backend;
  `application.yml` datasource pointing at the Docker Postgres; one trivial
  first migration (`V1__init.sql`, effectively a no-op marker - real schema
  starts in 1b). *Done when:* with the Step 3 containers running,
  `./mvnw spring-boot:run` then `GET /actuator/health` shows the `db` component
  `UP`, and `flyway_schema_history` in Postgres has exactly one row for V1.
- [x] **Step 5 - Document real commands in AGENTS.md** - replace the
  target-shape/TODO commands from `/onboard` with the real, just-verified
  ones (frontend dev/build/lint, backend run/build, docker compose up/down).
  *Done when:* `AGENTS.md` Commands section matches what Steps 1-4 actually
  proved works, with no test command added (gate stays off).

## Files / areas

- `frontend/` - new, `create-next-app` output
- `backend/` - new, Maven project (`pom.xml`, `src/main/java/com/testmgmt/platform/...`, `src/main/resources/application.yml`, `src/main/resources/db/migration/V1__init.sql`)
- `docker-compose.yml` - new, repo root
- `AGENTS.md` - Commands section only

> Correction from the original spec: no separate `.env.example` was created.
> Local-dev credentials (Postgres, Keycloak admin) are inlined directly in
> `docker-compose.yml` and `backend/src/main/resources/application.yml` - this
> wasn't gated by any step's done-when, so it's noted here rather than treated
> as a deviation from a completed step.

## Data / contracts

None yet. `V1__init.sql` is intentionally a no-op/marker migration - it exists
to prove the Flyway pipeline runs end-to-end, not to define schema. Feature 1b
adds the first real tables (`organizations`, `users`) as `V2__...`, `V3__...`
in this same migration path.

**Load-bearing:** the backend root package `com.testmgmt.platform` chosen in
Step 2 - later features' modules (`organization/`, `user/`, etc., per
`coding-standards.md`) nest under it.

## Testing

No test command exists yet and this feature does not add one, so the testing
gate (per `coding-standards.md`) stays off - verification for every step rides
on command output (dev server starts, health check response, `docker compose
ps` status, Flyway migration row) rather than an automated suite. The default
Spring Initializr `contextLoads` smoke test is fine to leave in place if
generated, but nothing extra is required here.

## Notes for the AI

- Don't touch Keycloak configuration beyond making the container run - realm,
  client, and OIDC flow are feature 1c's job entirely.
- Don't add Kafka, OpenSearch, or MinIO to `docker-compose.yml` - not used by
  any feature yet.
- Keep `V1__init.sql` trivial/no-op; resist the urge to sketch real tables
  here, that's 1b's spec to own.
- Follow `coding-standards.md` file organization for both apps (even though
  the module subfolders like `organization/`, `testcase/` etc. don't exist
  until later features need them).

## Build notes (from implementation)

Two real bugs hit and fixed during Step 2/4, not assumed away:

1. Spring Initializr's metadata advertised boot version `4.1.0.RELEASE`, but
   that exact string doesn't resolve on Maven Central - the real artifact is
   `4.1.0` (Boot 4.x dropped the `.RELEASE` suffix convention from 2.x).
2. Bare `flyway-core` + `flyway-database-postgresql` didn't trigger Spring
   Boot 4.1's Flyway auto-configuration at all (zero Flyway log output, no
   `flyway_schema_history` table, despite `db: UP` on the health check).
   Root cause: Boot 4.1 introduced a new `spring-boot-starter-flyway` wrapper
   artifact that bare `flyway-core` doesn't satisfy. Confirmed by generating a
   throwaway Initializr POM with the `flyway` dependency ID and diffing the
   coordinates. Swapping to `spring-boot-starter-flyway` fixed it.

Worth remembering for later features: don't assume pre-4.0 Spring Boot
artifact names/versions still apply - check Initializr's actual output.
