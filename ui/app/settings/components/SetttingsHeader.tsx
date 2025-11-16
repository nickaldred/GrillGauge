import React from "react";
import { SettingsIcon } from "lucide-react";
import { useTheme } from "@/app/providers/ThemeProvider";
export function Header() {
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  return (
    <header
      className={`${
        isDarkMode ? "bg-gray-800 border-gray-700" : "bg-white border-gray-200"
      } rounded-xl shadow-lg p-5 mb-6 border`}
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <SettingsIcon
            size={32}
            className={`mr-3 ${isDarkMode ? "text-red-500" : "text-red-400"}`}
          />
          <h1
            className={`text-2xl font-bold ${
              isDarkMode ? "text-white" : "text-gray-900"
            }`}
          >
            Settings
          </h1>
        </div>
      </div>
    </header>
  );
}
