# Feature: Keycloak login/logout & profile

**From build-plan:** feature 1c
**Status:** complete

## Goal

Wire real authentication end to end: a Keycloak realm/client, JWT validation
on the backend, an OIDC login flow on the frontend, and a protected profile
page that proves a logged-in user resolves to a real `User` row (per 1b's
deferred plan). This is the first feature where "who is making this request"
becomes a real, provable concept in the app.

## In scope

- Keycloak realm + client configuration, version-controlled as a realm-export
  JSON imported at container startup (not manual admin-console clicking) -
  includes one seeded local-dev test user for verification
- Backend: Spring Security resource server validating Keycloak-issued JWTs
- `GET /api/v1/me` - protected endpoint; on first authenticated request,
  auto-provisions a `User` row from the JWT (externalAuthId, email, name) if
  one doesn't exist yet
- Frontend: Auth.js (NextAuth v5) with the Keycloak provider - login flow,
  session, logout
- A protected `/profile` page showing the current user's data from `/api/v1/me`

## Out of scope

- **Which organization a new user joins is arbitrary (first org that
  exists).** No invite flow, no org picker, no signup - a real assignment
  mechanism is future scope once there's more than one organization that
  matters. If zero organizations exist, provisioning fails with a clear error
  rather than silently creating an orphaned user.
- **Retroactively protecting 1b's Organization endpoints or
  `/organizations/new`.** They stay open/unauthenticated, exactly as 1b left
  them. Deciding to lock them down is a separate call, not something this
  feature does as a side effect.
- RP-initiated logout (also ending the Keycloak-side SSO session) - "logout"
  here means clearing the app's own session. True SSO session termination is
  a hardening item, not needed to prove the flow works.
- Profile editing - `/api/v1/me` is read-only (GET). No update endpoint.
- MFA, SAML, SCIM, refresh-token rotation edge cases - default Keycloak/Auth.js
  behavior is enough for now.
- Roles/permissions enforcement (feature 2 - RBAC)

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Keycloak realm/client, imported at startup** -
  `keycloak/realm-export.json`: realm `testmgmt`, public client `frontend`
  (PKCE, no secret, redirect URIs for `localhost:3000`, Direct Access Grants
  **enabled** so later steps can fetch a token via `curl` for backend
  verification without a browser), one seeded test user
  (`testuser`/`testpass123`, local-dev only). `docker-compose.yml` mounts the
  file and passes `--import-realm`. *Done when:* `docker-compose up -d
  --force-recreate keycloak`, then `curl` the realm's OIDC discovery document
  (`/realms/testmgmt/.well-known/openid-configuration`) returns 200, and
  fetching a token for `testuser` via the password grant succeeds.
- [x] **Step 2 - Backend JWT validation (Spring Security resource server)** -
  add `spring-boot-starter-security` + `spring-boot-starter-oauth2-resource-server`;
  `issuer-uri` pointing at the Step 1 realm; security config permitting
  `/actuator/**` and `/api/v1/organizations/**` unauthenticated (unchanged
  from 1b), requiring a valid JWT for everything else. *Done when:* `curl
  /api/v1/me` with no token returns 401; `curl /api/v1/organizations` (no
  token) still returns 200 - proving 1b didn't regress.
- [x] **Step 3 - `/api/v1/me` endpoint + auto-provisioning** - on a valid
  request, look up `User` by `externalAuthId` (JWT `sub`); if none exists,
  create one from the token's claims (`email`, `given_name`, `family_name`),
  assigned to the first organization found by creation order. Return 409 if
  no organization exists yet. *Done when:* using the Step 1 test user's
  token, `curl /api/v1/me` returns 200 with the user's profile on first call,
  and the exact same user (not a duplicate) on a second call.
- [x] **Step 4 - Frontend Auth.js + Keycloak provider** - install Auth.js
  (verify current stable `next-auth` version/install command at build time,
  don't assume), configure the Keycloak provider against the Step 1 realm,
  add the App Router auth route handler. No UI yet. *Done when:* the real
  `signIn()` flow (CSRF-protected POST, not a bare GET - see Build notes)
  redirects toward the Keycloak realm's login page.
- [x] **Step 5 - Login/logout UI + protected profile page** - a sign-in
  link, a `/profile` page that redirects unauthenticated visitors to sign-in
  and otherwise calls `/api/v1/me` (passing the session's access token) and
  displays the result, plus a sign-out control. *Done when:* browser-driven -
  visiting `/profile` unauthenticated redirects toward sign-in; signing in as
  the Step 1 test user and revisiting `/profile` shows their real email/name
  from the backend; signing out and revisiting `/profile` redirects again.

## Files / areas

- `keycloak/realm-export.json` - new
- `docker-compose.yml` - mount the realm export, add `--import-realm`
- `backend/pom.xml` - `spring-boot-starter-security` + `spring-boot-starter-security-oauth2-resource-server` (corrected name, see Build notes)
- `backend/.../common/security/` - `SecurityConfig`
- `backend/.../user/controller/`, `user/service/`, `user/dto/` - new, extends the `user/` module from 1b (entity/repository only, until now). No separate mapper class - the service maps directly to `MeDto`, small enough not to warrant one.
- `frontend/auth.ts`, `frontend/app/api/auth/[...nextauth]/route.ts`, `frontend/types/next-auth.d.ts`
- `frontend/app/profile/page.tsx` - new
- `frontend/app/page.tsx` - added a minimal sign-in/organizations nav, not a redesign

## Data / contracts

**Load-bearing:**
- `GET /api/v1/me` response shape: `{ id, email, firstName, lastName, organizationId, organizationName }` - later features (dashboard, "current user" context anywhere) will want this same shape; don't invent a second one.
- Auto-provisioning assigns the **first organization by creation order** - documented here as a known, temporary simplification. The next feature that touches org membership should replace this, not build on top of it as if it were real design.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stayed off.
Steps 1-4 verified via `curl` (discovery document, token grant, 401/200/409
status codes, redirect behavior). Step 5's done-when was behavioral, verified
with real browser automation (ephemeral Playwright, session scratchpad only,
never added to `frontend/package.json`) - all 6 checks passed, plus the user
independently confirmed the flow manually before requesting `/complete`.

## Notes for the AI

- `testuser`/`testpass123` in the realm-export is local-dev-only seed data,
  clearly not production credentials - don't treat it as a security concern
  to fix, but don't let it leak into any deployment config later either.
- `/organizations/new` and the Organization REST endpoints' auth status were
  left untouched - exactly as 1b left them.
- Step 3's "first organization" lookup fails with 409 if none exists, not a
  silent orphaned user or a 500 - verify this stays true if that logic is
  ever touched.

## Build notes (from implementation)

Four real, sequential things discovered during the build, not assumed from
the spec's text:

1. **Backend security starter naming (Step 2).** Spring Boot 4.1's Initializr
   names it `spring-boot-starter-security-oauth2-resource-server`, not the
   `spring-boot-starter-oauth2-resource-server` the spec assumed - checked
   against Initializr's actual output before writing `pom.xml`, continuing
   the "verify, don't assume" pattern from feature 1a's Flyway surprise.
2. **Default session cookie (Step 2).** Spring Security defaults to
   session-based auth unless told otherwise - a stray `JSESSIONID` cookie
   appeared on the first 401 response. Not in the original spec; fixed with
   explicit `SessionCreationPolicy.STATELESS` since it's a real correctness
   issue for a JWT resource server, not scope creep.
3. **Stale build state (Step 2).** After the session-policy fix, a restart
   failed with a misleading DataSource error despite correct config -
   `target/classes` was stale from the day's many restart cycles. `mvn clean`
   resolved it; confirmed via dependency tree that the driver was never
   actually missing before concluding it was a build artifact, not a bug.
4. **Auth.js's actual sign-in mechanism (Step 4).** `next-auth`'s `latest`
   npm tag is still v4 even now; v5 remains on the `beta` tag but is still
   correct for App Router (that's what its APIs are built for). Once
   installed, two more surprises: `AUTH_SECRET` is required even in dev (no
   silent auto-generation in this build), and a bare `GET
   /api/auth/signin/keycloak` returns `UnknownAction` - the real flow is a
   CSRF-protected `POST` with a token matching a cookie, exactly what the
   client-side `signIn()` helper does. Verified by replicating that exact
   POST with `curl` and getting a genuine `302` to Keycloak with PKCE
   parameters attached automatically.

Every one of these was caught by actually running the code and reading the
real error, not by inspecting the spec or guessing.
