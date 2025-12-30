import NextAuth, { type NextAuthConfig } from "next-auth";
import Google from "next-auth/providers/google";
import jwt from "jsonwebtoken";
import { Buffer } from "node:buffer";
import { BASE_URL } from "./app/utils/envVars";
import type { User as GrillUser } from "./app/types/types";

/**
 * Secret key for signing JWTs.
 * Must be set in the environment variable JWT_SECRET.
 */
const JWT_SECRET = (() => {
  const secret = process.env.JWT_SECRET;
  if (!secret) {
    throw new Error("JWT_SECRET environment variable is required");
  }
  return secret;
})();

const MAX_PROFILE_IMAGE_BYTES = 1_000_000; // ~1 MB safety limit

/**
 * Fetches an image from a URL and converts it to a data URL (base64-encoded).
 *
 * @param imageUrl The URL of the image to fetch.
 * @returns A promise that resolves to the data URL string, or null if fetching/conversion fails.
 */
async function fetchImageDataUrl(imageUrl: string): Promise<string | null> {
  try {
    const res = await fetch(imageUrl);
    if (!res.ok) {
      console.warn(`Failed to fetch profile image: ${res.status}`);
      return null;
    }

    const contentLength = res.headers.get("content-length");
    if (contentLength && Number(contentLength) > MAX_PROFILE_IMAGE_BYTES) {
      console.warn("Profile image too large to cache in session token.");
      return null;
    }

    const arrayBuffer = await res.arrayBuffer();
    if (arrayBuffer.byteLength > MAX_PROFILE_IMAGE_BYTES) {
      console.warn("Profile image too large to cache in session token.");
      return null;
    }

    const contentType = res.headers.get("content-type") || "image/jpeg";
    const base64 = Buffer.from(arrayBuffer).toString("base64");
    return `data:${contentType};base64,${base64}`;
  } catch (err) {
    console.warn("Could not cache profile image", err);
    return null;
  }
}

/**
 * Fetches a user from the Grill Gauge API by email.
 *
 * @param email The email of the user to fetch.
 * @returns A promise that resolves to the GrillUser object or null if not found/error.
 */
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

/**
 * Determines whether to force a refresh of cached user data based on the trigger and session.
 *
 * @param trigger The trigger string from NextAuth.
 * @param session The NextAuth session object.
 * @returns True if a refresh should be forced, false otherwise.
 */
function shouldForceRefresh(
  trigger: string | undefined,
  session: Record<string, unknown> | null | undefined
): boolean {
  return trigger === "update" && Boolean(session?.refreshUser);
}

/**
 * Hydrates the email into the token from the user or existing token.
 *
 * @param token The JWT token object.
 * @param user The user object from sign-in (may be null).
 * @returns The hydrated email string or undefined.
 */
function hydrateEmail(
  token: Record<string, unknown>,
  user: unknown
): string | undefined {
  const emailFromUser = (user as { email?: string } | undefined)?.email ?? undefined;
  const emailFromToken = (token.email as string | undefined) ?? undefined;
  const email = emailFromUser ?? emailFromToken;
  if (email) {
    token.email = email;
  }
  return email;
}

/**
 * Fetches and caches the provider profile image into the token if missing or forced to refresh.
 *
 * @param token The JWT token object.
 * @param forceRefresh Whether to force refresh the cached data.
 * @returns A promise that resolves when caching is complete.
 */
async function cacheProviderImage(token: Record<string, unknown>, forceRefresh: boolean) {
  const picture = token.picture as string | undefined;
  const existingDataUrl = token.profileImageData as string | undefined;

  if ((forceRefresh || !existingDataUrl) && picture) {
    const dataUrl = await fetchImageDataUrl(picture);
    if (dataUrl) {
      token.profileImageData = dataUrl;
    }
  }
}

/**
 * Fetches and caches user fields (firstName, lastName, roles) into the token if missing 
 * or forced to refresh.
 *
 * @param token The JWT token object.
 * @param email The email of the user.
 * @param forceRefresh Whether to force refresh the cached data.
 * @returns A promise that resolves when caching is complete.
 */
async function cacheUserFields(
  token: Record<string, unknown>,
  email: string | undefined,
  forceRefresh: boolean
) {
  const tokenFirstName = token.firstName as string | undefined;
  const tokenLastName = token.lastName as string | undefined;
  const tokenRoles = token.roles as string[] | undefined;

  const hasNames = Boolean(tokenFirstName) || Boolean(tokenLastName);
  const hasRoles = Array.isArray(tokenRoles) && tokenRoles.length > 0;

  if (!email) return;
  if (!forceRefresh && hasNames && hasRoles) return;

  const dbUser = await fetchUserByEmail(email);
  if (dbUser) {
    token.firstName = dbUser.firstName;
    token.lastName = dbUser.lastName;
    token.roles = dbUser.roles;
  }
}

/**
 * Builds and enriches the session user object with data from the token and Grill Gauge API.
 * Also signs a JWT for API authentication and attaches it to the session.
 * 
 * @param session The NextAuth session object.
 * @param token The JWT token object.
 * @param email The email of the user.
 * @returns A promise that resolves to the enriched session object.
 */
async function buildSessionUser(
  session: any,
  token: Record<string, unknown>,
  email: string
) {
  const profileImageData = token.profileImageData as string | undefined;
  const picture = token.picture as string | undefined;
  const tokenFirstName = token.firstName as string | undefined;
  const tokenLastName = token.lastName as string | undefined;
  const tokenRoles = token.roles as string[] | undefined;

  let dbUser: GrillUser | null = null;
  if ((!tokenFirstName || !tokenLastName || !tokenRoles?.length) && email) {
    dbUser = await fetchUserByEmail(email);
  }

  const firstName = tokenFirstName ?? dbUser?.firstName;
  const lastName = tokenLastName ?? dbUser?.lastName;
  const roles =
    tokenRoles && tokenRoles.length > 0
      ? tokenRoles
      : dbUser?.roles && dbUser.roles.length > 0
        ? dbUser.roles
        : ["USER"];

  if (session.user) {
    if (firstName) session.user.firstName = firstName;
    if (lastName) session.user.lastName = lastName;
    session.user.roles = roles;

    session.user.image = profileImageData ?? picture ?? session.user.image;
    if (profileImageData) {
      session.user.imageData = profileImageData;
    }
  }

  if (session.user) {
    const payload = {
      sub: email,
      email,
      name: `${session.user.firstName ?? ""} ${session.user.lastName ?? ""}`.trim(),
      roles,
    };

    const signed = jwt.sign(payload, JWT_SECRET, {
      algorithm: "HS256",
      expiresIn: "1h",
    });
    session.apiToken = signed;
  }

  return session;
}

/**
 * NextAuth configuration for Google authentication and session handling.
 * Includes JWT and session callbacks to enrich the session with user data
 * fetched from the Grill Gauge API.
 * 
 * @returns NextAuth configuration object.
 */
export const authConfig: NextAuthConfig = {
  providers: [Google],
  callbacks: {
    async jwt({ token, user, trigger, session }) {
      const forceRefresh = shouldForceRefresh(trigger, session);
      const email = hydrateEmail(token, user);

      await cacheProviderImage(token, forceRefresh);
      await cacheUserFields(token, email, forceRefresh);

      return token;
    },

    // Prefers cached token values and falls back to API when missing.
    async session({ session, token }) {
      const email = session.user?.email ?? ((token as Record<string, unknown>).email as string | undefined);
      if (!email) return session;

      const enriched = await buildSessionUser(session, token, email);
      return enriched;
    },
  },
};

export const { handlers, signIn, signOut, auth } = NextAuth(authConfig);