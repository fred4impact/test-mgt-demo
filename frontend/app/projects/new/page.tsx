import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listProjects } from "@/services/projects";
import { CreateProjectForm } from "./CreateProjectForm";

export default async function NewProjectPage() {
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect("/api/auth/signin?callbackUrl=/projects/new");
  }

  const projects = await listProjects(session.accessToken);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Create a project</h1>
      <CreateProjectForm />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing projects</h2>
      <ul className="space-y-1">
        {projects.map((project) => (
          <li key={project.id} className="text-sm">
            {project.name} <span className="text-gray-400">({project.key})</span>{" "}
            <Link href={`/projects/${project.id}/requirements`} className="text-blue-600 hover:underline">
              Requirements
            </Link>
          </li>
        ))}
        {projects.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
