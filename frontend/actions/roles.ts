"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { createRole } from "@/services/roles";

export interface CreateRoleState {
  error?: string;
}

export async function createRoleAction(
  _prevState: CreateRoleState,
  formData: FormData,
): Promise<CreateRoleState> {
  const session = await auth();
  if (!session?.accessToken) {
    return { error: "You must be signed in to create a role." };
  }

  const name = String(formData.get("name") ?? "");
  const systemRole = formData.get("systemRole") === "on";

  try {
    await createRole(session.accessToken, { name, systemRole });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create role" };
  }

  revalidatePath("/roles/new");
  return {};
}
