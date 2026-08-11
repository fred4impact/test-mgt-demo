"use client";

import { useActionState } from "react";
import { createRequirementAction, type CreateRequirementState } from "@/actions/requirements";

const initialState: CreateRequirementState = {};

export function CreateRequirementForm({ projectId }: { projectId: string }) {
  const [state, formAction, pending] = useActionState(createRequirementAction, initialState);

  return (
    <form action={formAction} className="space-y-3 rounded-lg border border-border bg-surface p-5 shadow-card">
      <input type="hidden" name="projectId" value={projectId} />
      <div>
        <label htmlFor="title" className="block text-sm font-semibold text-text">
          Title
        </label>
        <input
          id="title"
          name="title"
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
        {pending ? "Creating…" : "Create requirement"}
      </button>
    </form>
  );
}
