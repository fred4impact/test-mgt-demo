const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Role {
  id: string;
  organizationId: string;
  name: string;
  systemRole: boolean;
  createdAt: string;
}

export async function listRoles(accessToken: string): Promise<Role[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/roles`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load roles");
  }
  return res.json();
}

export async function createRole(
  accessToken: string,
  input: { name: string; systemRole: boolean },
): Promise<Role> {
  const res = await fetch(`${BACKEND_URL}/api/v1/roles`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create role" }));
    throw new Error(error.message ?? "Failed to create role");
  }
  return res.json();
}
