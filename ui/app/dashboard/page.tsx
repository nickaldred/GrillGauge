"use client";

import { useSession } from "next-auth/react";
import { PageHeader } from "../components/pageHeader";
import { DashboardPage } from "./components/dashboardPage";
import { useRouter } from "next/navigation";
import { useTheme } from "../providers/ThemeProvider";
import { useRequireAuth } from "../utils/useRequireAuth";

/**
 * The Dashboard page is the main page for authenticated users,
 * displaying their dashboard with hubs and probes.
 *
 * @returns The Dashboard page.
 */
export default function Dashboard() {
  // ** Router & Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";
  const { status } = useRequireAuth("/");

  // While session isn't authenticated, don't render the dashboard UI to avoid
  // a flash of protected content before the redirect.
  if (status !== "authenticated") {
    return null;
  }

  return (
    <div
      className={`${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      } w-full flex-1 flex flex-col p-6`}
    >
      <div className="container mx-auto px-4 py-4">
        <PageHeader
          leftTitle="Dashboard"
          rightTitle="All temperatures shown in °F"
        />
        <DashboardPage />
      </div>
    </div>
  );
}
