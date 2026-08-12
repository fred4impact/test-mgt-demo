"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { addTestCycleCase, removeTestCycleCase } from "@/services/testcyclecases";

export async function removeTestCycleCaseAction(projectId: string, cycleId: string, testCaseId: string) {
  const session = await auth();
  if (!session?.accessToken) {
    return;
  }

  await removeTestCycleCase(session.accessToken, projectId, cycleId, testCaseId);
  revalidatePath(`/projects/${projectId}/test-cycles/${cycleId}`);
}

export async function addTestCycleCaseAction(projectId: string, cycleId: string, formData: FormData) {
  const session = await auth();
  if (!session?.accessToken) {
    return;
  }

  const testCaseId = String(formData.get("testCaseId") ?? "");
  const assigneeId = String(formData.get("assigneeId") ?? "");

  await addTestCycleCase(session.accessToken, projectId, cycleId, {
    testCaseId,
    assigneeId: assigneeId || undefined,
  });
  revalidatePath(`/projects/${projectId}/test-cycles/${cycleId}`);
}
