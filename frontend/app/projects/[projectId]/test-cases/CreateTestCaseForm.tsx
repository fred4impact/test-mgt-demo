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
    <form action={formAction} className="space-y-3">
      <input type="hidden" name="projectId" value={projectId} />
      <div>
        <label htmlFor="folderId" className="block text-sm font-medium">
          Folder
        </label>
        <select
          id="folderId"
          name="folderId"
          required
          className="mt-1 w-full rounded border border-gray-300 px-2 py-1"
        >
          {folders.map((folder) => (
            <option key={folder.id} value={folder.id}>
              {folder.name}
            </option>
          ))}
        </select>
      </div>
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

      <div>
        <span className="block text-sm font-medium">Steps</span>
        <div className="mt-1 space-y-2">
          {steps.map((step, index) => (
            <div key={step.key} className="grid grid-cols-3 gap-2">
              <input
                name="stepAction"
                placeholder={`Step ${index + 1} action`}
                className="rounded border border-gray-300 px-2 py-1 text-sm"
              />
              <input
                name="stepTestData"
                placeholder="Test data"
                className="rounded border border-gray-300 px-2 py-1 text-sm"
              />
              <input
                name="stepExpectedResult"
                placeholder="Expected result"
                className="rounded border border-gray-300 px-2 py-1 text-sm"
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
          className="mt-2 text-sm text-blue-600 hover:underline"
        >
          + Add step
        </button>
      </div>

      {state.error && <p className="text-sm text-red-600">{state.error}</p>}
      <button
        type="submit"
        disabled={pending}
        className="rounded bg-black px-3 py-1.5 text-sm text-white disabled:opacity-50"
      >
        {pending ? "Creating…" : "Create test case"}
      </button>
    </form>
  );
}
