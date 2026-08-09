"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createTeam } from "@/services/teams";

export interface CreateTeamState {
  error?: string;
}

export async function createTeamAction(
  _prevState: CreateTeamState,
  formData: FormData,
): Promise<CreateTeamState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a team." };
  }

  const name = String(formData.get("name") ?? "");
  const description = String(formData.get("description") ?? "");

  try {
    await createTeam(session.accessToken, { name, description });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create team" };
  }

  revalidatePath("/teams/new");
  return {};
}
