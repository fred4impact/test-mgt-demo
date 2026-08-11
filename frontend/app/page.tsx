import { redirect } from "next/navigation";
import { auth } from "@/auth";

export default async function Home() {
  const session = await auth();

  if (session?.accessToken) {
    redirect("/home");
  }

  return (
    <main className="flex flex-1 flex-col items-center justify-center bg-bg px-16 py-32">
      <div className="flex max-w-2xl flex-col items-center gap-6 text-center">
        <h1 className="text-5xl font-extrabold tracking-tight text-text">
          Test <span className="text-accent">Management</span> Platform
        </h1>
        <p className="max-w-md text-lg text-muted">
          Requirements, test cases, and execution tracking for your organization.
        </p>
        <a
          href="/api/auth/signin?callbackUrl=/home"
          className="rounded-lg bg-accent px-6 py-3 text-sm font-bold text-accent-ink hover:bg-accent-hover"
        >
          Sign in
        </a>
      </div>
    </main>
  );
}
