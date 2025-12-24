"use client";
import React from "react";
import { usePathname } from "next/navigation";
import Link from "next/link";
import { FlameIcon, SettingsIcon } from "lucide-react";
import { useTheme } from "../providers/ThemeProvider";
import GoogleSignInButton from "./googleSignIn";
import { useRouter } from "next/navigation";

export function Header() {
  // ** Theme **
  const { theme, toggle } = useTheme();
  const isDarkMode = theme === "dark";

  // ** Router **
  const router = useRouter();

  // Do not render header on specific routes
  const pathname = usePathname();
  if (pathname === "/") return null;
  if (pathname === "/login") return null;
  const isDashboardActive = pathname === "/dashboard";
  const isSettingsActive = pathname ? pathname.startsWith("/settings") : false;

  const handleLogoClick = () => {
    router.push("/dashboard");
  };

  // ** Nav Link Classnames **
  const navClass = (isActive: boolean, withFlex = false) =>
    `px-4 py-2 rounded-lg${withFlex ? " flex items-center" : ""} ${
      isActive
        ? isDarkMode
          ? "bg-orange-500/20 text-orange-400"
          : "bg-red-100 text-red-600"
        : isDarkMode
        ? "text-gray-300 hover:bg-gray-700"
        : "hover:bg-gray-100"
    }`;

  return (
    <header
      className={`w-full ${
        isDarkMode ? "bg-gray-800 border-gray-700" : "bg-white border-gray-200"
      } backdrop-blur-md`}
    >
      <div className="container mx-auto px-6 py-4">
        <div className="flex justify-between items-center">
          <button
            onClick={handleLogoClick}
            className="group flex items-center space-x-2 cursor-pointer"
          >
            <div
              className={`p-2 rounded-lg bg-gradient-to-br from-orange-400 to-red-500`}
            >
              <FlameIcon size={20} className="text-white flame-flicker" />
            </div>
            <span
              className={`text-xl font-bold ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              Grill Gauge
            </span>
          </button>

          <div className="flex items-center space-x-4">
            <Link href="/dashboard" className={navClass(isDashboardActive)}>
              Dashboard
            </Link>
            <Link href="/settings" className={navClass(isSettingsActive, true)}>
              <SettingsIcon size={18} className="mr-2" />
              Settings
            </Link>
            <button
              onClick={toggle}
              className={`p-2 rounded-lg transition-colors cursor-pointer ${
                isDarkMode
                  ? "bg-gray-800 text-yellow-400 hover:bg-gray-700"
                  : "bg-gray-200 text-gray-700 hover:bg-gray-300"
              }`}
              aria-label="Toggle theme"
            >
              {isDarkMode ? (
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="feather feather-sun"
                >
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
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  width="20"
                  height="20"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  className="feather feather-moon"
                >
                  <path d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z"></path>
                </svg>
              )}
            </button>
            <div
              className={`border-l ${
                isDarkMode ? "border-gray-700" : "border-gray-300"
              } h-8 mx-2`}
            ></div>
            <GoogleSignInButton />
          </div>
        </div>
      </div>
    </header>
  );
}
