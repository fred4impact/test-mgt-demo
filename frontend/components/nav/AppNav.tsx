import Link from "next/link";
import { auth, signOut } from "@/auth";

export async function AppNav() {
  const session = await auth();

  if (!session?.accessToken) {
    return (
      <nav className="flex w-full justify-end bg-surface px-12 py-4 text-sm shadow-nav">
        <Link href="/api/auth/signin?callbackUrl=/home" className="font-semibold text-muted hover:text-accent">
          Sign in
        </Link>
      </nav>
    );
  }

  return (
    <nav className="flex w-full items-center justify-between bg-surface px-12 py-4 text-sm shadow-nav">
      <Link href="/home" className="text-base font-extrabold tracking-tight text-text">
        Test<span className="text-accent">MGMT</span>
      </Link>
      <div className="flex items-center gap-7">
        <Link href="/teams/new" className="font-semibold text-muted hover:text-accent">
          Teams
        </Link>
        <Link href="/roles/new" className="font-semibold text-muted hover:text-accent">
          Roles
        </Link>
        <Link href="/profile" className="font-semibold text-muted hover:text-accent">
          Profile
        </Link>
        <form
          action={async () => {
            "use server";
            await signOut({ redirectTo: "/" });
          }}
        >
          <button type="submit" className="font-semibold text-muted hover:text-accent">
            Sign out
          </button>
        </form>
      </div>
    </nav>
  );
}
