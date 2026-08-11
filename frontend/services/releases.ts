const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Release {
  id: string;
  projectId: string;
  name: string;
  version: string | null;
  status: string;
  startDate: string | null;
  releaseDate: string | null;
  createdAt: string;
}

export async function listReleases(accessToken: string, projectId: string): Promise<Release[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/releases`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load releases");
  }
  return res.json();
}

export async function createRelease(
  accessToken: string,
  projectId: string,
  input: { name: string },
): Promise<Release> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/releases`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create release" }));
    throw new Error(error.message ?? "Failed to create release");
  }
  return res.json();
}
