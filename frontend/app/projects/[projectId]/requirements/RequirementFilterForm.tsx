export function RequirementFilterForm({
  q,
  status,
  priority,
}: {
  q?: string;
  status?: string;
  priority?: string;
}) {
  const hasFilters = Boolean(q || status || priority);

  return (
    <form method="get" className="flex flex-wrap items-end gap-2">
      <div>
        <label htmlFor="q" className="block text-xs font-medium text-gray-500">
          Keyword
        </label>
        <input
          id="q"
          name="q"
          defaultValue={q ?? ""}
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
          defaultValue={status ?? ""}
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
          defaultValue={priority ?? ""}
          className="mt-1 rounded border border-gray-300 px-2 py-1 text-sm"
        />
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
