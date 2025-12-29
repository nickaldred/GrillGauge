"use client";

import { MdOutlineOutdoorGrill } from "react-icons/md";
import { useTheme } from "../providers/ThemeProvider";

type LoadingStateProps = {
  message?: string;
  occupyFullHeight?: boolean;
};

/** The LoadingState component displays a loading indicator with an optional message. */
export function LoadingState({
  message = "Loading",
  occupyFullHeight = true,
}: LoadingStateProps) {
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  return (
    <div
      className={`flex items-center justify-center ${
        occupyFullHeight ? "min-h-[60vh]" : ""
      }`}
    >
      <div
        role="status"
        aria-live="polite"
        className="flex flex-col items-center"
      >
        <div className="flex flex-col items-center gap-1 animate-pulse opacity-90">
          <MdOutlineOutdoorGrill
            className={`w-20 h-20 ${
              isDarkMode ? "text-white" : "text-gray-800"
            }`}
            aria-hidden
          />
          <div
            className={`font-medium ${
              isDarkMode ? "text-gray-200" : "text-gray-700"
            }`}
          >
            {message}
          </div>
        </div>
      </div>
    </div>
  );
}
