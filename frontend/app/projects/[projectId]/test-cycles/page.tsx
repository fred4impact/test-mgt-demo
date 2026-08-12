import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTestCycles } from "@/services/testcycles";
import { listTestPlans } from "@/services/testplans";
import { listReleases } from "@/services/releases";
import { listBuilds } from "@/services/builds";
import { listEnvironments } from "@/services/environments";
import { statusBadgeClasses } from "@/lib/badges";
import { CreateTestCycleForm } from "./CreateTestCycleForm";

export default async function ProjectTestCyclesPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-cycles`);
  }

  const [testPlans, releases, builds, environments, testCycles] = await Promise.all([
    listTestPlans(session.accessToken, projectId),
    listReleases(session.accessToken, projectId),
    listBuilds(session.accessToken, projectId),
    listEnvironments(session.accessToken, projectId),
    listTestCycles(session.accessToken, projectId),
  ]);

  const testPlanNameById = new Map(testPlans.map((testPlan) => [testPlan.id, testPlan.name]));
  const releaseNameById = new Map(releases.map((release) => [release.id, release.name]));
  const buildNameById = new Map(builds.map((build) => [build.id, build.name]));
  const environmentNameById = new Map(environments.map((environment) => [environment.id, environment.name]));

  const missingPrerequisites = [
    testPlans.length === 0 ? "a test plan" : null,
    releases.length === 0 ? "a release" : null,
    builds.length === 0 ? "a build" : null,
    environments.length === 0 ? "an environment" : null,
  ].filter((item): item is string => item !== null);
  const missingPrerequisitesText =
    missingPrerequisites.length <= 1
      ? missingPrerequisites.join("")
      : missingPrerequisites.length === 2
        ? missingPrerequisites.join(" and ")
        : `${missingPrerequisites.slice(0, -1).join(", ")}, and ${missingPrerequisites.at(-1)}`;

  return (
    <main className="mx-auto max-w-7xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Test Cycles</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {testCycles.length}
        </span>
      </div>

      {missingPrerequisites.length > 0 ? (
        <p className="text-sm text-muted">
          Create {missingPrerequisitesText} first before creating a test cycle.
        </p>
      ) : (
        <CreateTestCycleForm
          projectId={projectId}
          testPlans={testPlans}
          releases={releases}
          builds={builds}
          environments={environments}
        />
      )}

      <div className="mt-6 overflow-x-auto rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Name</th>
              <th className="px-4 py-2.5">Test Plan</th>
              <th className="px-4 py-2.5">Release</th>
              <th className="px-4 py-2.5">Build</th>
              <th className="px-4 py-2.5">Environment</th>
              <th className="px-4 py-2.5">Status</th>
              <th className="px-4 py-2.5">Start Date</th>
              <th className="px-4 py-2.5">End Date</th>
            </tr>
          </thead>
          <tbody>
            {testCycles.map((testCycle) => (
              <tr key={testCycle.id} className="border-t border-border hover:bg-surface-sunken">
                <td className="px-4 py-2.5 font-semibold text-accent hover:underline">
                  <Link href={`/projects/${projectId}/test-cycles/${testCycle.id}`}>{testCycle.name}</Link>
                </td>
                <td className="px-4 py-2.5 text-muted">{testPlanNameById.get(testCycle.testPlanId) ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{releaseNameById.get(testCycle.releaseId) ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{buildNameById.get(testCycle.buildId) ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{environmentNameById.get(testCycle.environmentId) ?? "-"}</td>
                <td className="px-4 py-2.5">
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(testCycle.status)}`}
                  >
                    {testCycle.status}
                  </span>
                </td>
                <td className="px-4 py-2.5 text-muted">{testCycle.startDate ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{testCycle.endDate ?? "-"}</td>
              </tr>
            ))}
            {testCycles.length === 0 && (
              <tr>
                <td colSpan={8} className="px-4 py-6 text-center text-faint">
                  None yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
}
