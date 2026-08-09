import { redirect } from "next/navigation";
import { auth } from "@/auth";
import { listTeams } from "@/services/teams";
import { CreateTeamForm } from "./CreateTeamForm";

export default async function NewTeamPage() {
  const session = await auth();

  if (!session?.accessToken) {
    redirect("/api/auth/signin?callbackUrl=/teams/new");
  }

  const teams = await listTeams(session.accessToken);

  return (
    <main className="mx-auto max-w-md p-8">
      <h1 className="mb-4 text-xl font-semibold">Create a team</h1>
      <CreateTeamForm />

      <h2 className="mb-2 mt-8 text-sm font-medium text-gray-500">Existing teams</h2>
      <ul className="space-y-1">
        {teams.map((team) => (
          <li key={team.id} className="text-sm">
            {team.name}
            {team.description && <span className="text-gray-400"> - {team.description}</span>}
          </li>
        ))}
        {teams.length === 0 && <li className="text-sm text-gray-400">None yet.</li>}
      </ul>
    </main>
  );
}
