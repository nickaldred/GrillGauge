import React from "react";
import { ThermometerIcon } from "lucide-react";
import { useTheme } from "@/app/providers/ThemeProvider";

interface PageHeaderProps {
  leftTitle: string;
  rightTitle?: string;
}

export function PageHeader({ leftTitle, rightTitle }: PageHeaderProps) {
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
          <ThermometerIcon
            size={32}
            className={`mr-3 ${isDarkMode ? "text-red-500" : "text-red-400"}`}
          />
          <h1
            className={`text-2xl font-bold ${
              isDarkMode ? "text-white" : "text-gray-900"
            }`}
          >
            {leftTitle}
          </h1>
        </div>
        <div className="flex items-center gap-4">
          <p
            className={`text-sm ${
              isDarkMode ? "text-gray-300" : "text-gray-500"
            }`}
          >
            {rightTitle}
          </p>
        </div>
      </div>
    </header>
  );
}
