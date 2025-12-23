"use client";

import { PageHeader } from "../components/pageHeader";
import { useTheme } from "../providers/ThemeProvider";
import { HubManagement } from "./components/HubManagement";

/**
 * The Settings page component.
 *
 * @returns The Settings page.
 */
export default function Settings() {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  return (
    <div
      className={`${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      } w-full flex-1 flex flex-col p-6`}
    >
      <div className={`container mx-auto px-4 py-4`}>
        <PageHeader leftTitle="Settings" />
        <HubManagement />
      </div>
    </div>
  );
}
