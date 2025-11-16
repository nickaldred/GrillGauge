"use client";

import { useSession } from "next-auth/react";
import { PageHeader } from "../components/pageHeader";
import { DashboardPage } from "./components/dashboardPage";
import { useRouter } from "next/navigation";
// no React hooks from 'react' are needed here
import { useTheme } from "../providers/ThemeProvider";

export default function Dashboard() {
  const router = useRouter();
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // Use `required: true` so unauthenticated users are redirected immediately.
  const { status } = useSession({
    required: true,
    onUnauthenticated() {
      router.replace("/");
    },
  });

  // While session isn't authenticated, don't render the dashboard UI to avoid
  // a flash of protected content before the redirect.
  if (status !== "authenticated") {
    return null;
  }

  return (
    <main
      className={`min-h-screen p-6 ${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      }`}
    >
      <div className="container mx-auto px-4 py-4">
        <PageHeader
          leftTitle="Dashboard"
          rightTitle="All temperatures shown in °F"
        />
        <DashboardPage />
      </div>
    </main>
  );
}
