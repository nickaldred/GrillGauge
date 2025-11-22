"use client";

import React from "react";
import { FlameIcon } from "lucide-react";
import { useTheme } from "../providers/ThemeProvider";

export default function Footer() {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  return (
    <footer
      className={`py-12 ${
        isDarkMode
          ? "bg-gray-900 border-t border-gray-800"
          : "bg-white border-t border-gray-200"
      } transition-colors duration-500`}
    >
      <div className="container mx-auto px-6">
        <div className="flex flex-col md:flex-row justify-between items-center">
          <div className="flex items-center space-x-2 mb-4 md:mb-0">
            <FlameIcon
              size={24}
              className={isDarkMode ? "text-orange-500" : "text-orange-600"}
            />
            <span
              className={`text-lg font-semibold ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              Grill Gauge
            </span>
          </div>
          <div
            className={`text-sm ${
              isDarkMode ? "text-gray-400" : "text-gray-600"
            }`}
          >
            © 2025 Grill Gauge. All rights reserved.
          </div>
        </div>
      </div>
    </footer>
  );
}
