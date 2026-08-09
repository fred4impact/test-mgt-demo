# Project Plan

> One of the two planning docs you provide. Answer each section in a line or two
> (a worksheet, not an essay). Draft it yourself or let the AI help you expand and
> sharpen it; either way, the content is yours to direct. When it's filled in, run
> `/overview` to generate the project overview from this plus `build-plan.md`.

## 1. Problem - What problem are we solving?

Teams need a unified platform to manage test cases, executions, requirements
traceability, and defects so a QA manager can immediately answer: what needs
testing, what's been tested, what passed/failed, what's blocking release, and
whether the release can ship safely. Comparable in capability to Zephyr.

## 2. Users - Who is this for?

QA managers, testers/test engineers, and release managers at organizations
doing structured test management. Multi-tenant - multiple organizations, each
with their own teams, projects, and role-based permissions.

## 3. Features - What does the MVP need?

- Auth, organizations, projects, roles & permissions
- Test case repository - folders, cases, steps, tags, search, filters, bulk edit
- Requirements, releases, test plans, test cycles
- Test execution - assign, execute, pass/fail/block/skip, history, attachments
- Defects - create/edit, link to executions, severity/priority/status
- Traceability - requirement to test case to execution to defect, coverage view
- Reporting - execution dashboard, pass/fail stats, coverage, defect stats,
  release progress, CSV export
- Automation result ingestion (JUnit/TestNG/Cucumber/pytest/Playwright/Cypress),
  later MVP phase

## 4. Data - What are we storing?

Organizations, users, teams, projects, roles/permissions, releases, builds,
environments, requirements, test folders/cases/steps/versions, tags, test
suites, test plans/cycles, test executions/execution steps, defects,
attachments, comments, audit logs, automation runs/results,
integrations/webhooks, saved filters.

## 5. Tech - What stack are we using?

Next.js/React/TypeScript/Tailwind/shadcn (frontend); Spring Boot/Java 21
(backend); PostgreSQL with Flyway migrations; Redis (cache); Kafka
(async/events); OpenSearch (search, once needed); S3/MinIO (files);
Keycloak/OIDC (auth); Docker Compose for local dev, Kubernetes for prod;
GitHub Actions (CI/CD); OpenTelemetry/Prometheus/Grafana/Loki (observability).

## 6. Monetize - How will this make money?

> TODO - not addressed in the source spec. Decide before scope depends on it
> (e.g. seat-based tiers gating features).

## 7. UI/UX - How should this look and feel?

Enterprise dashboard tool - dense data-grid heavy, functional-first,
comparable to Zephyr/Jira.

> TODO - no explicit color/tone/visual direction yet. Consider `/prototype`
> once ready to nail down the actual look.

## 8. Deployment - Where and how will this ship?

Local: Docker Compose (frontend, backend, postgres, redis, kafka, opensearch,
minio, keycloak). Production: Kubernetes behind a load balancer/ingress,
managed Postgres/Redis/Kafka/object storage preferred. Cloud named only
generically as AWS/Azure/GCP.

> TODO - no specific host, env vars, or health-check path named yet.
