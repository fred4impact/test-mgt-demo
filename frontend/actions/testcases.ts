"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createTestCase, type CreateTestStepInput } from "@/services/testcases";

export interface CreateTestCaseState {
  error?: string;
}

export async function createTestCaseAction(
  _prevState: CreateTestCaseState,
  formData: FormData,
): Promise<CreateTestCaseState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a test case." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const folderId = String(formData.get("folderId") ?? "");
  const title = String(formData.get("title") ?? "");

  const actions = formData.getAll("stepAction").map(String);
  const testData = formData.getAll("stepTestData").map(String);
  const expectedResults = formData.getAll("stepExpectedResult").map(String);

  const steps: CreateTestStepInput[] = actions
    .map((action, i) => ({
      action: action.trim(),
      testData: testData[i]?.trim() || undefined,
      expectedResult: expectedResults[i]?.trim() || undefined,
    }))
    .filter((step) => step.action !== "");

  try {
    await createTestCase(session.accessToken, projectId, { folderId, title, steps });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create test case" };
  }

  revalidatePath(`/projects/${projectId}/test-cases`);
  return {};
}
