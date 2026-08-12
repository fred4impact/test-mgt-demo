"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createEnvironment } from "@/services/environments";

export interface CreateEnvironmentState {
  error?: string;
}

export async function createEnvironmentAction(
  _prevState: CreateEnvironmentState,
  formData: FormData,
): Promise<CreateEnvironmentState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create an environment." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");
  const type = String(formData.get("type") ?? "");
  const url = String(formData.get("url") ?? "");

  try {
    await createEnvironment(session.accessToken, projectId, {
      name,
      type: type || undefined,
      url: url || undefined,
    });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create environment" };
  }

  revalidatePath(`/projects/${projectId}/environments`);
  return {};
}
