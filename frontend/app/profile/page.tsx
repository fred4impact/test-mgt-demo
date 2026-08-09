import { redirect } from "next/navigation";
import { auth, signOut } from "@/auth";

interface MeResponse {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  organizationId: string;
  organizationName: string;
}

const BACKEND_URL = process.env.BACKEND_URL ?? "http://localhost:8080";

async function fetchMe(accessToken: string): Promise<MeResponse> {
  const res = await fetch(`${BACKEND_URL}/api/v1/me`, {
    headers: { Authorization: `Bearer ${accessToken}` },
    cache: "no-store",
  });
  if (!res.ok) {
    throw new Error("Failed to load profile");
  }
  return res.json();
}

export default async function ProfilePage() {
  const session = await auth();

  if (!session?.accessToken) {
    redirect("/api/auth/signin?callbackUrl=/profile");
  }

  const me = await fetchMe(session.accessToken);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Profile</h1>
      <dl className="space-y-3 text-sm">
        <div>
          <dt className="text-gray-500">Name</dt>
          <dd>
            {me.firstName} {me.lastName}
          </dd>
        </div>
        <div>
          <dt className="text-gray-500">Email</dt>
          <dd>{me.email}</dd>
        </div>
        <div>
          <dt className="text-gray-500">Organization</dt>
          <dd>{me.organizationName}</dd>
        </div>
      </dl>

      <form
        action={async () => {
          "use server";
          await signOut({ redirectTo: "/" });
        }}
        className="mt-6"
      >
        <button type="submit" className="rounded bg-black px-3 py-1.5 text-sm text-white">
          Sign out
        </button>
      </form>
    </main>
  );
}
