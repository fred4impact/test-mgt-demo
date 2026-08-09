import { listOrganizations } from "@/services/organizations";
import { CreateOrganizationForm } from "./CreateOrganizationForm";

export default async function NewOrganizationPage() {
  const organizations = await listOrganizations();

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Create an organization</h1>
      <CreateOrganizationForm />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing organizations</h2>
      <ul className="space-y-1">
        {organizations.map((org) => (
          <li key={org.id} className="text-sm">
            {org.name} <span className="text-gray-400">({org.slug})</span>
          </li>
        ))}
        {organizations.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
