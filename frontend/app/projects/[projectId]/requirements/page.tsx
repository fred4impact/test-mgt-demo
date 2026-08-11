import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listRequirements } from "@/services/requirements";
import { CreateRequirementForm } from "./CreateRequirementForm";
import { RequirementFilterForm } from "./RequirementFilterForm";

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
    <main className="mx-auto max-w-md p-8">
      <Link href={`/projects/${projectId}`} className="text-sm text-blue-600 hover:underline">
        &larr; Back to project
      </Link>
      <h1 className="mb-4 mt-4 text-xl font-semibold">Requirements</h1>
      <CreateRequirementForm projectId={projectId} />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Search &amp; filter</h2>
      <RequirementFilterForm q={q} status={status} priority={priority} />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing requirements</h2>
      <ul className="space-y-1">
        {requirements.map((requirement) => (
          <li key={requirement.id} className="text-sm">
            <span className="text-gray-400">{requirement.key}</span> {requirement.title}
          </li>
        ))}
        {requirements.length === 0 && <li className="text-sm text-gray-400">None found.</li>}
      </ul>
    </main>
  );
}
