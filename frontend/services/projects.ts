const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Project {
  id: string;
  organizationId: string;
  key: string;
  name: string;
  status: string;
  ownerId: string;
  createdAt: string;
}

export async function listProjects(accessToken: string): Promise<Project[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load projects");
  }
  return res.json();
}

export async function createProject(
  accessToken: string,
  input: { key: string; name: string },
): Promise<Project> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create project" }));
    throw new Error(error.message ?? "Failed to create project");
  }
  return res.json();
}
