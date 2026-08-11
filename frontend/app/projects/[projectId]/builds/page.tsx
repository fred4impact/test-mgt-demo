import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listBuilds } from "@/services/builds";
import { listReleases } from "@/services/releases";
import { CreateBuildForm } from "./CreateBuildForm";
import { statusBadgeClasses } from "@/lib/badges";

export default async function ProjectBuildsPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/builds`);
  }

  const [releases, builds] = await Promise.all([
    listReleases(session.accessToken, projectId),
    listBuilds(session.accessToken, projectId),
  ]);
  const releaseNameById = new Map(releases.map((release) => [release.id, release.name]));

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Builds</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {builds.length}
        </span>
      </div>

      {releases.length === 0 ? (
        <p className="text-sm text-muted">Create a release first before recording a build.</p>
      ) : (
        <CreateBuildForm projectId={projectId} releases={releases} />
      )}

      <div className="mt-6 overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Name</th>
              <th className="px-4 py-2.5">Release</th>
              <th className="px-4 py-2.5">Version</th>
              <th className="px-4 py-2.5">Branch</th>
              <th className="px-4 py-2.5">Commit</th>
              <th className="px-4 py-2.5">Status</th>
            </tr>
          </thead>
          <tbody>
            {builds.map((build) => (
              <tr key={build.id} className="border-t border-border hover:bg-surface-sunken">
                <td className="px-4 py-2.5 font-semibold text-text">{build.name}</td>
                <td className="px-4 py-2.5 text-muted">{releaseNameById.get(build.releaseId) ?? "unknown release"}</td>
                <td className="px-4 py-2.5 text-muted">{build.version ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{build.branch ?? "-"}</td>
                <td className="px-4 py-2.5 font-mono text-xs text-muted">{build.commitSha ?? "-"}</td>
                <td className="px-4 py-2.5">
                  <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(build.status)}`}>
                    {build.status}
                  </span>
                </td>
              </tr>
            ))}
            {builds.length === 0 && (
              <tr>
                <td colSpan={6} className="px-4 py-6 text-center text-faint">
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
