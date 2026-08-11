import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTestPlans } from "@/services/testplans";
import { listReleases } from "@/services/releases";
import { CreateTestPlanForm } from "./CreateTestPlanForm";

export default async function ProjectTestPlansPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-plans`);
  }

  const [releases, testPlans] = await Promise.all([
    listReleases(session.accessToken, projectId),
    listTestPlans(session.accessToken, projectId),
  ]);
  const releaseNameById = new Map(releases.map((release) => [release.id, release.name]));

  return (
    <main className="mx-auto max-w-md p-8">
      <Link href={`/projects/${projectId}`} className="text-sm text-blue-600 hover:underline">
        &larr; Back to project
      </Link>
      <h1 className="mb-4 mt-4 text-xl font-semibold">Test plans</h1>

      {releases.length === 0 ? (
        <p className="text-sm text-gray-500">Create a release first before creating a test plan.</p>
      ) : (
        <CreateTestPlanForm projectId={projectId} releases={releases} />
      )}

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing test plans</h2>
      <ul className="space-y-1">
        {testPlans.map((testPlan) => (
          <li key={testPlan.id} className="text-sm">
            {testPlan.name} <span className="text-gray-400">({testPlan.status})</span>{" "}
            <span className="text-gray-400">- {releaseNameById.get(testPlan.releaseId) ?? "unknown release"}</span>
          </li>
        ))}
        {testPlans.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
