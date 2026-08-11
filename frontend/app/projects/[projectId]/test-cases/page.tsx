import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTestFolders } from "@/services/testfolders";
import { listTestCases } from "@/services/testcases";
import { CreateFolderInlineForm } from "./CreateFolderInlineForm";
import { CreateTestCaseForm } from "./CreateTestCaseForm";
import { TestCaseFilterForm } from "./TestCaseFilterForm";
import { statusBadgeClasses, severityBadgeClasses } from "@/lib/badges";

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

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-cases`);
  }

  const [folders, testCases] = await Promise.all([
    listTestFolders(session.accessToken, projectId),
    listTestCases(session.accessToken, projectId, filters),
  ]);
  const folderNameById = new Map(folders.map((folder) => [folder.id, folder.name]));

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Test Cases</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {testCases.length}
        </span>
      </div>

      {folders.length === 0 ? (
        <CreateFolderInlineForm projectId={projectId} />
      ) : (
        <CreateTestCaseForm projectId={projectId} folders={folders} />
      )}

      <div className="mt-8">
        <TestCaseFilterForm filters={filters} folders={folders} />
      </div>

      <div className="mt-6 overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Key</th>
              <th className="px-4 py-2.5">Title</th>
              <th className="px-4 py-2.5">Folder</th>
              <th className="px-4 py-2.5">Status</th>
              <th className="px-4 py-2.5">Priority</th>
              <th className="px-4 py-2.5">Severity</th>
              <th className="px-4 py-2.5">Steps</th>
            </tr>
          </thead>
          <tbody>
            {testCases.map((testCase) => (
              <tr key={testCase.id} className="border-t border-border hover:bg-surface-sunken">
                <td className="px-4 py-2.5 font-mono text-xs text-muted">{testCase.key}</td>
                <td className="px-4 py-2.5 font-semibold text-text">{testCase.title}</td>
                <td className="px-4 py-2.5 text-muted">{folderNameById.get(testCase.folderId) ?? "-"}</td>
                <td className="px-4 py-2.5">
                  <span
                    className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(testCase.status)}`}
                  >
                    {testCase.status}
                  </span>
                </td>
                <td className="px-4 py-2.5">
                  {testCase.priority && (
                    <span className="rounded-full bg-status-neutral-soft px-2.5 py-0.5 text-xs font-bold text-status-neutral">
                      {testCase.priority}
                    </span>
                  )}
                </td>
                <td className="px-4 py-2.5">
                  {testCase.severity && (
                    <span
                      className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${severityBadgeClasses(testCase.severity)}`}
                    >
                      {testCase.severity}
                    </span>
                  )}
                </td>
                <td className="px-4 py-2.5 text-muted">{testCase.steps.length}</td>
              </tr>
            ))}
            {testCases.length === 0 && (
              <tr>
                <td colSpan={7} className="px-4 py-6 text-center text-faint">
                  None found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
    </main>
  );
}
