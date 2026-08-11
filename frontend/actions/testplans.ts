"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createTestPlan } from "@/services/testplans";

export interface CreateTestPlanState {
  error?: string;
}

export async function createTestPlanAction(
  _prevState: CreateTestPlanState,
  formData: FormData,
): Promise<CreateTestPlanState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a test plan." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");
  const releaseId = String(formData.get("releaseId") ?? "");

  try {
    await createTestPlan(session.accessToken, projectId, { name, releaseId });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create test plan" };
  }

  revalidatePath(`/projects/${projectId}/test-plans`);
  return {};
}
