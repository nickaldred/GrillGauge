import { auth } from "@/auth";
import { redirect } from "next/navigation";

export default async function Secret() {
  const session = await auth();
  if (!session) redirect("/");
  const user = session?.user;

  return (
    <h1 className="text-2xl text-green-700">
      Welcome to my account: {user?.name}
    </h1>
  );
}
