"use client";

import { useTheme } from "../providers/ThemeProvider";
import { HubManagement } from "./components/HubManagement";
import { Header } from "./components/SetttingsHeader";

export default function Settings() {
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  return (
    <main
      className={`${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      } min-h-screen flex flex-col`}
    >
      <div className={`container mx-auto px-4 py-4`}>
        <Header />
        <HubManagement />
      </div>
    </main>
  );
}
