"use client";

import { useRouter } from "next/navigation";
import { useSession } from "next-auth/react";

export function useRequireAuth(redirectTo: string = "/") {
  const router = useRouter();

  const { data: session, status } = useSession({
    required: true,
    onUnauthenticated() {
      router.replace(redirectTo);
    },
  });

  return { session, status };
}
