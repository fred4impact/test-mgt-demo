import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { getTestCycle, type TestCycle } from "@/services/testcycles";
import { listTestCycleCases } from "@/services/testcyclecases";
import { listTestExecutions } from "@/services/testexecutions";
import { listProjectMembers } from "@/services/projectmembers";
import { listTestFolders } from "@/services/testfolders";
import { listTestCases } from "@/services/testcases";
import { statusBadgeClasses, severityBadgeClasses } from "@/lib/badges";
import { removeTestCycleCaseAction, addTestCycleCaseAction } from "@/actions/testcyclecases";
import { TestCaseFilterForm } from "../../test-cases/TestCaseFilterForm";

export default async function TestCycleDetailPage({
  params,
  searchParams,
}: {
  params: Promise<{ projectId: string; cycleId: string }>;
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
  const { projectId, cycleId } = await params;
  const filters = await searchParams;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-cycles/${cycleId}`);
  }

  let cycle: TestCycle;
  try {
    cycle = await getTestCycle(session.accessToken, projectId, cycleId);
  } catch {
    return (
      <main className="mx-auto max-w-md p-8">
        <Link
          href={`/projects/${projectId}/test-cycles`}
          className="text-sm font-semibold text-muted hover:text-accent"
        >
          &larr; Back to test cycles
        </Link>
        <p className="mt-4 text-sm text-faint">Test cycle not found.</p>
      </main>
    );
  }

  const [selectedCases, executions, members, folders, testCases] = await Promise.all([
    listTestCycleCases(session.accessToken, projectId, cycleId),
    listTestExecutions(session.accessToken, projectId, cycleId),
    listProjectMembers(session.accessToken, projectId),
    listTestFolders(session.accessToken, projectId),
    listTestCases(session.accessToken, projectId, filters),
  ]);

  const memberNameById = new Map(members.map((member) => [member.userId, `${member.firstName} ${member.lastName}`]));
  const selectedTestCaseIds = new Set(selectedCases.map((selectedCase) => selectedCase.testCaseId));
  const executionByTestCaseId = new Map(executions.map((execution) => [execution.testCaseId, execution]));

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link
        href={`/projects/${projectId}/test-cycles`}
        className="text-sm font-semibold text-muted hover:text-accent"
      >
        &larr; Back to test cycles
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">{cycle.name}</h1>
        <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(cycle.status)}`}>
          {cycle.status}
        </span>
      </div>

      <h2 className="mb-2 text-xs font-bold uppercase tracking-wide text-muted">Selected test cases</h2>
      <div className="overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Key</th>
              <th className="px-4 py-2.5">Title</th>
              <th className="px-4 py-2.5">Assignee</th>
              <th className="px-4 py-2.5">Execution</th>
              <th className="px-4 py-2.5" />
            </tr>
          </thead>
          <tbody>
            {selectedCases.map((selectedCase) => {
              const execution = executionByTestCaseId.get(selectedCase.testCaseId);
              const executionStatus = execution?.status ?? "NOT_RUN";
              return (
                <tr key={selectedCase.testCaseId} className="border-t border-border hover:bg-surface-sunken">
                  <td className="px-4 py-2.5 font-mono text-xs text-muted">{selectedCase.key}</td>
                  <td className="px-4 py-2.5 font-semibold text-text">{selectedCase.title}</td>
                  <td className="px-4 py-2.5 text-muted">
                    {selectedCase.assigneeId ? (memberNameById.get(selectedCase.assigneeId) ?? "Unknown") : "Unassigned"}
                  </td>
                  <td className="px-4 py-2.5">
                    <Link href={`/projects/${projectId}/test-cycles/${cycleId}/executions/${selectedCase.testCaseId}`}>
                      <span
                        className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(executionStatus)}`}
                      >
                        {executionStatus}
                      </span>
                    </Link>
                  </td>
                  <td className="px-4 py-2.5 text-right">
                    <form action={removeTestCycleCaseAction.bind(null, projectId, cycleId, selectedCase.testCaseId)}>
                      <button type="submit" className="text-sm font-semibold text-status-danger hover:underline">
                        Remove
                      </button>
                    </form>
                  </td>
                </tr>
              );
            })}
            {selectedCases.length === 0 && (
              <tr>
                <td colSpan={5} className="px-4 py-6 text-center text-faint">
                  No test cases selected yet.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      <h2 className="mb-2 mt-8 text-xs font-bold uppercase tracking-wide text-muted">Add test cases</h2>
      <TestCaseFilterForm filters={filters} folders={folders} />

      <div className="mt-6 overflow-hidden rounded-xl border border-border bg-surface shadow-card">
        <table className="w-full border-collapse text-sm">
          <thead>
            <tr className="bg-surface-sunken text-left text-xs font-bold uppercase tracking-wide text-muted">
              <th className="px-4 py-2.5">Key</th>
              <th className="px-4 py-2.5">Title</th>
              <th className="px-4 py-2.5">Status</th>
              <th className="px-4 py-2.5">Priority</th>
              <th className="px-4 py-2.5">Severity</th>
              <th className="px-4 py-2.5">Assign to</th>
              <th className="px-4 py-2.5" />
            </tr>
          </thead>
          <tbody>
            {testCases.map((testCase) => {
              const alreadyAdded = selectedTestCaseIds.has(testCase.id);
              return (
                <tr key={testCase.id} className="border-t border-border hover:bg-surface-sunken">
                  <td className="px-4 py-2.5 font-mono text-xs text-muted">{testCase.key}</td>
                  <td className="px-4 py-2.5 font-semibold text-text">{testCase.title}</td>
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
                  {alreadyAdded ? (
                    <td colSpan={2} className="px-4 py-2.5 text-right text-xs font-bold text-muted">
                      Added
                    </td>
                  ) : (
                    <td colSpan={2} className="px-4 py-2.5">
                      <form
                        action={addTestCycleCaseAction.bind(null, projectId, cycleId)}
                        className="flex items-center justify-end gap-2"
                      >
                        <input type="hidden" name="testCaseId" value={testCase.id} />
                        <select
                          name="assigneeId"
                          className="rounded-md border border-border bg-bg px-2 py-1 text-xs text-text"
                          defaultValue=""
                        >
                          <option value="">Unassigned</option>
                          {members.map((member) => (
                            <option key={member.userId} value={member.userId}>
                              {member.firstName} {member.lastName}
                            </option>
                          ))}
                        </select>
                        <button
                          type="submit"
                          className="rounded-md bg-accent px-3 py-1 text-xs font-bold text-accent-ink hover:bg-accent-hover"
                        >
                          + Add
                        </button>
                      </form>
                    </td>
                  )}
                </tr>
              );
            })}
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
