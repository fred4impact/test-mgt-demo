const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Requirement {
  id: string;
  projectId: string;
  key: string;
  title: string;
  status: string;
  priority: string | null;
  ownerId: string;
  createdAt: string;
}

export interface RequirementFilters {
  q?: string;
  status?: string;
  priority?: string;
}

export async function listRequirements(
  accessToken: string,
  projectId: string,
  filters: RequirementFilters = {},
): Promise<Requirement[]> {
  const params = new URLSearchParams();
  if (filters.q) params.set("q", filters.q);
  if (filters.status) params.set("status", filters.status);
  if (filters.priority) params.set("priority", filters.priority);
  const query = params.toString();

  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/requirements${query ? `?${query}` : ""}`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load requirements");
  }
  return res.json();
}

export async function createRequirement(
  accessToken: string,
  projectId: string,
  input: { title: string },
): Promise<Requirement> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/requirements`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create requirement" }));
    throw new Error(error.message ?? "Failed to create requirement");
  }
  return res.json();
}
