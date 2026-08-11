import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTestSuites } from "@/services/testsuites";
import { CreateTestSuiteForm } from "./CreateTestSuiteForm";

export default async function ProjectTestSuitesPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/test-suites`);
  }

  const suites = await listTestSuites(session.accessToken, projectId);

  return (
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Test Suites</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {suites.length}
        </span>
      </div>

      <CreateTestSuiteForm projectId={projectId} />

      <div className="mt-6 grid grid-cols-4 gap-5">
        {suites.map((suite) => (
          <div
            key={suite.id}
            className="rounded-xl border border-border bg-surface p-6 shadow-card"
          >
            <h3 className="text-lg font-bold text-text">{suite.name}</h3>
          </div>
        ))}
        {suites.length === 0 && <p className="text-sm text-faint">None yet.</p>}
      </div>
    </main>
  );
}
