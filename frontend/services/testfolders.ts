const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface TestFolder {
  id: string;
  projectId: string;
  parentId: string | null;
  name: string;
  createdAt: string;
}

export async function listTestFolders(accessToken: string, projectId: string): Promise<TestFolder[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-folders`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load test folders");
  }
  return res.json();
}

export async function createTestFolder(
  accessToken: string,
  projectId: string,
  input: { name: string },
): Promise<TestFolder> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/test-folders`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create test folder" }));
    throw new Error(error.message ?? "Failed to create test folder");
  }
  return res.json();
}
