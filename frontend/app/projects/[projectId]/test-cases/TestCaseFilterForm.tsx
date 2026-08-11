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
    <form method="get" className="flex flex-wrap items-end gap-2">
      <div>
        <label htmlFor="q" className="block text-xs font-medium text-gray-500">
          Keyword
        </label>
        <input
          id="q"
          name="q"
          defaultValue={filters.q ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        />
      </div>
      <div>
        <label htmlFor="status" className="block text-xs font-medium text-gray-500">
          Status
        </label>
        <input
          id="status"
          name="status"
          defaultValue={filters.status ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        />
      </div>
      <div>
        <label htmlFor="priority" className="block text-xs font-medium text-gray-500">
          Priority
        </label>
        <input
          id="priority"
          name="priority"
          defaultValue={filters.priority ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        />
      </div>
      <div>
        <label htmlFor="severity" className="block text-xs font-medium text-gray-500">
          Severity
        </label>
        <input
          id="severity"
          name="severity"
          defaultValue={filters.severity ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        />
      </div>
      <div>
        <label htmlFor="testType" className="block text-xs font-medium text-gray-500">
          Test type
        </label>
        <input
          id="testType"
          name="testType"
          defaultValue={filters.testType ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        />
      </div>
      <div>
        <label htmlFor="automationStatus" className="block text-xs font-medium text-gray-500">
          Automation status
        </label>
        <input
          id="automationStatus"
          name="automationStatus"
          defaultValue={filters.automationStatus ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        />
      </div>
      <div>
        <label htmlFor="filterFolderId" className="block text-xs font-medium text-gray-500">
          Folder
        </label>
        <select
          id="filterFolderId"
          name="folderId"
          defaultValue={filters.folderId ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        >
          <option value="">Any</option>
          {folders.map((folder) => (
            <option key={folder.id} value={folder.id}>
              {folder.name}
            </option>
          ))}
        </select>
      </div>
      <button type="submit" className="rounded border border-gray-300 px-3 py-1.5 text-sm">
        Filter
      </button>
      {hasFilters && (
        <a href="?" className="text-sm text-blue-600 hover:underline">
          Clear
        </a>
      )}
    </form>
  );
}
