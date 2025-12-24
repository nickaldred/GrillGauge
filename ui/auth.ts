import NextAuth, { type NextAuthConfig } from "next-auth";
import Google from "next-auth/providers/google";
import jwt from "jsonwebtoken";
import { BASE_URL } from "./app/utils/envVars";
import type { User as GrillUser } from "./app/types/types";

// Shared secret for signing JWTs towards the Spring API.
// In production, override via process.env.JWT_SECRET.
// Must be at least 256 bits (32 bytes) for HS256 to be valid.
const JWT_SECRET =
  process.env.JWT_SECRET ?? "dev-test-jwt-secret-change-me-0123456789ABCDEF";

async function fetchUserByEmail(email: string): Promise<GrillUser | null> {
  if (!BASE_URL) {
    console.error("BASE_URL is not defined; cannot fetch user by email.");
    return null;
  }

  const url = `${BASE_URL}/user?email=` + encodeURIComponent(email);

  try {
    const token = jwt.sign(
      { sub: email, email, roles: ["USER"] },
      JWT_SECRET,
      { algorithm: "HS256", expiresIn: "5m" }
    );

    const res = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
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
    async session({ session, token }) {
      const email = session.user?.email;
      if (!email) return session;

      const dbUser = await fetchUserByEmail(email);

      if (dbUser && session.user) {
        session.user.firstName = dbUser.firstName;
        session.user.lastName = dbUser.lastName;
      }

      if (session.user) {
        // Simple example: treat a hard-coded list of emails as admins.
        const adminEmails = (process.env.ADMIN_EMAILS || "").split(",").map((e) => e.trim());
        const isAdmin = adminEmails.includes(email ?? "");

        const payload = {
          sub: email,
          email,
          name: `${session.user.firstName ?? ""} ${session.user.lastName ?? ""}`.trim(),
          roles: isAdmin ? ["ADMIN"] : ["USER"],
        };

        const signed = jwt.sign(payload, JWT_SECRET, {
          algorithm: "HS256",
          expiresIn: "1h",
        });
        // @ts-expect-error augment session with apiToken
        session.apiToken = signed;
      }

      return session;
    },
  },
};

export const { handlers, signIn, signOut, auth } = NextAuth(authConfig);