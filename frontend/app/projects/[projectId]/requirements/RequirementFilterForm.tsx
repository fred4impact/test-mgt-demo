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
          defaultValue={q ?? ""}
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
          defaultValue={status ?? ""}
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
          defaultValue={priority ?? ""}
          className="mt-1 rounded-md border border-border bg-bg px-2.5 py-1.5 text-sm text-text"
        />
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
