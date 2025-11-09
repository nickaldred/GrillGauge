"use client";

import { useEffect } from "react";
import { signIn, signOut, useSession } from "next-auth/react";

export default function GoogleSignInButton() {
  const { data: session, status } = useSession();
  const user = session?.user;

  // optionally keep dev console parity with previous server component
  useEffect(() => {
    if (process.env.NODE_ENV === "development")
      console.log("session", session, "status", status);
  }, [session, status]);

  return user ? (
    <>
      <h1 className="text-2xl">Welcome {user.name}</h1>
      <button
        onClick={async (e) => {
          e.preventDefault();
          await signOut();
        }}
        className="p-2 border-2 bg-blue-400"
      >
        Sign Out
      </button>
    </>
  ) : (
    <>
      <h1 className="text-2xl">Sign In</h1>
      <button
        onClick={async (e) => {
          e.preventDefault();
          await signIn("google");
        }}
        className="p-2 border-2 bg-blue-400"
      >
        Sign In
      </button>
    </>
  );
}
