const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface ProjectMember {
  userId: string;
  email: string;
  firstName: string;
  lastName: string;
  roleId: string;
  roleName: string;
  joinedAt: string;
}

export async function listProjectMembers(accessToken: string, projectId: string): Promise<ProjectMember[]> {
  const res = await fetch(`${BACKEND_URL}/api/v1/projects/${projectId}/members`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load project members");
  }
  return res.json();
}
