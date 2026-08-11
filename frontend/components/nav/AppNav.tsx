import Link from "next/link";
import { auth, signOut } from "@/auth";

export async function AppNav() {
  const session = await auth();

  if (!session?.accessToken) {
    return (
      <nav className="flex w-full justify-end gap-4 border-b border-gray-200 px-8 py-3 text-sm">
        <Link href="/api/auth/signin?callbackUrl=/home" className="text-gray-600 hover:underline">
          Sign in
        </Link>
      </nav>
    );
  }

  return (
    <nav className="flex w-full items-center justify-between border-b border-gray-200 px-8 py-3 text-sm">
      <Link href="/home" className="font-semibold text-black">
        Test Management Platform
      </Link>
      <div className="flex items-center gap-4">
        <Link href="/teams/new" className="text-gray-600 hover:underline">
          Teams
        </Link>
        <Link href="/roles/new" className="text-gray-600 hover:underline">
          Roles
        </Link>
        <Link href="/profile" className="text-gray-600 hover:underline">
          Profile
        </Link>
        <form
          action={async () => {
            "use server";
            await signOut({ redirectTo: "/" });
          }}
        >
          <button type="submit" className="text-gray-600 hover:underline">
            Sign out
          </button>
        </form>
      </div>
    </nav>
  );
}
