import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { fetchMe } from "@/services/me";
import { listProjects } from "@/services/projects";

export default async function OrgHomePage() {
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect("/api/auth/signin?callbackUrl=/home");
  }

  const [me, projects] = await Promise.all([
    fetchMe(session.accessToken),
    listProjects(session.accessToken),
  ]);

  return (
    <main className="mx-auto max-w-5xl px-12 py-10">
      <h1 className="mb-1 text-3xl font-extrabold tracking-tight text-text">{me.organizationName}</h1>
      <p className="mb-8 text-muted">Your organization&apos;s projects</p>

      <div className="mb-6">
        <Link href="/projects/new" className="text-sm font-semibold text-accent hover:text-accent-hover">
          + Create project
        </Link>
      </div>

      <div className="grid grid-cols-3 gap-5">
        {projects.map((project) => (
          <Link
            key={project.id}
            href={`/projects/${project.id}`}
            className="flex flex-col gap-2 rounded-xl border border-border bg-surface p-6 shadow-card transition hover:-translate-y-0.5 hover:border-accent"
          >
            <h3 className="text-lg font-bold text-text">{project.name}</h3>
            <span className="font-mono text-xs text-muted">{project.key}</span>
          </Link>
        ))}
        {projects.length === 0 && <p className="text-sm text-faint">No projects yet.</p>}
      </div>
    </main>
  );
}
