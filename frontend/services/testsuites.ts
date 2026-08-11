const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface TestSuite {
  id: string;
  projectId: string;
  parentId: string | null;
  name: string;
  createdAt: string;
}

export async function listTestSuites(accessToken: string, projectId: string): Promise<TestSuite[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-suites`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test suites");
  }
  return res.json();
}

export async function createTestSuite(
  accessToken: string,
  projectId: string,
  input: { name: string },
): Promise<TestSuite> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-suites`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create test suite" }));
    throw new Error(error.message ?? "Failed to create test suite");
  }
  return res.json();
}
