const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

export interface Me {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  organizationName: string;
}

export async function fetchMe(accessToken: string): Promise<Me> {
  const res = await fetch(`${BACKEND_URL}/api/v1/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load profile");
  }
  return res.json();
}
