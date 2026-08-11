# Fix: Apply the modern bold wide theme to the real app

**Type:** Fix
**Status:** complete

## Design reference

`prototypes/theme.css` (the locked tokens) and `prototypes/landing.html`,
`prototypes/project-hub.html`, `prototypes/test-cases.html` (the approved
mockups) - all committed on `master`. This fix ports the theme into the
real app and re-themes the pages that were actually mocked.

## The problem

Every real page still uses the unstyled default (`max-w-md`, plain
black/white/gray, no accent color, narrow centered forms) - the
`/prototype` pass locked a real direction (bold wide shell + dense
functional data-grids, deep indigo/violet accent) but nothing in
`frontend/` uses it yet.

## The fix

Port `theme.css`'s tokens into `frontend/app/globals.css`'s `@theme` (using
Tailwind v4's `--color-*`/`--font-*`/`--shadow-*` namespaces so the tokens
generate real utility classes like `bg-accent`, `text-muted`, `shadow-card`),
then re-theme the shared nav shell and the three pages that were actually
mocked and approved: the landing page (`/`), org home (`/home`), the
project hub (`/projects/[projectId]`), and Test Cases as the dense-grid
template (`/projects/[projectId]/test-cases`).

**Scoped deliberately smaller than the full app.** The other 6 project
sub-pages (Requirements, Test Suites, Tags, Releases, Builds, Test Plans)
share near-identical structure with Test Cases today and were not
individually mocked - re-theming them is a natural, well-templated
follow-up once Test Cases exists as the real dense-grid reference, not
bundled into this fix. Same for the remaining "new" forms
(`/projects/new`, `/teams/new`, `/roles/new`, `/organizations/new`,
`/profile`). Cramming all ~18 pages into one fix would make the diff too
big to review in one sitting - this fix proves the theme end-to-end on the
exact pages that were prototyped and approved.

Must not break: the multi-tenancy/auth guards (`if (!session?.accessToken
|| session.error)`), any data-fetching logic, or route structure. This is
styling only.

## Build steps

- [x] **Step 1 - Port theme tokens into `globals.css`** - replace the
  current minimal `--background`/`--foreground` `@theme` block with the
  full token set from `prototypes/theme.css`, mapped into Tailwind v4's
  color/font/shadow namespaces (`--color-bg`, `--color-surface`,
  `--color-surface-sunken`, `--color-border`, `--color-text`,
  `--color-muted`, `--color-faint`, `--color-accent`,
  `--color-accent-hover`, `--color-accent-ink`, `--color-accent-soft`,
  `--color-accent-soft-text`, `--color-status-success`/`-soft`,
  `-warning`/`-soft`, `-danger`/`-soft`, `-neutral`/`-soft`, `--font-sans`,
  `--font-mono`, `--shadow-card`, `--shadow-nav`), including the
  `prefers-color-scheme: dark` override block; `body` background/color
  switch to the new tokens. *Done when:* `npm run build` passes; every
  existing page still renders (just with new colors/fonts where Tailwind
  defaults were used) - no layout breakage, confirmed via screenshot of one
  unmodified page (e.g. `/projects/[projectId]/releases`, untouched this
  fix) before and after.
- [x] **Step 2 - Re-theme `AppNav`** - wider padding, bold wordmark (accent
  on the "MGMT" half, matching the mockups' `Test<span>MGMT</span>`
  treatment), accent hover state on nav links, matching
  `project-hub.html`/`test-cases.html`'s nav bar. *Done when:* screenshot
  of the signed-in nav matches the mockup's look; signed-out nav (just
  "Sign in") still renders correctly.
- [x] **Step 3 - Re-theme the landing page (`/`)** - bold wide hero
  (headline, sub-lede, CTA row) matching `prototypes/landing.html`'s
  layout and type scale; keep the real copy already there (product name,
  one-line purpose) rather than copying the mockup's placeholder marketing
  copy verbatim. *Done when:* screenshot of signed-out `/` matches the
  mockup's structure (bold headline, wide layout, accent CTA button); the
  redirect-when-authenticated behavior is unchanged.
- [x] **Step 4 - Re-theme org home (`/home`)** - bold page heading, project
  list as cards (not a plain `<ul>`) matching `project-hub.html`'s card
  style, wider container. *Done when:* screenshot of `/home` with real
  projects shows card-based layout with the new theme; empty-state and
  "create project" link still present and working.
- [x] **Step 5 - Re-theme the project hub
  (`/projects/[projectId]`)** - match `prototypes/project-hub.html`
  directly: breadcrumb, bold title + key/status pills, the 7-card grid
  with icon tiles. *Done when:* screenshot of the hub matches the mockup;
  all 7 links still route correctly; the "not found" fallback state still
  renders (plain, doesn't need the full theme treatment).
- [x] **Step 6 - Re-theme Test Cases as the dense-grid template
  (`/projects/[projectId]/test-cases`)** - replace the plain `<ul>` list
  with a real `<table>` data-grid matching `prototypes/test-cases.html`:
  columns Key/Title/Folder/Status/Priority/Severity/Steps, status/priority/
  severity as colored badges, dense row height, sunken header row; restyle
  the filter form and create form to match the mockup's filter-bar and
  button styling. Keep all existing data-fetching, filtering, and folder
  fallback logic unchanged - this is a rendering change only. *Done when:*
  screenshot of the page with real test cases shows the dense grid with
  badges matching the mockup; search/filter still works (same query params,
  same results); creating a test case still works; the zero-folders
  fallback message still renders correctly.

## Verify

- `npm run build` passes after every step.
- Signed-in browser walkthrough after Step 6: `/` -> sign in -> `/home` ->
  click a project -> hub -> Test Cases -> confirm the whole path looks
  consistent (same nav, same accent, same type scale) and every existing
  behavior (filters, create forms, back-links, redirects) still works
  exactly as before, just restyled.
- No test command exists in `AGENTS.md`, so this rides on `npm run build` +
  browser/screenshot evidence per step, matching every UI-only step in this
  project.

## Notes for the AI

- Tailwind v4 CSS-first: a `--color-foo` variable in `@theme` generates
  `bg-foo`/`text-foo`/`border-foo`/etc. utilities automatically -
  `prototypes/theme.css`'s bare names (`--accent`, `--surface`, ...) need
  the `--color-` prefix added when porting, they don't carry over 1:1.
- Keep `--font-sans`/`--font-mono` as Tailwind already names them (no
  prefix needed) - `theme.css`'s font stack replaces the current
  Geist-based one; the `next/font` Geist imports in `layout.tsx` can stay
  or go, but the CSS variable they feed (`--font-geist-sans`) should no
  longer be what `--font-sans` resolves to once the new stack is ported -
  don't leave both wired in a way that makes one silently win.
- Don't hand-roll a new component library. Reuse the same badge/card/table
  patterns shown in the mockups' inline `<style>` blocks, translated to
  Tailwind utility classes (or scoped CSS if a utility doesn't exist for
  something like the sunken table header) - don't introduce shadcn/ui or
  another dependency for this fix.
- Steps 3-6 each touch exactly one real page - don't let any step bleed
  into restyling a page not listed for that step, even if it looks
  inconsistent in between (that inconsistency resolves once the deferred
  follow-up fix covers the remaining 6 list pages).

## Build notes (from implementation)

All three mocked pages (landing, project hub, Test Cases) were built to
directly match their mockups, plus the shared nav shell. `prototypes/` is
therefore fully consumed and deleted as part of this completion, per the
Blueprint convention - the theme's durable output now lives in
`frontend/app/globals.css`, and the real Test Cases page is now the
reference the deferred follow-up fix (the other 6 list pages) should build
against, not the old static mockup.

- The Tailwind v4 token-porting required care around variable indirection:
  raw color-scheme-dependent values live in `:root` (with a
  `prefers-color-scheme: dark` override block), and a separate `@theme
  inline` block aliases them into Tailwind's `--color-*`/`--shadow-*`
  namespace via `var()` - defining the same name in both places would be a
  circular reference. Verified correct by checking a real computed style
  (`getComputedStyle(body).backgroundColor` resolved to exactly `rgb(248,
  250, 252)`, the new `#f8fafc` token) rather than assuming from the CSS.
- Every step was proven against the running app, not just visually: Step 1
  confirmed via computed style on an untouched page; Step 5's 7 hub cards
  were each click-tested to confirm routing, not just eyeballed in a
  screenshot; Step 6 exercised the full real flow (genuine empty state on
  this project, folder creation, dense grid render, a no-match filter, the
  clear link, and a real test case creation with the folder name correctly
  joined).
- Removed the now-dead `next/font` Geist wiring from `layout.tsx` (the old
  `--font-geist-sans`/`--font-geist-mono` variables) since the new theme's
  font stack replaced what they fed - avoids two font systems silently
  fighting over `--font-sans`.
- Scope discipline held: no new data fetches were added to any re-themed
  page (e.g. the hub's breadcrumb stayed a single "back" link rather than
  fetching `/me` again just to show an org name in a two-level breadcrumb),
  keeping this fix genuinely styling-only as scoped.
