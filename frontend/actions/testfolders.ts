"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createTestFolder } from "@/services/testfolders";

export interface CreateTestFolderState {
  error?: string;
}

export async function createTestFolderAction(
  _prevState: CreateTestFolderState,
  formData: FormData,
): Promise<CreateTestFolderState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a test folder." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");

  try {
    await createTestFolder(session.accessToken, projectId, { name });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create test folder" };
  }

  revalidatePath(`/projects/${projectId}/test-cases`);
  return {};
}
