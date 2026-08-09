"use client";

import { useActionState } from "react";
import { createTeamAction, type CreateTeamState } from "@/actions/teams";

const initialState: CreateTeamState = {};

export function CreateTeamForm() {
  const [state, formAction, pending] = useActionState(createTeamAction, initialState);

  return (
    <form action={formAction} className="space-y-3">
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
        <label htmlFor="description" className="block text-sm font-medium">
          Description
        </label>
        <input
          id="description"
          name="description"
          className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
        />
      </div>
      {state.error && <p className="text-sm text-red-600">{state.error}</p>}
      <button
        type="submit"
        disabled={pending}
        className="rounded bg-black px-3 py-1.5 text-sm text-white disabled:opacity-50"
      >
        {pending ? "Creating…" : "Create team"}
      </button>
    </form>
  );
}
