const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Build {
  id: string;
  projectId: string;
  releaseId: string;
  name: string;
  version: string | null;
  branch: string | null;
  commitSha: string | null;
  status: string;
  createdAt: string;
}

export async function listBuilds(accessToken: string, projectId: string): Promise<Build[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/builds`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load builds");
  }
  return res.json();
}

export async function createBuild(
  accessToken: string,
  projectId: string,
  input: { name: string; releaseId: string },
): Promise<Build> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/builds`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create build" }));
    throw new Error(error.message ?? "Failed to create build");
  }
  return res.json();
}
