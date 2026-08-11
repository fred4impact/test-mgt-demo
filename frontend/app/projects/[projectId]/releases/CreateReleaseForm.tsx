"use client";

import { useActionState } from "react";
import { createReleaseAction, type CreateReleaseState } from "@/actions/releases";

const initialState: CreateReleaseState = {};

export function CreateReleaseForm({ projectId }: { projectId: string }) {
  const [state, formAction, pending] = useActionState(createReleaseAction, initialState);

  return (
    <form action={formAction} className="space-y-3">
      <input type="hidden" name="projectId" value={projectId} />
      <div>
        <label htmlFor="name" className="block text-sm font-medium">
          Name
        </label>
        <input
          id="name"
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
        {pending ? "Creating…" : "Create release"}
      </button>
    </form>
  );
}
