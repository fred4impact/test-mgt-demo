import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { getProject, type Project } from "@/services/projects";

const SECTIONS = [
  { href: "requirements", label: "Requirements", icon: "R" },
  { href: "test-cases", label: "Test Cases", icon: "TC" },
  { href: "test-suites", label: "Test Suites", icon: "TS" },
  { href: "tags", label: "Tags", icon: "Tg" },
  { href: "releases", label: "Releases", icon: "Rl" },
  { href: "builds", label: "Builds", icon: "Bd" },
  { href: "test-plans", label: "Test Plans", icon: "Pl" },
  { href: "environments", label: "Environments", icon: "Ev" },
  { href: "test-cycles", label: "Test Cycles", icon: "Cy" },
];

export default async function ProjectHubPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}`);
  }

  let project: Project;
  try {
    project = await getProject(session.accessToken, projectId);
  } catch {
    return (
      <main className="mx-auto max-w-md p-8">
        <Link href="/home" className="text-sm font-semibold text-muted hover:text-accent">
          &larr; Back to organization
        </Link>
        <p className="mt-4 text-sm text-faint">Project not found.</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-5xl px-12 py-10">
      <Link href="/home" className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to organization
      </Link>

      <div className="mb-10 mt-6">
        <h1 className="mb-1 text-3xl font-extrabold tracking-tight text-text">{project.name}</h1>
        <div className="flex items-center gap-2">
          <span className="rounded bg-surface-sunken px-2 py-0.5 font-mono text-xs text-text">{project.key}</span>
          <span className="rounded-full bg-status-neutral-soft px-2.5 py-0.5 text-xs font-bold text-status-neutral">
            {project.status}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-4 gap-5">
        {SECTIONS.map((section) => (
          <Link
            key={section.href}
            href={`/projects/${projectId}/${section.href}`}
            className="flex flex-col gap-3 rounded-xl border border-border bg-surface p-6 shadow-card transition hover:-translate-y-0.5 hover:border-accent"
          >
            <span className="flex h-10 w-10 items-center justify-center rounded-lg bg-accent-soft text-sm font-extrabold text-accent">
              {section.icon}
            </span>
            <h3 className="text-lg font-bold text-text">{section.label}</h3>
          </Link>
        ))}
      </div>
    </main>
  );
}
