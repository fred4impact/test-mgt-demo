import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listRequirements } from "@/services/requirements";
import { CreateRequirementForm } from "./CreateRequirementForm";

export default async function ProjectRequirementsPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/requirements`);
  }

  const requirements = await listRequirements(session.accessToken, projectId);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Requirements</h1>
      <CreateRequirementForm projectId={projectId} />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing requirements</h2>
      <ul className="space-y-1">
        {requirements.map((requirement) => (
          <li key={requirement.id} className="text-sm">
            <span className="text-gray-400">{requirement.key}</span> {requirement.title}
          </li>
        ))}
        {requirements.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
