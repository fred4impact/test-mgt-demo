const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface TestPlan {
  id: string;
  projectId: string;
  releaseId: string;
  name: string;
  status: string;
  ownerId: string;
  startDate: string | null;
  endDate: string | null;
  createdAt: string;
}

export async function listTestPlans(accessToken: string, projectId: string): Promise<TestPlan[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-plans`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test plans");
  }
  return res.json();
}

export async function createTestPlan(
  accessToken: string,
  projectId: string,
  input: { name: string; releaseId: string },
): Promise<TestPlan> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-plans`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create test plan" }));
    throw new Error(error.message ?? "Failed to create test plan");
  }
  return res.json();
}
