import { auth } from "@/auth";
import { redirect } from "next/navigation";

/**
 * The Profile page component.
 *
 * @returns The Profile page.
 */
export default async function Profile() {
  const session = await auth();
  if (!session) redirect("/");
  const user = session?.user;

  return (
    <h1 className="text-2xl text-green-700">
      Welcome to my account: {user?.name}
    </h1>
  );
}
