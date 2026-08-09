# Coding Standards

> Your conventions. Tuned by `/onboard` to the stack in `blueprint/project-plan.md`:
> a **separate** Next.js frontend and Spring Boot backend (not a single
> Next.js app with Prisma), per the planned repo layout (`frontend/`,
> `backend/`). Neither app is scaffolded yet, so file-path and command
> details below are the target shape, not verified conventions - revisit
> once both exist.

## Frontend (`frontend/` - Next.js + TypeScript)

### TypeScript

- Strict mode enabled
- No `any` types - use proper typing or `unknown`
- Define interfaces for all props, API responses, and data models

### React

- Functional components only (no class components)
- Use hooks for state and side effects
- Keep components focused - one job per component
- Extract reusable logic into custom hooks

### Next.js

- Server components by default; `'use client'` only when needed (interactivity,
  hooks, browser APIs)
- **Business logic lives in the Spring Boot backend, not here.** The frontend
  calls the backend's REST API (`/api/v1/...`); it does not own the data model
  or write directly to the database.
- Next.js API routes are for frontend-only concerns: proxying/session glue for
  Keycloak/OIDC, webhooks specific to the frontend, or file-upload proxying -
  not general CRUD, which belongs to the backend.
- Dynamic routes for item/collection pages (`/projects/[projectId]/test-cases/[id]`)

### File Organization

- Components: `frontend/components/[feature]/ComponentName.tsx`
- Pages: `frontend/app/[route]/page.tsx`
- Hooks: `frontend/hooks/[useHookName].ts`
- API client calls: `frontend/services/[feature].ts`
- Types: `frontend/types/[feature].ts`
- Lib/Utils: `frontend/lib/[utility].ts`

(Matches `frontend/{app,components,features,hooks,lib,services,types,tests}/`
from the planned repo layout.)

### Styling

- Tailwind CSS for all styling
- Tailwind v4: CSS-first config (`@theme` in `globals.css`), no `tailwind.config.js`
- Use shadcn/ui components where applicable
- No inline styles
- > TODO - light/dark mode direction not specified in `project-plan.md` §7 (UI/UX);
  don't assume one until that's filled in

### Data Fetching (frontend)

- Fetch from the backend REST API via `frontend/services/`, typed against the
  contracts in `blueprint/context/project-overview.md`
- Validate form inputs with Zod (React Hook Form + Zod)
- Never trust a client-supplied `organizationId` or `projectId` for anything
  security-relevant - the backend derives tenant context from the
  authenticated user's token, the frontend just passes it along

## Backend (`backend/` - Spring Boot + Java 21, Maven)

- Package-by-feature module structure under
  `backend/src/main/java/.../<module>/`, e.g. `testcase/`, `execution/`,
  `defect/` - each with `controller/ service/ repository/ entity/ dto/
  mapper/ validation/`. Avoid a giant shared service layer.
- Spring Data JPA / Hibernate for persistence
- Bean Validation (`@Valid`) on all request DTOs
- REST controllers return DTOs, never JPA entities directly

### Database

- PostgreSQL is the system of record
- **Flyway** for all schema changes, version-controlled migrations only
  (`backend/db/migration/V1__..., V2__...`) - never hand-edit the production
  schema, never rely on Hibernate auto-DDL outside local dev
- Redis is a cache/session/rate-limit layer only, never the system of record

### Multi-tenancy (load-bearing - every module must follow this)

- Every tenant-owned table carries `organization_id`
- Every query must be scoped by the authenticated user's `organization_id`,
  derived server-side from the validated JWT - never from a client-supplied
  field
- This is the single most important invariant in the system; a missed scope
  check is a cross-tenant data leak

### Authentication & Authorization

- Keycloak + OpenID Connect issues the JWT; Spring Security validates it on
  every request
- RBAC: permissions like `TEST_CASE_CREATE`, `DEFECT_UPDATE`, `PROJECT_ADMIN`
  attached to roles, roles attached to users per organization/project (see
  `project-overview.md` once generated for the concrete model)
- No Clerk, no NextAuth-owned authorization - the frontend only relays the
  Keycloak-issued token

### Error Handling

- Consistent JSON error shape: `timestamp`, `status`, `code`, `message`,
  `path`, `errors[]` (field-level validation errors)
- Backend: `@ControllerAdvice` exception handling, never leak stack traces to
  the client
- Frontend: surface backend error `message`/`errors[]` via toast, don't
  swallow them

## Testing

The blueprint installs no test runner; testing is opt-in per app, because the
overlay can't know your stack. Adding it is an explicit setup task, either as a
build-plan item or via `/tests` (frontend) - the backend's JUnit/Testcontainers
setup is part of scaffolding the Spring Boot app itself.

When `AGENTS.md` declares a `Verify` command, treat it as the umbrella
automated gate. It combines only the checks this project actually has, in this
order when available: typecheck, tests, then build. The command does not
enable an absent test runner or replace focused evidence. `/ci` owns Verify
and CI setup. `/tests` adds the real test command to Verify when it already
exists, but never creates CI only because testing was configured.

**The opt-in switch is one signal: a `test` command in the Commands section of
`AGENTS.md`.** Declare one and **tests become a gate for logic-bearing steps**,
not an optional extra; leave it out and the loop verifies logic with the
evidence it already uses (run it, a screenshot, the build). Adding the runner
is itself a deliberate step, never a silent mid-step install.

- **What to test (the scope rule):** pure logic where a wrong answer is
  possible - parsers, formatters, validators, id/slug builders, mappers,
  service-layer business rules (backend), request/response DTO mapping. These
  have assertable inputs and outputs and real edge cases (empty, missing,
  malformed).
- **What not to test:** UI components and integration-level surfaces (render
  or export routes, anything driving a real browser or external service).
  Verify those with a screenshot and the build, not brittle unit tests.
- **The gate (when a runner is configured):** a build step that adds in-scope
  logic must ship a passing test in the same reviewable diff. The project's
  test command must be green before the step is approved, before any
  checkpoint commit, and before `/complete` merges. UI and integration-only
  steps are exempt and ride on screenshot plus build evidence.
- **When it's named:** the `/feature` spec's Testing section predicts the
  coverage, `/implement` writes the test with the step, and if a step surfaces
  logic the spec didn't foresee, add a focused test then.
- An empty suite should fail, not pass, so "no tests ran" never looks like
  "passed".
- Test files live next to source files (e.g. `feature.test.ts`,
  `TestCaseServiceTest.java`).
- Run them via the project's test command (see Commands in `AGENTS.md`), not a
  hardcoded tool name.

Stack binding: frontend uses Vitest + React Testing Library (once configured);
backend uses JUnit 5 + Mockito + Spring Boot Test, with Testcontainers for
integration tests against real Postgres/Redis/Kafka rather than mocks alone.

## Browser Verification

For UI and integration behavior, prefer real browser evidence over reading the
code and assuming it works.

- If Playwright is already installed, or the Commands section of `AGENTS.md`
  declares a Playwright script, use Playwright for browser checks, screenshots,
  console-error checks, and user-flow verification.
- If Playwright is not installed, do not add it silently in the middle of an
  unrelated feature. Use the available dev server, browser screenshots, build
  output, API output, or manual verification evidence instead.
- Add Playwright only when the user asks for it, or when the current spec is
  explicitly about setting up browser automation.
- Browser evidence is especially important for flows that click, type, submit,
  navigate, download files, render complex layouts, or depend on client-side
  state.

## Code Quality

- No commented-out code unless specified
- No unused imports or variables
- Keep functions under 50 lines when possible

## Comments

Write code that explains itself; comment only what the code cannot say.
Over-commenting is a common AI tell, so resist it.

- Comment the **why**, not the **what**. Delete any comment that restates the code.
- No banner/header blocks, section dividers, or step-by-step narration of obvious
  code. A file does not need a comment announcing each region.
- A comment earns its place only when it captures something the code can't: a
  non-obvious decision, a gotcha or workaround, why a value is what it is, or a
  link to a spec or issue.
- Prefer self-documenting names and small functions over explanatory comments.
- Keep doc comments minimal: a one-line purpose on an exported type or function is
  plenty; don't write JSDoc/Javadoc that just repeats the signature.
- When in doubt, leave the comment out.

## Writing

- No em dashes (U+2014) in generated content: docs, comments, commit messages,
  READMEs, specs. They read as AI-generated.
- Use a hyphen for `term - description` separators; rephrase prose with commas,
  parentheses, or a colon. Avoid en dashes and the ellipsis character too.
