import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listEnvironments } from "@/services/environments";
import { CreateEnvironmentForm } from "./CreateEnvironmentForm";

export default async function ProjectEnvironmentsPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/environments`);
  }

  const environments = await listEnvironments(session.accessToken, projectId);

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Environments</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {environments.length}
        </span>
      </div>

      <CreateEnvironmentForm projectId={projectId} />

      <div className="mt-6 overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Name</th>
              <th className="px-4 py-2.5">Type</th>
              <th className="px-4 py-2.5">URL</th>
            </tr>
          </thead>
          <tbody>
            {environments.map((environment) => (
              <tr key={environment.id} className="border-t border-border hover:bg-surface-sunken">
                <td className="px-4 py-2.5 font-semibold text-text">{environment.name}</td>
                <td className="px-4 py-2.5 text-muted">{environment.type ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{environment.url ?? "-"}</td>
              </tr>
            ))}
            {environments.length === 0 && (
              <tr>
                <td colSpan={3} className="px-4 py-6 text-center text-faint">
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
