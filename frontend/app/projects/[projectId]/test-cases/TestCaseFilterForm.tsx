import type { TestCaseFilters } from "@/services/testcases";
import type { TestFolder } from "@/services/testfolders";

export function TestCaseFilterForm({ filters, folders }: { filters: TestCaseFilters; folders: TestFolder[] }) {
  const hasFilters = Boolean(
    filters.q ||
      filters.status ||
      filters.priority ||
      filters.severity ||
      filters.testType ||
      filters.automationStatus ||
      filters.folderId,
  );

  return (
    <form
      method="get"
      className="flex flex-wrap items-end gap-3 rounded-lg border border-border bg-surface p-4 shadow-card"
    >
      <div>
        <label htmlFor="q" className="block text-xs font-semibold text-muted">
          Keyword
        </label>
        <input
          id="q"
          name="q"
          defaultValue={filters.q ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="status" className="block text-xs font-semibold text-muted">
          Status
        </label>
        <input
          id="status"
          name="status"
          defaultValue={filters.status ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="priority" className="block text-xs font-semibold text-muted">
          Priority
        </label>
        <input
          id="priority"
          name="priority"
          defaultValue={filters.priority ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="severity" className="block text-xs font-semibold text-muted">
          Severity
        </label>
        <input
          id="severity"
          name="severity"
          defaultValue={filters.severity ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="testType" className="block text-xs font-semibold text-muted">
          Test type
        </label>
        <input
          id="testType"
          name="testType"
          defaultValue={filters.testType ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="automationStatus" className="block text-xs font-semibold text-muted">
          Automation status
        </label>
        <input
          id="automationStatus"
          name="automationStatus"
          defaultValue={filters.automationStatus ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
      </div>
      <div>
        <label htmlFor="filterFolderId" className="block text-xs font-semibold text-muted">
          Folder
        </label>
        <select
          id="filterFolderId"
          name="folderId"
          defaultValue={filters.folderId ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        >
          <option value="">Any</option>
          {folders.map((folder) => (
            <option key={folder.id} value={folder.id}>
              {folder.name}
            </option>
          ))}
        </select>
      </div>
      <button
        type="submit"
        className="rounded-md bg-accent px-4 py-1.5 text-sm font-bold text-accent-ink hover:bg-accent-hover"
      >
        Filter
      </button>
      {hasFilters && (
        <a href="?" className="text-sm font-semibold text-accent hover:text-accent-hover">
          Clear
        </a>
      )}
    </form>
  );
}
