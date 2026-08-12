"use server";

import { revalidatePath } from "next/cache";
import { auth } from "@/auth";
import { updateTestExecution, updateExecutionStep, type TestExecutionStatus } from "@/services/testexecutions";

export async function updateTestExecutionAction(
  projectId: string,
  cycleId: string,
  testCaseId: string,
  formData: FormData,
) {
  const session = await auth();
  if (!session?.accessToken) {
    return;
  }

  const status = String(formData.get("status") ?? "") as TestExecutionStatus;
  const actualResult = String(formData.get("actualResult") ?? "");
  const comment = String(formData.get("comment") ?? "");

  await updateTestExecution(session.accessToken, projectId, cycleId, testCaseId, {
    status,
    actualResult: actualResult || undefined,
    comment: comment || undefined,
  });
  revalidatePath(`/projects/${projectId}/test-cycles/${cycleId}/executions/${testCaseId}`);
  revalidatePath(`/projects/${projectId}/test-cycles/${cycleId}`);
}

export async function updateExecutionStepAction(
  projectId: string,
  cycleId: string,
  testCaseId: string,
  testStepId: string,
  formData: FormData,
) {
  const session = await auth();
  if (!session?.accessToken) {
    return;
  }

  const status = String(formData.get("status") ?? "") as TestExecutionStatus;
  const actualResult = String(formData.get("actualResult") ?? "");
  const comment = String(formData.get("comment") ?? "");

  await updateExecutionStep(session.accessToken, projectId, cycleId, testCaseId, testStepId, {
    status,
    actualResult: actualResult || undefined,
    comment: comment || undefined,
  });
  revalidatePath(`/projects/${projectId}/test-cycles/${cycleId}/executions/${testCaseId}`);
}
