"use client";

import { useActionState } from "react";
import { createTestPlanAction, type CreateTestPlanState } from "@/actions/testplans";
import type { Release } from "@/services/releases";

const initialState: CreateTestPlanState = {};

export function CreateTestPlanForm({ projectId, releases }: { projectId: string; releases: Release[] }) {
  const [state, formAction, pending] = useActionState(createTestPlanAction, initialState);

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
      <div>
        <label htmlFor="releaseId" className="block text-sm font-medium">
          Release
        </label>
        <select
          id="releaseId"
          name="releaseId"
          required
          className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
        >
          {releases.map((release) => (
            <option key={release.id} value={release.id}>
              {release.name}
            </option>
          ))}
        </select>
      </div>
      {state.error && <p className="text-sm text-red-600">{state.error}</p>}
      <button
        type="submit"
        disabled={pending}
        className="rounded bg-black px-3 py-1.5 text-sm text-white disabled:opacity-50"
      >
        {pending ? "Creating…" : "Create test plan"}
      </button>
    </form>
  );
}
