"use client";

import { useActionState } from "react";
import { createEnvironmentAction, type CreateEnvironmentState } from "@/actions/environments";

const initialState: CreateEnvironmentState = {};

export function CreateEnvironmentForm({ projectId }: { projectId: string }) {
  const [state, formAction, pending] = useActionState(createEnvironmentAction, initialState);

  return (
    <form action={formAction} className="space-y-3 rounded-lg border border-border bg-surface p-5 shadow-card">
      <input type="hidden" name="projectId" value={projectId} />
      <div>
        <label htmlFor="name" className="block text-sm font-semibold text-text">
          Name
        </label>
        <input
          id="name"
          name="name"
          required
          className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="type" className="block text-sm font-semibold text-text">
          Type
        </label>
        <input
          id="type"
          name="type"
          placeholder="e.g. STAGING"
          className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="url" className="block text-sm font-semibold text-text">
          URL
        </label>
        <input
          id="url"
          name="url"
          placeholder="https://..."
          className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      {state.error && <p className="text-sm font-semibold text-status-danger">{state.error}</p>}
      <button
        type="submit"
        disabled={pending}
        className="rounded-md bg-accent px-4 py-2 text-sm font-bold text-accent-ink hover:bg-accent-hover disabled:opacity-50"
      >
        {pending ? "Creating…" : "Create environment"}
      </button>
    </form>
  );
}
