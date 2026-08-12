const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface TestCycle {
  id: string;
  projectId: string;
  testPlanId: string;
  releaseId: string;
  buildId: string;
  environmentId: string;
  name: string;
  status: string;
  ownerId: string;
  startDate: string | null;
  endDate: string | null;
  createdAt: string;
}

export async function listTestCycles(accessToken: string, projectId: string): Promise<TestCycle[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test cycles");
  }
  return res.json();
}

export async function getTestCycle(accessToken: string, projectId: string, cycleId: string): Promise<TestCycle> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles/${cycleId}`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test cycle");
  }
  return res.json();
}

export async function createTestCycle(
  accessToken: string,
  projectId: string,
  input: { name: string; testPlanId: string; releaseId: string; buildId: string; environmentId: string },
): Promise<TestCycle> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cycles`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create test cycle" }));
    throw new Error(error.message ?? "Failed to create test cycle");
  }
  return res.json();
}
