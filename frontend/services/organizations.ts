const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Organization {
  id: string;
  name: string;
  slug: string;
  status: string;
  createdAt: string;
}

export async function listOrganizations(): Promise<Organization[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/organizations`, { cache: "no-store" });
  if (!res.ok) {
    throw new Error("Failed to load organizations");
  }
  return res.json();
}

export async function createOrganization(input: {
  name: string;
  slug: string;
}): Promise<Organization> {
  const res = await fetch(`${BACKEND_URL}/api/v1/organizations`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create organization" }));
    throw new Error(error.message ?? "Failed to create organization");
  }
  return res.json();
}
