import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listReleases } from "@/services/releases";
import { CreateReleaseForm } from "./CreateReleaseForm";
import { statusBadgeClasses } from "@/lib/badges";

export default async function ProjectReleasesPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/releases`);
  }

  const releases = await listReleases(session.accessToken, projectId);

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Releases</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {releases.length}
        </span>
      </div>

      <CreateReleaseForm projectId={projectId} />

      <div className="mt-6 overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Name</th>
              <th className="px-4 py-2.5">Version</th>
              <th className="px-4 py-2.5">Status</th>
              <th className="px-4 py-2.5">Start Date</th>
              <th className="px-4 py-2.5">Release Date</th>
            </tr>
          </thead>
          <tbody>
            {releases.map((release) => (
              <tr key={release.id} className="border-t border-border hover:bg-surface-sunken">
                <td className="px-4 py-2.5 font-semibold text-text">{release.name}</td>
                <td className="px-4 py-2.5 text-muted">{release.version ?? "-"}</td>
                <td className="px-4 py-2.5">
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(release.status)}`}
                  >
                    {release.status}
                  </span>
                </td>
                <td className="px-4 py-2.5 text-muted">{release.startDate ?? "-"}</td>
                <td className="px-4 py-2.5 text-muted">{release.releaseDate ?? "-"}</td>
              </tr>
            ))}
            {releases.length === 0 && (
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
