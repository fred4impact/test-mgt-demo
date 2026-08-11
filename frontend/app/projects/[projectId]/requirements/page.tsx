import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listRequirements } from "@/services/requirements";
import { CreateRequirementForm } from "./CreateRequirementForm";
import { RequirementFilterForm } from "./RequirementFilterForm";
import { statusBadgeClasses } from "@/lib/badges";

export default async function ProjectRequirementsPage({
  params,
  searchParams,
}: {
  params: Promise<{ projectId: string }>;
  searchParams: Promise<{ q?: string; status?: string; priority?: string }>;
}) {
  const { projectId } = await params;
  const { q, status, priority } = await searchParams;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/requirements`);
  }

  const requirements = await listRequirements(session.accessToken, projectId, { q, status, priority });

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Requirements</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {requirements.length}
        </span>
      </div>

      <CreateRequirementForm projectId={projectId} />

      <div className="mt-8">
        <RequirementFilterForm q={q} status={status} priority={priority} />
      </div>

      <div className="mt-6 overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Key</th>
              <th className="px-4 py-2.5">Title</th>
              <th className="px-4 py-2.5">Status</th>
              <th className="px-4 py-2.5">Priority</th>
            </tr>
          </thead>
          <tbody>
            {requirements.map((requirement) => (
              <tr key={requirement.id} className="border-t border-border hover:bg-surface-sunken">
                <td className="px-4 py-2.5 font-mono text-xs text-muted">{requirement.key}</td>
                <td className="px-4 py-2.5 font-semibold text-text">{requirement.title}</td>
                <td className="px-4 py-2.5">
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(requirement.status)}`}
                  >
                    {requirement.status}
                  </span>
                </td>
                <td className="px-4 py-2.5">
                  {requirement.priority && (
                    <span className="rounded-full bg-status-neutral-soft px-2.5 py-0.5 text-xs font-bold text-status-neutral">
                      {requirement.priority}
                    </span>
                  )}
                </td>
              </tr>
            ))}
            {requirements.length === 0 && (
              <tr>
                <td colSpan={4} className="px-4 py-6 text-center text-faint">
                  None found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
}
