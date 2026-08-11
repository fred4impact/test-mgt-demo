import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTestSuites } from "@/services/testsuites";
import { CreateTestSuiteForm } from "./CreateTestSuiteForm";

export default async function ProjectTestSuitesPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-suites`);
  }

  const suites = await listTestSuites(session.accessToken, projectId);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Test suites</h1>
      <CreateTestSuiteForm projectId={projectId} />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing suites</h2>
      <ul className="space-y-1">
        {suites.map((suite) => (
          <li key={suite.id} className="text-sm">
            {suite.name}
          </li>
        ))}
        {suites.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
