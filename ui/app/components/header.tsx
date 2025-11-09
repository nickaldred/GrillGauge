"use client";
import React from "react";
import { useRouter, usePathname } from "next/navigation";
import { FlameIcon } from "lucide-react";
import { useTheme } from "../providers/ThemeProvider";

// Shared site header (derived from LandingPage nav). This component is a
// client component so it can manage theme toggling and client navigation.
export function Header() {
  const router = useRouter();
  const pathname = usePathname();
  const { theme, toggle } = useTheme();

  // Keep landing page as-is (it renders its own nav). When this header is
  // rendered on the root path we'll return null to avoid duplicate navs.
  if (pathname === "/") return null;

  return (
    <header
      className={`w-full bg-white/80 border-b border-gray-200 backdrop-blur-md dark:bg-gray-900/80 dark:border-gray-800`}
    >
      <div className="container mx-auto px-6 py-4">
        <div className="flex justify-between items-center">
          <div className="flex items-center space-x-2">
            <div className={`p-2 rounded-lg bg-gradient-to-br from-orange-400 to-red-500`}>
              <FlameIcon size={20} className="text-white" />
            </div>
            <span className={`text-xl font-bold dark:text-white text-gray-900`}>Grill Gauge</span>
          </div>

          <div className="flex items-center space-x-4">
            <button
              onClick={toggle}
              className="p-2 rounded-lg transition-colors bg-gray-200 dark:bg-gray-800"
              aria-label="Toggle theme"
            >
              {theme === "dark" ? (
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="feather feather-sun">
                  <circle cx="12" cy="12" r="5"></circle>
                  <line x1="12" y1="1" x2="12" y2="3"></line>
                  <line x1="12" y1="21" x2="12" y2="23"></line>
                  <line x1="4.22" y1="4.22" x2="5.64" y2="5.64"></line>
                  <line x1="18.36" y1="18.36" x2="19.78" y2="19.78"></line>
                  <line x1="1" y1="12" x2="3" y2="12"></line>
                  <line x1="21" y1="12" x2="23" y2="12"></line>
                  <line x1="4.22" y1="19.78" x2="5.64" y2="18.36"></line>
                  <line x1="18.36" y1="5.64" x2="19.78" y2="4.22"></line>
                </svg>
              ) : (
                <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" className="feather feather-moon">
                  <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
                </svg>
              )}
            </button>
            <button
              onClick={() => router.push("/login")}
              className="px-4 py-2 rounded-lg font-medium bg-gradient-to-r from-orange-500 to-red-600 text-white"
            >
              Sign In
            </button>
          </div>
        </div>
      </div>
    </header>
  );
}
