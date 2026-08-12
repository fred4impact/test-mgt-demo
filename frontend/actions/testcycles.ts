"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createTestCycle } from "@/services/testcycles";

export interface CreateTestCycleState {
  error?: string;
}

export async function createTestCycleAction(
  _prevState: CreateTestCycleState,
  formData: FormData,
): Promise<CreateTestCycleState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a test cycle." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");
  const testPlanId = String(formData.get("testPlanId") ?? "");
  const releaseId = String(formData.get("releaseId") ?? "");
  const buildId = String(formData.get("buildId") ?? "");
  const environmentId = String(formData.get("environmentId") ?? "");

  try {
    await createTestCycle(session.accessToken, projectId, { name, testPlanId, releaseId, buildId, environmentId });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create test cycle" };
  }

  revalidatePath(`/projects/${projectId}/test-cycles`);
  return {};
}
