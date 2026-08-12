import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { getTestExecution, type TestExecution } from "@/services/testexecutions";
import { getTestCase, type TestCase } from "@/services/testcases";
import { statusBadgeClasses } from "@/lib/badges";
import { updateTestExecutionAction } from "@/actions/testexecutions";

const STATUSES = ["NOT_RUN", "IN_PROGRESS", "PASSED", "FAILED", "BLOCKED", "SKIPPED", "NOT_APPLICABLE"];

export default async function TestExecutionPage({
  params,
}: {
  params: Promise<{ projectId: string; cycleId: string; testCaseId: string }>;
}) {
  const { projectId, cycleId, testCaseId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(
      `/api/auth/signin?callbackUrl=/projects/${projectId}/test-cycles/${cycleId}/executions/${testCaseId}`,
    );
  }

  let execution: TestExecution;
  let testCase: TestCase;
  try {
    [execution, testCase] = await Promise.all([
      getTestExecution(session.accessToken, projectId, cycleId, testCaseId),
      getTestCase(session.accessToken, projectId, testCaseId),
    ]);
  } catch {
    return (
      <main className="mx-auto max-w-md p-8">
        <Link
          href={`/projects/${projectId}/test-cycles/${cycleId}`}
          className="text-sm font-semibold text-muted hover:text-accent"
        >
          &larr; Back to cycle
        </Link>
        <p className="mt-4 text-sm text-faint">Test execution not found.</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-2xl px-12 py-10">
      <Link
        href={`/projects/${projectId}/test-cycles/${cycleId}`}
        className="text-sm font-semibold text-muted hover:text-accent"
      >
        &larr; Back to cycle
      </Link>

      <div className="mb-1 mt-4 flex items-center gap-3">
        <span className="font-mono text-xs text-muted">{testCase.key}</span>
        <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(execution.status)}`}>
          {execution.status}
        </span>
      </div>
      <h1 className="mb-6 text-3xl font-extrabold tracking-tight text-text">{testCase.title}</h1>

      <form
        action={updateTestExecutionAction.bind(null, projectId, cycleId, testCaseId)}
        className="space-y-4 rounded-xl border border-border bg-surface p-6 shadow-card"
      >
        <div>
          <label className="mb-1 block text-xs font-bold uppercase tracking-wide text-muted" htmlFor="status">
            Status
          </label>
          <select
            id="status"
            name="status"
            defaultValue={execution.status}
            className="w-full rounded-md border border-border bg-bg px-3 py-2 text-sm text-text"
          >
            {STATUSES.map((status) => (
              <option key={status} value={status}>
                {status}
              </option>
            ))}
          </select>
        </div>

        <div>
          <label className="mb-1 block text-xs font-bold uppercase tracking-wide text-muted" htmlFor="actualResult">
            Actual result
          </label>
          <textarea
            id="actualResult"
            name="actualResult"
            rows={4}
            defaultValue={execution.actualResult ?? ""}
            className="w-full rounded-md border border-border bg-bg px-3 py-2 text-sm text-text"
          />
        </div>

        <div>
          <label className="mb-1 block text-xs font-bold uppercase tracking-wide text-muted" htmlFor="comment">
            Comment
          </label>
          <textarea
            id="comment"
            name="comment"
            rows={3}
            defaultValue={execution.comment ?? ""}
            className="w-full rounded-md border border-border bg-bg px-3 py-2 text-sm text-text"
          />
        </div>

        <button
          type="submit"
          className="rounded-md bg-accent px-4 py-2 text-sm font-bold text-accent-ink hover:bg-accent-hover"
        >
          Save
        </button>
      </form>
    </main>
  );
}
