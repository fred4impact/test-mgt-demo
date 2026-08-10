# Build Plan

> One of the two planning docs you provide. Write it yourself or with the AI's help.

The features that make up this project, high level and in rough build order, one
line each, no detail (that comes per feature). Rough is fine at first, but before
`/overview` runs this file should be shaped into a checkbox list the build loop
can track.

Keep it as a checklist. Run `/feature` with no number to spec the **next
unchecked** item, or `/feature 3` / `/feature "login"` to pick a specific one.
Completed features get checked off here, so the build plan doubles as your
progress tracker. A big item gets split into sub-items (4a, 4b, etc.) when you
spec it.

## Continuing after the initial build

This is a living roadmap, not a plan that freezes when the first release is
done. Keep completed items checked, then append new unchecked features as the
project grows. Optional milestone headings such as `## MVP` and `## Post-MVP`
keep a longer plan readable without changing how `/feature` finds the next
unchecked item.

Do not renumber completed features because their archived specs refer back to
those numbers. Continue with the next unused number. If a new feature materially
changes the product direction, users, data, stack, monetization, UI/UX, or
deployment, update the relevant part of `project-plan.md` too. Then re-run
`/overview` before spec'ing the feature.

You can edit this file directly or ask the AI to start a new feature by name. If
`/feature "team workspaces"` does not match an existing item, it will propose the
new build-plan line and any necessary project-plan changes, wait for approval,
refresh the overview, and then write the feature spec.

Scaffolding the app (create-next-app, etc.) and prototyping the look are
pre-build steps, not features (see the README), so don't list them here. Start
with your first real slice of functionality.

A common order that works well: build the core UI with placeholder data first,
then wire up data, auth, and integrations. Add deployment readiness only when
the app is worth shipping or a provider config change is part of the work. Adapt
it to your project.

## Format

Use checkboxes. Each item should be a feature-sized outcome, not a loose task or
a whole product area.

Good:

- [ ] 1. **Skill submission** - upload a skill package and save its metadata
- [ ] 2. **Validation result** - run checks and show pass/fail status for a skill
- [ ] 3. **Directory listing** - browse and filter published skills
- [ ] 4. **Deployment readiness** - configure Render or Vercel and verify the
  production build

Avoid:

- Upload stuff
- Database
- Make it look nice
- Auth, billing, dashboard, validation, and deploy

If your first pass is just rough bullets, that is okay. Run `/overview` after
filling both planning docs; it will flag plan-shape problems and can propose a
cleaned-up checkbox version before generating the project overview.

- [x] 1. **Org & auth foundation** - organization creation, user accounts, login/logout via Keycloak/OIDC, profile
  - [x] 1a. **Project scaffolding & local dev environment** - Next.js frontend, Spring Boot (Maven) backend, Docker Compose for Postgres/Redis/Keycloak; both apps run locally with a health check
  - [x] 1b. **Organization & user model** - backend Organization/User entities + Flyway migrations + REST endpoints; minimal frontend to create the first organization
  - [x] 1c. **Keycloak login/logout & profile** - OIDC login flow, JWT validation on the backend, protected routes, session/logout, profile page
- [x] 2. **Projects** - create/manage projects (project membership moved to feature 3, alongside Role)
- [x] 3. **Teams & roles** - teams, roles, permissions, RBAC assignment (assignment via `ProjectMember.roleId`, added by this feature)
  - [x] 3a. **Teams** - Team + TeamMember, org-scoped CRUD, add/remove members
  - [x] 3b. **Roles & permissions catalog** - seeded Permission catalog, org-scoped Role CRUD, attach/detach permissions to a role
  - [x] 3c. **Project membership & role assignment** - ProjectMember (assign a user to a project with a role); enforcing permissions on endpoints is a separate later concern, not this sub-feature
- [x] 4. **Requirements** - create and manage requirements within a project
- [ ] 5. **Test case repository** - folders, test cases, test steps, tags
- [ ] 6. **Test suites** - group test cases into suites
- [ ] 7. **Search & filters** - keyword search and filtering across test cases/requirements/defects
- [ ] 8. **Releases, builds & environments** - create and manage each
- [ ] 9. **Test plans** - create test plans scoped to a release
- [ ] 10. **Test cycles** - create cycles under a plan, assign build/environment
- [ ] 11. **Test execution** - assign testers, execute steps, record pass/fail/block/skip, attachments
- [ ] 12. **Defects** - create/edit, link severity/priority/status
- [ ] 13. **Traceability** - link requirement to test case to execution to defect, coverage view
- [ ] 14. **Reporting dashboard** - execution stats, pass/fail, coverage, defect stats, release progress
- [ ] 15. **CSV export** - export reports and test data
- [ ] 16. **Automation result ingestion** - accept JUnit/TestNG/Cucumber/pytest/Playwright/Cypress results, automation dashboard
