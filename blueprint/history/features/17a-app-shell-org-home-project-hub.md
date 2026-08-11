# Feature: App shell, org home & project hub

**From build-plan:** feature 17a
**Status:** complete

## Goal

Replace the current flat, disconnected pages (root page is still the
unmodified `create-next-app` boilerplate; `/organizations`, `/projects`,
`/teams`, `/roles` only have "new" creation forms, no browsing) with the
drill-down flow a signed-in user actually expects: land on your organization,
see your projects, open one into a hub that links out to its requirements,
test cases, test suites, tags, and releases. This is frontend-only - every
backend endpoint it needs (`GET /api/v1/me`, `GET /api/v1/projects`, `GET
/api/v1/projects/{id}`) already exists.

## In scope

- A persistent nav shell (app name linking home, Teams, Roles, Profile, sign
  out) rendered on every authenticated page; minimal (sign-in link only) on
  the signed-out landing page
- Real root page (`/`): signed-out visitors see an actual landing page (not
  the Next.js/Vercel template), signed-in visitors redirect to `/home`
- Org home (`/home`): the user's organization name + their projects (via the
  existing `listProjects`), each linking into that project; a "create
  project" link
- `frontend/services/me.ts`: extract the `/me` fetch already duplicated
  inline in `profile/page.tsx` so `/home` can reuse it for the org name,
  instead of copy-pasting the same fetch a second time
- Project hub (`/projects/[projectId]`): project name/key/status + link-cards
  into that project's Requirements, Test Cases, Test Suites, Tags, and
  Releases (the existing sub-pages) - `Build` isn't linked yet since 8b
  hasn't shipped; add its card when it does
- `getProject(accessToken, id)` added to `frontend/services/projects.ts`
  (the `GET /api/v1/projects/{id}` endpoint already exists, just unused by
  the frontend so far)
- Breadcrumbs: the project hub links back to `/home`; the five existing
  project sub-pages (requirements, test-cases, test-suites, tags, releases)
  gain a link back to the project hub

## Out of scope

- `/teams` and `/roles` real listing pages - explicitly 17b. The nav shell
  links to today's `/teams/new` and `/roles/new` (the only routes that
  exist); 17b upgrades those two links once real listing pages exist, it
  doesn't touch this feature's other nav targets.
- A project members page. `ProjectMember` (feature 3c) is deliberately
  API-only per that feature's spec; adding a frontend for it is a separate
  concern, not implied by this navigation feature.
- Any visual redesign, theming, or color/typography work. `project-plan.md`
  §7 has no visual direction yet (`> TODO` in the overview) - this feature is
  structural (what links to what), not a reskin. Plain Tailwind utility
  classes matching the existing pages' style.
- Multi-organization switching. `User.organizationId` is a single field (see
  `project-overview.md`'s data model) - a user belongs to exactly one
  organization, so there's no org picker to build.
- Redesigning the `/projects/new`, `/teams/new`, `/roles/new` create forms
  themselves - they keep working as they are, just get linked to from the
  new hub pages instead of being the only entry point.
- A `Build` link-card on the project hub - added when 8b ships, not now.

## Build loop

Build one step at a time, never the whole feature at once.

1. Plan mode lays out the step before any code.
2. The AI implements just that step.
3. It shows the diff (not full files); you read it and understand it.
4. You approve, then choose whether to commit a checkpoint or roll straight on.
   Checkpoints are optional; `/complete` makes the real feature-level commit at the end.

Never accept a step you haven't read. If a diff is too big to review, the step was too big, so split it.

## Build steps

- [x] **Step 1 - Real root page + org home** - `frontend/services/me.ts`
  (extracted from `profile/page.tsx`'s inline `fetchMe`); `app/page.tsx`
  rewritten: signed-out shows a real landing page with a sign-in link (no
  Next.js/Vercel boilerplate), signed-in redirects to `/home`; new
  `app/home/page.tsx` fetches `/me` + `listProjects`, shows the org name and
  each project (name, key) linking to `/projects/[id]/requirements` (the
  existing route - the hub doesn't exist until Step 3), plus a "create
  project" link to `/projects/new`; empty project list shows "no projects
  yet." *Done when:* signed-out visit to `/` shows the new landing page (no
  Vercel/Next.js content); signed-in visit to `/` redirects to `/home`;
  `/home` lists real projects with working links; `profile/page.tsx` still
  works, now importing `fetchMe` from `services/me.ts` instead of defining
  it locally.
- [x] **Step 2 - Nav shell** - `frontend/components/nav/AppNav.tsx`, an
  async server component calling `auth()`: signed out renders just a
  sign-in link; signed in renders app name (linking `/home`), Teams
  (`/teams/new`), Roles (`/roles/new`), Profile (`/profile`), and a sign-out
  form (reuse the pattern already in `profile/page.tsx`). Rendered from
  `app/layout.tsx` above `{children}` so it's on every page. *Done when:*
  screenshot shows the nav on `/home` and `/profile` with all links working;
  screenshot of the signed-out `/` shows only the sign-in link, no internal
  nav.
- [x] **Step 3 - Project hub** - `getProject(accessToken, id)` added to
  `frontend/services/projects.ts`; new `app/projects/[projectId]/page.tsx`
  fetches the project and renders its name/key/status plus link-cards to
  Requirements, Test Cases, Test Suites, Tags, and Releases (the existing
  sub-routes) and a link back to `/home`; `/home`'s project links updated to
  point at `/projects/[id]` instead of `/projects/[id]/requirements`. *Done
  when:* visiting `/projects/[id]` for a real project shows its info and 5
  working links; visiting a project id that belongs to a different
  organization shows a clear "not found" state (backend already 404s via
  `getById`, catch it and render a message rather than an unhandled error);
  `/home`'s project links now land on the hub.
- [x] **Step 4 - Breadcrumb back-links on sub-pages** - each of
  `requirements/page.tsx`, `test-cases/page.tsx`, `test-suites/page.tsx`,
  `tags/page.tsx`, `releases/page.tsx` gains a small link back to
  `/projects/[projectId]` (e.g. "<- Back to project"), placed consistently
  above each page's existing heading. *Done when:* screenshot of each of the
  5 pages shows the back-link and clicking it lands on the project hub.

## Files / areas

- `frontend/app/page.tsx` (rewritten)
- `frontend/app/home/page.tsx` (new)
- `frontend/app/layout.tsx` (adds `<AppNav>`)
- `frontend/components/nav/AppNav.tsx` (new)
- `frontend/services/me.ts` (new, extracted from `profile/page.tsx`)
- `frontend/app/profile/page.tsx` (imports `fetchMe` from the new service
  instead of defining it inline)
- `frontend/services/projects.ts` (adds `getProject`)
- `frontend/app/projects/[projectId]/page.tsx` (new - the hub)
- `frontend/app/projects/[projectId]/{requirements,test-cases,test-suites,tags,releases}/page.tsx`
  (each gains a back-link)

## Data / contracts

None new - this feature is entirely a frontend consumer of existing backend
endpoints (`GET /api/v1/me`, `GET /api/v1/projects`, `GET
/api/v1/projects/{id}`). No migration, no DTO change, no new REST endpoint.

## Testing

No test command exists in `AGENTS.md` yet, so the testing gate stays off -
this is entirely UI/integration work anyway (page composition and links, no
new parsing/validation/mapping logic), so it rides on browser screenshots and
`npm run build` per every prior UI-only step in this project.

## Notes for the AI

- `AppNav` is a server component that calls `auth()` itself (same pattern
  every page already uses) rather than threading session down as a prop -
  simplest fit for the existing per-page `auth()` convention.
- Reuse the sign-out `<form action={...}>` pattern already in
  `profile/page.tsx` verbatim inside `AppNav` rather than inventing a new
  sign-out mechanism.
- `getProject`'s error handling should follow the same shape as every other
  service function in `frontend/services/` (`throw new Error(...)` on
  `!res.ok`), and the hub page catches it to render "not found" rather than
  letting Next.js's default error boundary show a raw stack trace.
- Don't touch `/organizations/new`, `/projects/new`, `/teams/new`,
  `/roles/new` beyond linking to them - their internals are out of scope.
- Keep styling consistent with the existing pages (`max-w-md`/`max-w-3xl`
  containers, `text-sm`, `rounded border border-gray-300`, etc.) - no new
  design system, per Out of scope.

## Build notes (from implementation)

This feature required zero backend changes, confirmed before writing the
spec by reading `OrganizationController`/`ProjectService` directly - every
endpoint needed (`/me`, `listProjects`, `getById`) already existed and was
simply unused by the frontend.

- Step ordering was deliberately chosen to avoid any dead link mid-feature:
  org home shipped before the nav shell linked to it, and the project hub
  shipped before sub-pages linked back to it. Every step left the app fully
  working end to end, verified with a real signed-in Playwright run each
  time (Keycloak login via the `testuser`/`testpass123` test account already
  established in prior features' verification scripts).
- Caught and fixed one leftover boilerplate item outside the original file
  list: `app/layout.tsx`'s `<title>` still said "Create Next App" even after
  the root page itself was rewritten - fixed as part of Step 1 since it's
  the same category of leftover the step was already removing.
- The project hub's "not found" state (a project id from another
  organization, or a nonexistent one) was proven with a real request to a
  random UUID, confirming the backend's existing 404 is caught and rendered
  as a clean message rather than an unhandled error boundary.
- The full round-trip (org home -> hub -> each of the 5 sub-pages -> back to
  hub) was scripted and verified for all 5 pages in one pass, not spot-checked
  on just one.
