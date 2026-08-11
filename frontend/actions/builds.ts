"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createBuild } from "@/services/builds";

export interface CreateBuildState {
  error?: string;
}

export async function createBuildAction(
  _prevState: CreateBuildState,
  formData: FormData,
): Promise<CreateBuildState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a build." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");
  const releaseId = String(formData.get("releaseId") ?? "");

  try {
    await createBuild(session.accessToken, projectId, { name, releaseId });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create build" };
  }

  revalidatePath(`/projects/${projectId}/builds`);
  return {};
}
