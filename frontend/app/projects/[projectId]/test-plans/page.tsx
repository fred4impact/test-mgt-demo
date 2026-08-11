import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTestPlans } from "@/services/testplans";
import { listReleases } from "@/services/releases";
import { CreateTestPlanForm } from "./CreateTestPlanForm";
import { statusBadgeClasses } from "@/lib/badges";

export default async function ProjectTestPlansPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-plans`);
  }

  const [releases, testPlans] = await Promise.all([
    listReleases(session.accessToken, projectId),
    listTestPlans(session.accessToken, projectId),
  ]);
  const releaseNameById = new Map(releases.map((release) => [release.id, release.name]));

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Test Plans</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {testPlans.length}
        </span>
      </div>

      {releases.length === 0 ? (
        <p className="text-sm text-muted">Create a release first before creating a test plan.</p>
      ) : (
        <CreateTestPlanForm projectId={projectId} releases={releases} />
      )}

      <div className="mt-6 overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Name</th>
              <th className="px-4 py-2.5">Release</th>
              <th className="px-4 py-2.5">Status</th>
              <th className="px-4 py-2.5">Start Date</th>
              <th className="px-4 py-2.5">End Date</th>
            </tr>
          </thead>
          <tbody>
            {testPlans.map((testPlan) => (
              <tr key={testPlan.id} className="border-t border-border hover:bg-surface-sunken">
                <td className="px-4 py-2.5 font-semibold text-text">{testPlan.name}</td>
                <td className="px-4 py-2.5 text-muted">
                  {releaseNameById.get(testPlan.releaseId) ?? "unknown release"}
                </td>
                <td className="px-4 py-2.5">
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(testPlan.status)}`}
                  >
                    {testPlan.status}
                  </span>
                </td>
                <td className="px-4 py-2.5 text-muted">{testPlan.startDate ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{testPlan.endDate ?? "-"}</td>
              </tr>
            ))}
            {testPlans.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-faint">
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
