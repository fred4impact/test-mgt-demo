"use client";

import { useActionState, useState } from "react";
import { createTestCaseAction, type CreateTestCaseState } from "@/actions/testcases";
import type { TestFolder } from "@/services/testfolders";

const initialState: CreateTestCaseState = {};

interface StepRow {
  key: number;
}

export function CreateTestCaseForm({ projectId, folders }: { projectId: string; folders: TestFolder[] }) {
  const [state, formAction, pending] = useActionState(createTestCaseAction, initialState);
  const [steps, setSteps] = useState<StepRow[]>([{ key: 0 }]);
  const [nextKey, setNextKey] = useState(1);

  return (
    <form action={formAction} className="space-y-3 rounded-lg border border-border bg-surface p-5 shadow-card">
      <input type="hidden" name="projectId" value={projectId} />
      <div>
        <label htmlFor="folderId" className="block text-sm font-semibold text-text">
          Folder
        </label>
        <select
          id="folderId"
          name="folderId"
          required
          className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        >
          {folders.map((folder) => (
            <option key={folder.id} value={folder.id}>
              {folder.name}
            </option>
          ))}
        </select>
      </div>
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

      <div>
        <span className="block text-sm font-semibold text-text">Steps</span>
        <div className="mt-1 space-y-2">
          {steps.map((step, index) => (
            <div key={step.key} className="grid grid-cols-3 gap-2">
              <input
                name="stepAction"
                placeholder={`Step ${index + 1} action`}
                className="rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
              />
              <input
                name="stepTestData"
                placeholder="Test data"
                className="rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
              />
              <input
                name="stepExpectedResult"
                placeholder="Expected result"
                className="rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
              />
            </div>
          ))}
        </div>
        <button
          type="button"
          onClick={() => {
            setSteps([...steps, { key: nextKey }]);
            setNextKey(nextKey + 1);
          }}
          className="mt-2 text-sm font-semibold text-accent hover:text-accent-hover"
        >
          + Add step
        </button>
      </div>

      {state.error && <p className="text-sm font-semibold text-status-danger">{state.error}</p>}
      <button
        type="submit"
        disabled={pending}
        className="rounded-md bg-accent px-4 py-2 text-sm font-bold text-accent-ink hover:bg-accent-hover disabled:opacity-50"
      >
        {pending ? "Creating…" : "Create test case"}
      </button>
    </form>
  );
}
