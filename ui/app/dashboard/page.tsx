"use client";

import { useSession } from "next-auth/react";
import { Header } from "./components/dashboardHeader";
import { DashboardPage } from "./components/dashboardPage";
import { useRouter } from "next/navigation";
import { useEffect } from "react";

export default function Dashboard() {
  const { status } = useSession();
  const router = useRouter();

  useEffect(() => {
    if (status === "unauthenticated") {
      router.replace("/"); // Redirect to home if not logged in
    }
  }, [status, router]);

  return (
    <main className="p-6">
      <Header />
      <DashboardPage />
    </main>
  );
}
