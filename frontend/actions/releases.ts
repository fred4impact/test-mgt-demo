"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createRelease } from "@/services/releases";

export interface CreateReleaseState {
  error?: string;
}

export async function createReleaseAction(
  _prevState: CreateReleaseState,
  formData: FormData,
): Promise<CreateReleaseState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a release." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");

  try {
    await createRelease(session.accessToken, projectId, { name });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create release" };
  }

  revalidatePath(`/projects/${projectId}/releases`);
  return {};
}
