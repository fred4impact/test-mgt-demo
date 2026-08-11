"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createTestSuite } from "@/services/testsuites";

export interface CreateTestSuiteState {
  error?: string;
}

export async function createTestSuiteAction(
  _prevState: CreateTestSuiteState,
  formData: FormData,
): Promise<CreateTestSuiteState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a test suite." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");

  try {
    await createTestSuite(session.accessToken, projectId, { name });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create test suite" };
  }

  revalidatePath(`/projects/${projectId}/test-suites`);
  return {};
}
