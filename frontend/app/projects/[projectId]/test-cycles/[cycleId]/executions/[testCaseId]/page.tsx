import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { getTestExecution, listExecutionSteps, type TestExecution, type ExecutionStep } from "@/services/testexecutions";
import { getTestCase, type TestCase } from "@/services/testcases";
import { statusBadgeClasses } from "@/lib/badges";
import { updateTestExecutionAction, updateExecutionStepAction } from "@/actions/testexecutions";

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
  let steps: ExecutionStep[];
  try {
    [execution, testCase, steps] = await Promise.all([
      getTestExecution(session.accessToken, projectId, cycleId, testCaseId),
      getTestCase(session.accessToken, projectId, testCaseId),
      listExecutionSteps(session.accessToken, projectId, cycleId, testCaseId),
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

      <h2 className="mb-2 mt-8 text-xs font-bold uppercase tracking-wide text-muted">Steps</h2>
      <div className="space-y-4">
        {steps.map((step) => {
          const testStep = testCase.steps.find((s) => s.id === step.testStepId);
          return (
            <div key={step.id} className="rounded-xl border border-border bg-surface p-4 shadow-card">
              <div className="mb-2 flex items-center gap-3">
                <span className="text-xs font-bold text-muted">Step {step.stepNumber}</span>
                <span className={`rounded-full px-2.5 py-0.5 text-xs font-bold ${statusBadgeClasses(step.status)}`}>
                  {step.status}
                </span>
              </div>
              <p className="mb-1 text-sm font-semibold text-text">{testStep?.action}</p>
              {testStep?.testData && <p className="mb-1 text-xs text-muted">Test data: {testStep.testData}</p>}
              {testStep?.expectedResult && (
                <p className="mb-3 text-xs text-muted">Expected: {testStep.expectedResult}</p>
              )}

              <form
                action={updateExecutionStepAction.bind(null, projectId, cycleId, testCaseId, step.testStepId)}
                className="grid grid-cols-1 gap-3 sm:grid-cols-[160px_1fr_1fr_auto] sm:items-start"
              >
                <select
                  name="status"
                  defaultValue={step.status}
                  className="rounded-md border border-border bg-bg px-2 py-1.5 text-xs text-text"
                >
                  {STATUSES.map((status) => (
                    <option key={status} value={status}>
                      {status}
                    </option>
                  ))}
                </select>
                <textarea
                  name="actualResult"
                  placeholder="Actual result"
                  rows={2}
                  defaultValue={step.actualResult ?? ""}
                  className="rounded-md border border-border bg-bg px-2 py-1.5 text-xs text-text"
                />
                <textarea
                  name="comment"
                  placeholder="Comment"
                  rows={2}
                  defaultValue={step.comment ?? ""}
                  className="rounded-md border border-border bg-bg px-2 py-1.5 text-xs text-text"
                />
                <button
                  type="submit"
                  className="h-fit rounded-md bg-accent px-3 py-1.5 text-xs font-bold text-accent-ink hover:bg-accent-hover"
                >
                  Save
                </button>
              </form>
            </div>
          );
        })}
        {steps.length === 0 && <p className="text-sm text-faint">This test case has no steps.</p>}
      </div>
    </main>
  );
}
