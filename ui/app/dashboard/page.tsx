"use client";

import { useSession } from "next-auth/react";
import { PageHeader } from "../components/pageHeader";
import { DashboardPage } from "./components/dashboardPage";
import { useRouter } from "next/navigation";
import { useTheme } from "../providers/ThemeProvider";

/**
 * The Dashboard page is the main page for authenticated users,
 * displaying their dashboard with hubs and probes.
 *
 * @returns The Dashboard page.
 */
export default function Dashboard() {
  // ** Router & Theme **
  const router = useRouter();
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // Unauthenticated users are redirected immediately.
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
