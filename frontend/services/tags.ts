const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Tag {
  id: string;
  projectId: string;
  name: string;
  createdAt: string;
}

export async function listTags(accessToken: string, projectId: string): Promise<Tag[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/tags`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load tags");
  }
  return res.json();
}

export async function createTag(accessToken: string, projectId: string, input: { name: string }): Promise<Tag> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/tags`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create tag" }));
    throw new Error(error.message ?? "Failed to create tag");
  }
  return res.json();
}
