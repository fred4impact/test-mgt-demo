const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export type TestExecutionStatus =
  | "NOT_RUN"
  | "IN_PROGRESS"
  | "PASSED"
  | "FAILED"
  | "BLOCKED"
  | "SKIPPED"
  | "NOT_APPLICABLE";

export interface TestExecution {
  id: string;
  projectId: string;
  cycleId: string;
  testCaseId: string;
  assigneeId: string | null;
  environmentId: string;
  buildId: string;
  status: TestExecutionStatus;
  startedAt: string | null;
  completedAt: string | null;
  durationMs: number | null;
  actualResult: string | null;
  comment: string | null;
  createdAt: string;
  updatedAt: string;
}

export async function listTestExecutions(
  accessToken: string,
  projectId: string,
  cycleId: string,
): Promise<TestExecution[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles/${cycleId}/executions`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test executions");
  }
  return res.json();
}

export async function getTestExecution(
  accessToken: string,
  projectId: string,
  cycleId: string,
  testCaseId: string,
): Promise<TestExecution> {
  const res = await fetch(
    `${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles/${cycleId}/executions/${testCaseId}`,
    {
      headers: { Authorization: `Bearer ${accessToken}` },
      cache: "no-store",
    },
  );
  if (!res.ok) {
    throw new Error("Failed to load test execution");
  }
  return res.json();
}

export async function updateTestExecution(
  accessToken: string,
  projectId: string,
  cycleId: string,
  testCaseId: string,
  input: { status: TestExecutionStatus; actualResult?: string; comment?: string },
): Promise<TestExecution> {
  const res = await fetch(
    `${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles/${cycleId}/executions/${testCaseId}`,
    {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${accessToken}`,
      },
      body: JSON.stringify(input),
    },
  );
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to update test execution" }));
    throw new Error(error.message ?? "Failed to update test execution");
  }
  return res.json();
}
