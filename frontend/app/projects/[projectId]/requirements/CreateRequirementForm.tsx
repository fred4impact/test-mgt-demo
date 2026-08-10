"use client";

import { useActionState } from "react";
import { createRequirementAction, type CreateRequirementState } from "@/actions/requirements";

const initialState: CreateRequirementState = {};

export function CreateRequirementForm({ projectId }: { projectId: string }) {
  const [state, formAction, pending] = useActionState(createRequirementAction, initialState);

  return (
    <form action={formAction} className="space-y-3">
      <input type="hidden" name="projectId" value={projectId} />
      <div>
        <label htmlFor="title" className="block text-sm font-medium">
          Title
        </label>
        <input
          id="title"
          name="title"
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
        {pending ? "Creating…" : "Create requirement"}
      </button>
    </form>
  );
}
