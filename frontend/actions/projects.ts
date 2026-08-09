"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createProject } from "@/services/projects";

export interface CreateProjectState {
  error?: string;
}

export async function createProjectAction(
  _prevState: CreateProjectState,
  formData: FormData,
): Promise<CreateProjectState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a project." };
  }

  const key = String(formData.get("key") ?? "");
  const name = String(formData.get("name") ?? "");

  try {
    await createProject(session.accessToken, { key, name });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create project" };
  }

  revalidatePath("/projects/new");
  return {};
}
