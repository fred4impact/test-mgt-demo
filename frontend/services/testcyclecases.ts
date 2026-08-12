const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface TestCycleCase {
  testCaseId: string;
  key: string;
  title: string;
  assigneeId: string | null;
  sortOrder: number;
  addedAt: string;
}

export async function listTestCycleCases(
  accessToken: string,
  projectId: string,
  cycleId: string,
): Promise<TestCycleCase[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles/${cycleId}/cases`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test cycle cases");
  }
  return res.json();
}

export async function addTestCycleCase(
  accessToken: string,
  projectId: string,
  cycleId: string,
  input: { testCaseId: string; assigneeId?: string },
): Promise<TestCycleCase> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles/${cycleId}/cases`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to add test case" }));
    throw new Error(error.message ?? "Failed to add test case");
  }
  return res.json();
}

export async function removeTestCycleCase(
  accessToken: string,
  projectId: string,
  cycleId: string,
  testCaseId: string,
): Promise<void> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles/${cycleId}/cases/${testCaseId}`, {
    method: "DELETE",
    headers: { Authorization: `Bearer ${accessToken}` },
  });
  if (!res.ok) {
    throw new Error("Failed to remove test case");
  }
}
