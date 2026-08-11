"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createTag } from "@/services/tags";

export interface CreateTagState {
  error?: string;
}

export async function createTagAction(
  _prevState: CreateTagState,
  formData: FormData,
): Promise<CreateTagState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a tag." };
  }

  const projectId = String(formData.get("projectId") ?? "");
  const name = String(formData.get("name") ?? "");

  try {
    await createTag(session.accessToken, projectId, { name });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create tag" };
  }

  revalidatePath(`/projects/${projectId}/tags`);
  return {};
}
