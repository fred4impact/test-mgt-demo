import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listReleases } from "@/services/releases";
import { CreateReleaseForm } from "./CreateReleaseForm";

export default async function ProjectReleasesPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/releases`);
  }

  const releases = await listReleases(session.accessToken, projectId);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Releases</h1>
      <CreateReleaseForm projectId={projectId} />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing releases</h2>
      <ul className="space-y-1">
        {releases.map((release) => (
          <li key={release.id} className="text-sm">
            {release.name} <span className="text-gray-400">({release.status})</span>
          </li>
        ))}
        {releases.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
