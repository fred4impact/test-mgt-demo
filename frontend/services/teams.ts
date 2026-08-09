const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Team {
  id: string;
  organizationId: string;
  name: string;
  description: string | null;
  createdAt: string;
}

export async function listTeams(accessToken: string): Promise<Team[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/teams`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load teams");
  }
  return res.json();
}

export async function createTeam(
  accessToken: string,
  input: { name: string; description: string },
): Promise<Team> {
  const res = await fetch(`${BACKEND_URL}/api/v1/teams`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      Authorization: `Bearer ${accessToken}`,
    },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const error = await res.json().catch(() => ({ message: "Failed to create team" }));
    throw new Error(error.message ?? "Failed to create team");
  }
  return res.json();
}
