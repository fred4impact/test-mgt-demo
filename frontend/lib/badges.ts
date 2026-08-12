export function statusBadgeClasses(status: string) {
  const normalized = status.toUpperCase();
  if (normalized === "ACTIVE" || normalized === "PASSED") return "bg-status-success-soft text-status-success";
  if (normalized === "FAILED") return "bg-status-danger-soft text-status-danger";
  if (normalized === "BLOCKED" || normalized === "SKIPPED") return "bg-status-warning-soft text-status-warning";
  return "bg-status-neutral-soft text-status-neutral";
}

export function severityBadgeClasses(severity: string | null) {
  const normalized = severity?.toUpperCase();
  if (normalized === "CRITICAL") return "bg-status-danger-soft text-status-danger";
  if (normalized === "MAJOR") return "bg-status-warning-soft text-status-warning";
  return "bg-status-neutral-soft text-status-neutral";
}
