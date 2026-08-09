"use client";

import { useActionState } from "react";
import { createRoleAction, type CreateRoleState } from "@/actions/roles";

const initialState: CreateRoleState = {};

export function CreateRoleForm() {
  const [state, formAction, pending] = useActionState(createRoleAction, initialState);

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
      <div className="flex items-center gap-2">
        <input id="systemRole" name="systemRole" type="checkbox" className="h-4 w-4" />
        <label htmlFor="systemRole" className="text-sm font-medium">
          System role
        </label>
      </div>
      {state.error && <p className="text-sm text-red-600">{state.error}</p>}
      <button
        type="submit"
        disabled={pending}
        className="rounded bg-black px-3 py-1.5 text-sm text-white disabled:opacity-50"
      >
        {pending ? "Creating…" : "Create role"}
      </button>
    </form>
  );
}
