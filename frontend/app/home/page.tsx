import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { fetchMe } from "@/services/me";
import { listProjects } from "@/services/projects";

export default async function OrgHomePage() {
  const session = await auth();

  if (!session?.accessToken) {
    redirect("/api/auth/signin?callbackUrl=/home");
  }

  const [me, projects] = await Promise.all([
    fetchMe(session.accessToken),
    listProjects(session.accessToken),
  ]);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-1 text-xl font-semibold">{me.organizationName}</h1>
      <p className="mb-6 text-sm text-gray-500">Your organization's projects</p>

      <div className="mb-4">
        <Link href="/projects/new" className="text-sm text-blue-600 hover:underline">
          + Create project
        </Link>
      </div>

      <ul className="space-y-2">
        {projects.map((project) => (
          <li key={project.id} className="rounded border border-gray-200 p-3 text-sm">
            <Link href={`/projects/${project.id}`} className="font-medium text-blue-600 hover:underline">
              {project.name}
            </Link>{" "}
            <span className="text-gray-400">({project.key})</span>
          </li>
        ))}
        {projects.length === 0 && <li className="text-sm text-gray-400">No projects yet.</li>}
      </ul>
    </main>
  );
}
