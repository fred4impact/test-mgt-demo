import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { getProject, type Project } from "@/services/projects";

const SECTIONS = [
  { href: "requirements", label: "Requirements" },
  { href: "test-cases", label: "Test Cases" },
  { href: "test-suites", label: "Test Suites" },
  { href: "tags", label: "Tags" },
  { href: "releases", label: "Releases" },
];

export default async function ProjectHubPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}`);
  }

  let project: Project;
  try {
    project = await getProject(session.accessToken, projectId);
  } catch {
    return (
      <main className="mx-auto max-w-md p-8">
        <Link href="/home" className="text-sm text-blue-600 hover:underline">
          &larr; Back to organization
        </Link>
        <p className="mt-4 text-sm text-gray-500">Project not found.</p>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-md p-8">
      <Link href="/home" className="text-sm text-blue-600 hover:underline">
        &larr; Back to organization
      </Link>

      <h1 className="mb-1 mt-4 text-xl font-semibold">{project.name}</h1>
      <p className="mb-6 text-sm text-gray-500">
        {project.key} <span className="text-gray-400">({project.status})</span>
      </p>

      <ul className="space-y-2">
        {SECTIONS.map((section) => (
          <li key={section.href} className="rounded border border-gray-200 p-3 text-sm">
            <Link
              href={`/projects/${projectId}/${section.href}`}
              className="font-medium text-blue-600 hover:underline"
            >
              {section.label}
            </Link>
          </li>
        ))}
      </ul>
    </main>
  );
}
