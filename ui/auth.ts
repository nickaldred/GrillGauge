import NextAuth, { type NextAuthConfig } from "next-auth";
import Google from "next-auth/providers/google";
import { BASE_URL } from "./app/utils/envVars";
import type { User as GrillUser } from "./app/types/types";

async function fetchUserByEmail(email: string): Promise<GrillUser | null> {
  if (!BASE_URL) {
    console.error("BASE_URL is not defined; cannot fetch user by email.");
    return null;
  }

  const url = `${BASE_URL}/user?email=` + encodeURIComponent(email);

  try {
    const res = await fetch(url, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
    });

    if (!res.ok) {
      if (res.status !== 404) {
        console.error(`Failed to fetch user: ${res.status}`);
      }
      return null;
    }

    const data = await res.json();
    if (Array.isArray(data)) {
      return (data[0] as GrillUser | undefined) ?? null;
    }
    return (data as GrillUser | null) ?? null;
  } catch (err) {
    console.error("Error fetching user by email:", err);
    return null;
  }
}

export const authConfig: NextAuthConfig = {
  providers: [Google],
  callbacks: {
    async session({ session }) {
      const email = session.user?.email;
      if (!email) return session;

      const dbUser = await fetchUserByEmail(email);

      if (dbUser && session.user) {
        session.user.firstName = dbUser.firstName;
        session.user.lastName = dbUser.lastName;
      }

      return session;
    },
  },
};

export const { handlers, signIn, signOut, auth } = NextAuth(authConfig);