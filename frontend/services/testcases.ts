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

export async function listTestCases(accessToken: string, projectId: string): Promise<TestCase[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-cases`, {
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
