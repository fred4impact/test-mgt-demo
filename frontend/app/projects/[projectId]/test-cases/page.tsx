import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTestFolders } from "@/services/testfolders";
import { listTestCases } from "@/services/testcases";
import { CreateFolderInlineForm } from "./CreateFolderInlineForm";
import { CreateTestCaseForm } from "./CreateTestCaseForm";
import { TestCaseFilterForm } from "./TestCaseFilterForm";

export default async function ProjectTestCasesPage({
  params,
  searchParams,
}: {
  params: Promise<{ projectId: string }>;
  searchParams: Promise<{
    q?: string;
    status?: string;
    priority?: string;
    severity?: string;
    testType?: string;
    automationStatus?: string;
    folderId?: string;
  }>;
}) {
  const { projectId } = await params;
  const filters = await searchParams;
  const session = await auth();

  if (!session?.accessToken) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-cases`);
  }

  const [folders, testCases] = await Promise.all([
    listTestFolders(session.accessToken, projectId),
    listTestCases(session.accessToken, projectId, filters),
  ]);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Test cases</h1>

      {folders.length === 0 ? (
        <CreateFolderInlineForm projectId={projectId} />
      ) : (
        <CreateTestCaseForm projectId={projectId} folders={folders} />
      )}

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Search &amp; filter</h2>
      <TestCaseFilterForm filters={filters} folders={folders} />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing test cases</h2>
      <ul className="space-y-1">
        {testCases.map((testCase) => (
          <li key={testCase.id} className="text-sm">
            <span className="text-gray-400">{testCase.key}</span> {testCase.title}{" "}
            <span className="text-gray-400">({testCase.steps.length} steps)</span>
          </li>
        ))}
        {testCases.length === 0 && <li className="text-sm text-gray-400">None found.</li>}
      </ul>
    </main>
  );
}
