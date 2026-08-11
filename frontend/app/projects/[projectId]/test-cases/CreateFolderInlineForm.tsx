"use client";

import { useActionState } from "react";
import { createTestFolderAction, type CreateTestFolderState } from "@/actions/testfolders";

const initialState: CreateTestFolderState = {};

export function CreateFolderInlineForm({ projectId }: { projectId: string }) {
  const [state, formAction, pending] = useActionState(createTestFolderAction, initialState);

  return (
    <div className="rounded-lg border border-dashed border-border bg-surface p-5">
      <p className="mb-3 text-sm text-muted">
        This project has no test folders yet. Create one to start adding test cases.
      </p>
      <form action={formAction} className="space-y-3">
        <input type="hidden" name="projectId" value={projectId} />
        <div>
          <label htmlFor="folderName" className="block text-sm font-semibold text-text">
            Folder name
          </label>
          <input
            id="folderName"
            name="name"
            required
            className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
          />
        </div>
        {state.error && <p className="text-sm font-semibold text-status-danger">{state.error}</p>}
        <button
          type="submit"
          disabled={pending}
          className="rounded-md bg-accent px-4 py-2 text-sm font-bold text-accent-ink hover:bg-accent-hover disabled:opacity-50"
        >
          {pending ? "Creating…" : "Create folder"}
        </button>
      </form>
    </div>
  );
}
