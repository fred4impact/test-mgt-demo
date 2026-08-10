"use client";

import { useActionState } from "react";
import { createTestFolderAction, type CreateTestFolderState } from "@/actions/testfolders";

const initialState: CreateTestFolderState = {};

export function CreateFolderInlineForm({ projectId }: { projectId: string }) {
  const [state, formAction, pending] = useActionState(createTestFolderAction, initialState);

  return (
    <div className="rounded border border-dashed border-gray-300 p-4">
      <p className="mb-3 text-sm text-gray-500">
        This project has no test folders yet. Create one to start adding test cases.
      </p>
      <form action={formAction} className="space-y-3">
        <input type="hidden" name="projectId" value={projectId} />
        <div>
          <label htmlFor="folderName" className="block text-sm font-medium">
            Folder name
          </label>
          <input
            id="folderName"
            name="name"
            required
            className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
          />
        </div>
        {state.error && <p className="text-sm text-red-600">{state.error}</p>}
        <button
          type="submit"
          disabled={pending}
          className="rounded bg-black px-3 py-1.5 text-sm text-white disabled:opacity-50"
        >
          {pending ? "Creating…" : "Create folder"}
        </button>
      </form>
    </div>
  );
}
