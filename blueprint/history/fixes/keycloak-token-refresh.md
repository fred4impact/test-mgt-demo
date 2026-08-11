# Fix: Keycloak access token refresh

**Type:** Fix
**Status:** complete

## Goal

`frontend/auth.ts`'s `jwt` callback only captures Keycloak's `access_token`
on initial sign-in and never refreshes it. Keycloak access tokens in this
dev realm are short-lived (observed expiring within minutes during backend
testing this session), so every session eventually starts sending a stale
token to the backend, which returns 401 - surfaced to the user as "Failed to
load projects" (or any other entity) via the generic `!res.ok` error path in
every `frontend/services/*.ts` function. This has been flagged as a known
gap several times this session and just became a real, user-hit blocker.

## In scope

- `frontend/auth.ts`: `jwt` callback stores `refreshToken` and
  `accessTokenExpires` (epoch ms) alongside `accessToken` on initial
  sign-in (`account` present); on every subsequent call, if the current
  time is still before `accessTokenExpires`, return the token unchanged; if
  expired, call Keycloak's token endpoint with `grant_type=refresh_token`
  to get a new access/refresh token pair and update the token
- On a failed refresh (e.g. the refresh token itself expired, or is
  missing entirely - the exact shape an old, pre-fix session cookie has),
  set `token.error = "RefreshAccessTokenError"` rather than throwing - the
  `session` callback surfaces this as `session.error`
- `frontend/types/next-auth.d.ts`: extend the `JWT` type with
  `refreshToken?: string`, `accessTokenExpires?: number`, `error?: string`,
  and the `Session` type with `error?: string`
- **Every page's existing session guard treats `session.error` as
  unauthenticated, not just a missing `accessToken`.** All 13 pages that
  currently check `if (!session?.accessToken) { redirect(...) }` change to
  `if (!session?.accessToken || session.error) { redirect(...) }` - the
  same one-line change, same redirect target, in every file. This is the
  actual production-safety fix: without it, *any* eventual auth failure
  (refresh token expired after days of inactivity, Keycloak session
  revoked, or - as hit live during this fix - an old pre-migration session
  cookie) reaches an unguarded `services/*.ts` fetch, throws, and crashes
  the page with a raw Next.js Runtime Error screen instead of a clean
  redirect to sign-in. Token refresh (the first bullet) reduces how often
  this happens; this bullet is what makes the failure mode safe when it
  still happens.

## Out of scope

- A shared `requireSession()` helper to de-duplicate the guard across
  pages. The mechanical one-line change is small enough per file (13 near-
  identical edits) that introducing an abstraction now is not worth it;
  revisit if a 14th page makes the duplication actually painful.
- A visible "your session expired" message before the redirect - the
  redirect to sign-in is the fix; a friendlier interstitial is a UI
  polish concern for later.
- Any change to Keycloak realm/client configuration (token lifespans,
  refresh token rotation policy). This fix works with whatever the realm is
  already configured to issue.
- Any change to the `frontend/services/*.ts` error-handling pattern
  (`throw new Error(...)` on `!res.ok`). Out of scope for this fix.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Refresh-token rotation in `auth.ts`** - store
  `refreshToken`/`accessTokenExpires` on sign-in, refresh via Keycloak's
  token endpoint when expired, extend `next-auth.d.ts` types. *Done when:*
  `npm run build` passes; a real sign-in followed by forcing the stored
  token to look expired (simulated by moving `accessTokenExpires` into the
  past) triggers a real refresh call to Keycloak that succeeds, and a
  subsequent backend call with the refreshed token succeeds (proven with
  browser evidence, not just reading the code).
- [x] **Step 2 - Redirect on `session.error`, everywhere** - all 13 pages'
  `if (!session?.accessToken)` guards become
  `if (!session?.accessToken || session.error)`. *Done when:* `npm run
  build` passes; with a session carrying `error: "RefreshAccessTokenError"`
  (simulated the same way Step 1's forced-expiry case was), visiting any
  guarded page redirects to sign-in instead of reaching the page body and
  throwing.

## Files / areas

- `frontend/auth.ts`
- `frontend/types/next-auth.d.ts`
- 13 page files under `frontend/app/` (session guard change)

## Data / contracts

None - this is frontend session-handling internals only, no backend or DTO
change.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
this is integration/session-flow logic, verified via browser evidence
(real sign-in, forced-expiry scenario, confirmed backend call succeeds
post-refresh) rather than a unit test, matching the project's UI/integration
convention.

## Notes for the AI

- Keycloak client `frontend` is public (no `KEYCLOAK_CLIENT_SECRET` in
  `.env.local`) - confirmed the refresh grant works with just `client_id`,
  no secret, the same way the initial `password` grant curl calls worked
  this session without one.
- `account.expires_at` from NextAuth's OAuth flow is seconds-since-epoch;
  convert to milliseconds for `Date.now()` comparisons.
- Keep the refresh failure path non-throwing - returning
  `{ ...token, error: "RefreshAccessTokenError" }` keeps the session alive
  (just flagged as broken) rather than crashing the `jwt` callback and
  breaking sign-in entirely.

## Build notes (from implementation)

This fix's scope grew mid-implementation, on the user's explicit direction:
the original plan (Step 1 only, token refresh) was drafted with the
session-error UI handling deliberately deferred as "a separate, later
concern." After the user hit the live symptom (`Failed to load profile` /
`Failed to load projects` crash screens) and then asked "I can't afford
this in a live environment, what can be done," Step 2 was added in the same
fix rather than deferred, since the user's concern was specifically about
production safety, not just the dev-session convenience the original scope
addressed.

Both steps were proven with real, non-simulated evidence rather than
assumed from the code:

- Step 1: Keycloak's realm-wide `accessTokenLifespan` was temporarily
  lowered from 300s to 5s via the admin REST API (`admin`/`admin`, realm
  `master`), a real signed-in session was driven past that expiry, and
  `/profile` loaded successfully via a transparent refresh - proving the
  `jwt` callback's timing and refresh-call logic actually fire, not just
  that they compile.
- Step 2: the same session's Keycloak session was then revoked server-side
  via the admin API (`POST .../users/{id}/logout`), which invalidates the
  refresh token. After the next access-token expiry, the app was confirmed
  to redirect cleanly to Keycloak's sign-in page rather than reaching an
  unguarded fetch and crashing.
- The realm's `accessTokenLifespan` was restored to its original value
  (300) immediately after the test.
- A known, expected, one-time side effect: any browser session that was
  already open before this fix landed has no `refreshToken` in its
  encrypted cookie (it predates the new `jwt` callback shape), so its next
  refresh attempt fails and - correctly, per Step 2 - redirects to sign-in
  rather than crashing. This was observed live by the user immediately
  after the fix deployed and is the expected, one-time migration behavior,
  not a bug; a single fresh sign-in resolves it permanently for that
  browser.
