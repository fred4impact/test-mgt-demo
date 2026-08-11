import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listBuilds } from "@/services/builds";
import { listReleases } from "@/services/releases";
import { CreateBuildForm } from "./CreateBuildForm";

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
    <main className="mx-auto max-w-md p-8">
      <Link href={`/projects/${projectId}`} className="text-sm text-blue-600 hover:underline">
        &larr; Back to project
      </Link>
      <h1 className="mb-4 mt-4 text-xl font-semibold">Builds</h1>

      {releases.length === 0 ? (
        <p className="text-sm text-gray-500">Create a release first before recording a build.</p>
      ) : (
        <CreateBuildForm projectId={projectId} releases={releases} />
      )}

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing builds</h2>
      <ul className="space-y-1">
        {builds.map((build) => (
          <li key={build.id} className="text-sm">
            {build.name} <span className="text-gray-400">({build.status})</span>{" "}
            <span className="text-gray-400">- {releaseNameById.get(build.releaseId) ?? "unknown release"}</span>
          </li>
        ))}
        {builds.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
