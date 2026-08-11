# Fix: Re-theme the remaining project sub-pages

**Type:** Fix
**Status:** complete

## The problem

The previous fix (`apply-bold-wide-theme`) ported the theme and re-themed
the shared shell plus 4 pages (landing, org home, project hub, Test Cases
as the dense-grid template) - deliberately deferring the other 6 project
sub-pages. They still use the unstyled default (`max-w-md`, plain
black/white/gray, bare `<ul>` lists). This fix finishes the visual pass on
those 6: Requirements, Test Suites, Tags, Releases, Builds, Test Plans.

## The fix

Extract the two badge-color helpers already written inline in Test Cases
(`statusBadgeClasses`, `severityBadgeClasses`) into a shared
`frontend/lib/badges.ts`, since 4 of the 6 remaining pages need the same
status-coloring logic - then re-theme each page to match Test Cases'
established real pattern (not a mockup this time; Test Cases itself is now
the reference). Not every page becomes a dense table: two of them
(`Test Suites`, `Tags`) only have a `name` field today, so a table would be
mostly empty columns - those two become a card grid / chip grid instead,
which fits their actual shape better while staying consistent with the
same theme tokens.

| Page | Treatment |
|---|---|
| Requirements | Dense table: Key, Title, Status, Priority. Filter bar restyled to match Test Cases'. |
| Releases | Dense table: Name, Version, Status, Start Date, Release Date. |
| Builds | Dense table: Name, Release, Version, Branch, Commit SHA, Status. |
| Test Plans | Dense table: Name, Release, Status, Start Date, End Date. |
| Test Suites | Card grid (bold name cards), matching the org-home/hub card style. |
| Tags | Chip/pill grid, matching a tag's actual shape (a label, not a row of data). |

Must not break: the multi-tenancy/auth guards, any data-fetching, filtering,
or form-submission logic, or route structure. Styling only, same as the
previous fix.

## Build steps

- [x] **Step 1 - Extract shared badge helpers** - move
  `statusBadgeClasses`/`severityBadgeClasses` from
  `test-cases/page.tsx` into `frontend/lib/badges.ts`; update
  `test-cases/page.tsx` to import them. *Done when:* `npm run build`
  passes; Test Cases page renders identically to before (same screenshot
  result as the previous fix's Step 6 evidence) - this step is a pure
  refactor, zero visual change.
- [x] **Step 2 - Re-theme Requirements** - dense table (Key/Title/Status/
  Priority) replacing the `<ul>`; filter bar and create form restyled to
  match Test Cases' filter-bar/card treatment; status badge via the shared
  helper. *Done when:* screenshot shows the dense grid; keyword/status/
  priority filters and "Clear" still work (same query params); creating a
  requirement still works.
- [x] **Step 3 - Re-theme Releases** - dense table (Name/Version/Status/
  Start Date/Release Date, nullable fields shown as "-"); create form
  restyled. *Done when:* screenshot shows the dense grid with status
  badges (`PLANNED` renders via the shared helper); creating a release
  still works.
- [x] **Step 4 - Re-theme Builds** - dense table (Name/Release/Version/
  Branch/Commit SHA/Status, nullable fields as "-", release name joined
  client-side as it already is); create form restyled; zero-releases
  fallback message restyled but unchanged in behavior. *Done when:*
  screenshot shows the dense grid; creating a build still works; the
  zero-releases fallback still renders when applicable.
- [x] **Step 5 - Re-theme Test Plans** - dense table (Name/Release/Status/
  Start Date/End Date); create form restyled; zero-releases fallback
  restyled but unchanged in behavior. *Done when:* screenshot shows the
  dense grid; creating a test plan still works; the zero-releases fallback
  still renders when applicable.
- [x] **Step 6 - Re-theme Test Suites as a card grid** - bold name cards
  matching the hub/org-home card style; create form restyled. *Done when:*
  screenshot shows the card grid; creating a suite still works; empty
  state still renders.
- [x] **Step 7 - Re-theme Tags as a chip grid** - small rounded pills in a
  wrapping flex layout; create form restyled. *Done when:* screenshot
  shows the chip grid; creating a tag still works; empty state still
  renders.

## Verify

- `npm run build` passes after every step.
- Real signed-in browser check per step (not just a screenshot): existing
  filter/create/back-link behavior proven unchanged, same as the previous
  fix's verification standard.
- No test command exists in `AGENTS.md`, so this rides on `npm run build` +
  browser/screenshot evidence per step.

## Notes for the AI

- Test Cases (`test-cases/page.tsx`, already re-themed) is the reference
  pattern now - match its table structure, spacing, and badge treatment,
  not a mockup file (none exist anymore, `prototypes/` was discarded).
- Nullable fields (`version`, `branch`, `commitSha`, `startDate`,
  `releaseDate`, `endDate`, `priority`) render as `"-"` in table cells when
  `null`, not an empty cell or `"null"` text.
- Keep each step to exactly the one page listed - don't let Step 2 bleed
  into Step 3's file, etc.
- `frontend/lib/badges.ts` is a small, directly-justified extraction (4 of
  6 pages need identical status-coloring logic) - not a broader component
  library. Don't add anything to it beyond what these pages actually use.

## Build notes (from implementation)

Built cleanly against the spec. One judgment call worth recording: rather
than forcing every remaining page into the Test Cases dense-table pattern,
Test Suites and Tags got a shape-appropriate treatment instead (card grid
and chip grid respectively), since both only carry a `name` field today - a
table would have been mostly empty columns. This was flagged in the spec
up front, not discovered mid-build.

- Every step's create flow was exercised for real (not just screenshotted):
  a real requirement, release, build, test plan, test suite, and tag were
  each created through their restyled forms and confirmed to appear
  immediately with correct data.
- Existing data created in earlier features this session (builds, test
  plans, releases) was confirmed still rendering correctly under the new
  theme - including nullable fields (`version`, `branch`, `commitSha`,
  dates) rendering as `"-"`, not `"null"` or blank.
- A red "1 Issue" badge appeared in the Next.js dev overlay screenshot
  after Step 6's create-form submission; investigated rather than assumed
  benign - a fresh page load showed zero console errors, confirming it was
  the same known Playwright `caret-color` hydration artifact identified
  earlier this session (triggered by `.fill()`), not a real bug.
