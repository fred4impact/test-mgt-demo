"use client";

import { useActionState } from "react";
import { createTestCycleAction, type CreateTestCycleState } from "@/actions/testcycles";
import type { TestPlan } from "@/services/testplans";
import type { Release } from "@/services/releases";
import type { Build } from "@/services/builds";
import type { Environment } from "@/services/environments";

const initialState: CreateTestCycleState = {};

export function CreateTestCycleForm({
  projectId,
  testPlans,
  releases,
  builds,
  environments,
}: {
  projectId: string;
  testPlans: TestPlan[];
  releases: Release[];
  builds: Build[];
  environments: Environment[];
}) {
  const [state, formAction, pending] = useActionState(createTestCycleAction, initialState);

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
      <div className="grid grid-cols-2 gap-3">
        <div>
          <label htmlFor="testPlanId" className="block text-sm font-semibold text-text">
            Test Plan
          </label>
          <select
            id="testPlanId"
            name="testPlanId"
            required
            className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
          >
            {testPlans.map((testPlan) => (
              <option key={testPlan.id} value={testPlan.id}>
                {testPlan.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="releaseId" className="block text-sm font-semibold text-text">
            Release
          </label>
          <select
            id="releaseId"
            name="releaseId"
            required
            className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
          >
            {releases.map((release) => (
              <option key={release.id} value={release.id}>
                {release.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="buildId" className="block text-sm font-semibold text-text">
            Build
          </label>
          <select
            id="buildId"
            name="buildId"
            required
            className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
          >
            {builds.map((build) => (
              <option key={build.id} value={build.id}>
                {build.name}
              </option>
            ))}
          </select>
        </div>
        <div>
          <label htmlFor="environmentId" className="block text-sm font-semibold text-text">
            Environment
          </label>
          <select
            id="environmentId"
            name="environmentId"
            required
            className="mt-1 w-full rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
          >
            {environments.map((environment) => (
              <option key={environment.id} value={environment.id}>
                {environment.name}
              </option>
            ))}
          </select>
        </div>
      </div>
      {state.error && <p className="text-sm font-semibold text-status-danger">{state.error}</p>}
      <button
        type="submit"
        disabled={pending}
        className="rounded-md bg-accent px-4 py-2 text-sm font-bold text-accent-ink hover:bg-accent-hover disabled:opacity-50"
      >
        {pending ? "Creating…" : "Create test cycle"}
      </button>
    </form>
  );
}
