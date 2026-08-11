import Link from "next/link";
import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTags } from "@/services/tags";
import { CreateTagForm } from "./CreateTagForm";

export default async function ProjectTagsPage({
  params,
}: {
  params: Promise<{ projectId: string }>;
}) {
  const { projectId } = await params;
  const session = await auth();

  if (!session?.accessToken || session.error) {
    redirect(`/api/auth/signin?callbackUrl=/projects/${projectId}/tags`);
  }

  const tags = await listTags(session.accessToken, projectId);

  return (
    <main className="mx-auto max-w-md p-8">
      <Link href={`/projects/${projectId}`} className="text-sm text-blue-600 hover:underline">
        &larr; Back to project
      </Link>
      <h1 className="mb-4 mt-4 text-xl font-semibold">Tags</h1>
      <CreateTagForm projectId={projectId} />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing tags</h2>
      <ul className="space-y-1">
        {tags.map((tag) => (
          <li key={tag.id} className="text-sm">
            {tag.name}
          </li>
        ))}
        {tags.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
