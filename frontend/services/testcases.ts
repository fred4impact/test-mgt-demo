const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface TestStep {
  id: string;
  stepNumber: number;
  action: string;
  testData: string | null;
  expectedResult: string | null;
}

export interface TestCase {
  id: string;
  projectId: string;
  folderId: string;
  key: string;
  title: string;
  priority: string | null;
  severity: string | null;
  status: string;
  testType: string | null;
  automationStatus: string | null;
  ownerId: string;
  createdAt: string;
  steps: TestStep[];
}

export interface CreateTestStepInput {
  action: string;
  testData?: string;
  expectedResult?: string;
}

export interface TestCaseFilters {
  q?: string;
  status?: string;
  priority?: string;
  severity?: string;
  testType?: string;
  automationStatus?: string;
  folderId?: string;
}

export async function listTestCases(
  accessToken: string,
  projectId: string,
  filters: TestCaseFilters = {},
): Promise<TestCase[]> {
  const params = new URLSearchParams();
  if (filters.q) params.set("q", filters.q);
  if (filters.status) params.set("status", filters.status);
  if (filters.priority) params.set("priority", filters.priority);
  if (filters.severity) params.set("severity", filters.severity);
  if (filters.testType) params.set("testType", filters.testType);
  if (filters.automationStatus) params.set("automationStatus", filters.automationStatus);
  if (filters.folderId) params.set("folderId", filters.folderId);
  const query = params.toString();

  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cases${query ? `?${query}` : ""}`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test cases");
  }
  return res.json();
}

export async function createTestCase(
  accessToken: string,
  projectId: string,
  input: { folderId: string; title: string; steps: CreateTestStepInput[] },
): Promise<TestCase> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cases`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create test case" }));
    throw new Error(error.message ?? "Failed to create test case");
  }
  return res.json();
}
