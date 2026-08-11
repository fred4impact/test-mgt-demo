import { redirect } from "next/navigation";
import { auth } from "@/auth";

export default async function Home() {
  const session = await auth();

  if (session?.accessToken) {
    redirect("/home");
  }

  return (
    <main className="flex flex-1 flex-col items-center justify-center bg-zinc-50 px-16 py-32 dark:bg-black">
      <div className="flex max-w-md flex-col items-center gap-6 text-center">
        <h1 className="text-3xl font-semibold tracking-tight text-black dark:text-zinc-50">
          Test Management Platform
        </h1>
        <p className="text-zinc-600 dark:text-zinc-400">
          Requirements, test cases, and execution tracking for your organization.
        </p>
        <a
          href="/api/auth/signin?callbackUrl=/home"
          className="rounded bg-black px-4 py-2 text-sm font-medium text-white dark:bg-white dark:text-black"
        >
          Sign in
        </a>
      </div>
    </main>
  );
}
