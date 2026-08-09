import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listRoles } from "@/services/roles";
import { CreateRoleForm } from "./CreateRoleForm";

export default async function NewRolePage() {
  const session = await auth();

  if (!session?.accessToken) {
    redirect("/api/auth/signin?callbackUrl=/roles/new");
  }

  const roles = await listRoles(session.accessToken);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Create a role</h1>
      <CreateRoleForm />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing roles</h2>
      <ul className="space-y-1">
        {roles.map((role) => (
          <li key={role.id} className="text-sm">
            {role.name}
            {role.systemRole && <span className="text-gray-400"> - system role</span>}
          </li>
        ))}
        {roles.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
