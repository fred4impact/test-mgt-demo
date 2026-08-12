const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Environment {
  id: string;
  projectId: string;
  name: string;
  type: string | null;
  url: string | null;
  createdAt: string;
}

export async function listEnvironments(accessToken: string, projectId: string): Promise<Environment[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/environments`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load environments");
  }
  return res.json();
}

export async function createEnvironment(
  accessToken: string,
  projectId: string,
  input: { name: string; type?: string; url?: string },
): Promise<Environment> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/environments`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create environment" }));
    throw new Error(error.message ?? "Failed to create environment");
  }
  return res.json();
}
