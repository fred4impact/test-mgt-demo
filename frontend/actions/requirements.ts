"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createRequirement } from "@/services/requirements";

export interface CreateRequirementState {
  error?: string;
}

export async function createRequirementAction(
  _prevState: CreateRequirementState,
  formData: FormData,
): Promise<CreateRequirementState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a requirement." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const title = String(formData.get("title") ?? "");

  try {
    await createRequirement(session.accessToken, projectId, { title });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create requirement" };
  }

  revalidatePath(`/projects/${projectId}/requirements`);
  return {};
}
