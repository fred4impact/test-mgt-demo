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
    <main className="mx-auto max-w-6xl px-12 py-10">
      <Link href={`/projects/${projectId}`} className="text-sm font-semibold text-muted hover:text-accent">
        &larr; Back to project
      </Link>

      <div className="mb-6 mt-4 flex items-center gap-3">
        <h1 className="text-3xl font-extrabold tracking-tight text-text">Tags</h1>
        <span className="rounded-full bg-surface-sunken px-2.5 py-1 text-xs font-bold text-muted">
          {tags.length}
        </span>
      </div>

      <CreateTagForm projectId={projectId} />

      <div className="mt-6 flex flex-wrap gap-2">
        {tags.map((tag) => (
          <span
            key={tag.id}
            className="rounded-full bg-accent-soft px-3.5 py-1.5 text-sm font-bold text-accent-soft-text"
          >
            {tag.name}
          </span>
        ))}
        {tags.length === 0 && <p className="text-sm text-faint">None yet.</p>}
      </div>
    </main>
  );
}
