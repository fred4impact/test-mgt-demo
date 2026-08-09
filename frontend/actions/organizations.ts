"use server";

import { revalidatePath } from "next/cache";
import { createOrganization } from "@/services/organizations";

export interface CreateOrganizationState {
  error?: string;
}

export async function createOrganizationAction(
  _prevState: CreateOrganizationState,
  formData: FormData,
): Promise<CreateOrganizationState> {
  const name = String(formData.get("name") ?? "");
  const slug = String(formData.get("slug") ?? "");

  try {
    await createOrganization({ name, slug });
  } catch (error) {
    return { error: error instanceof Error ? error.message : "Failed to create organization" };
  }

  revalidatePath("/organizations/new");
  return {};
}
