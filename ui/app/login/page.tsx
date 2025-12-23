"use client";

import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { signIn, useSession } from "next-auth/react";
import { AlertCircleIcon, FlameIcon } from "lucide-react";
import { useTheme } from "../providers/ThemeProvider";
import { BASE_URL } from "../utils/envVars";
import { User } from "../types/types";

/**
 * The Login page allows users to sign in using Google authentication.
 *
 * @returns The Login page component.
 */
export default function Login() {
  // ** Session and Router **
  const { data: session, status } = useSession();
  const router = useRouter();

  const searchParams = useSearchParams();
  const from = searchParams.get("from") || "/dashboard";

  useEffect(() => {
    const checkUserAndRedirect = async () => {
      if (status !== "authenticated") return;

      const email = session?.user?.email;
      if (!email) {
        console.error("No email present in session user");
        setError("Unable to read email from session.");
        return;
      }

      const url = `${BASE_URL}/user?email=` + encodeURIComponent(email);
      try {
        const res = await fetch(url, {
          method: "GET",
          headers: { "Content-Type": "application/json" },
        });

        if (res.status === 404) {
          router.replace("/signup");
          return;
        }

        if (!res.ok) {
          throw new Error(`Failed to fetch user: ${res.status}`);
        }

        const data = await res.json();
        const userExists = Array.isArray(data)
          ? data.length > 0
          : Boolean(data);
        if (userExists) {
          router.replace(from);
        } else {
          router.replace("/signup");
        }
      } catch (err) {
        console.error("Error fetching user:", err);
        setError("Error checking user existence.");
      }
    };

    checkUserAndRedirect();
  }, [status, session, from, router]);

  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** States **
  const [error, setError] = useState("");

  // ** Handlers **
  const handleGoogleLogin = async (e: React.MouseEvent<HTMLButtonElement>) => {
    e.preventDefault();
    setError("");
    try {
      await signIn("google", {
        callbackUrl: `/login?from=${encodeURIComponent(from)}`,
      });
    } catch (err) {
      console.error(err);
      setError("An error occurred during Google login.");
    }
  };

  return (
    <div
      className={`min-h-screen ${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      } flex flex-col justify-center items-center p-4 transition-colors duration-500`}
    >
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8">
          <div className="bg-gradient-to-br from-orange-500 to-red-600 p-4 rounded-full shadow-md">
            <FlameIcon size={40} className="text-white" />
          </div>
        </div>

        <div
          className={`${
            isDarkMode
              ? "bg-gray-800 border-gray-700"
              : "bg-white border-gray-200"
          } rounded-xl shadow-lg p-8 border transition-colors duration-500`}
        >
          <div className="text-center mb-6">
            <h1
              className={`text-2xl font-bold ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              Grill Gauge
            </h1>
            <p
              className={`mt-2 ${
                isDarkMode ? "text-gray-400" : "text-gray-600"
              }`}
            >
              Sign in to access your dashboard
            </p>
          </div>

          {error && (
            <div
              className={`mb-4 p-3 rounded-lg flex items-center ${
                isDarkMode
                  ? "bg-red-500/20 border-red-500/50 text-red-400"
                  : "bg-red-50 border-red-200 text-red-700"
              } border`}
            >
              <AlertCircleIcon size={18} className="mr-2 flex-shrink-0" />
              <span>{error}</span>
            </div>
          )}

          <div className="space-y-4">
            <button
              onClick={handleGoogleLogin}
              disabled={status === "loading"}
              className={`w-full py-3 px-4 rounded-lg font-medium flex items-center justify-center transition-all cursor-pointer ${
                isDarkMode
                  ? "bg-white text-gray-900 hover:bg-gray-100"
                  : "bg-white text-gray-900 hover:bg-gray-50 border border-gray-300"
              } ${
                status === "loading" ? "opacity-70 cursor-not-allowed" : ""
              } shadow-sm hover:shadow-md`}
            >
              {status === "loading" ? (
                <span className="flex items-center">
                  <span className="animate-spin h-5 w-5 border-2 border-gray-900 border-t-transparent rounded-full mr-3"></span>
                  Signing in...
                </span>
              ) : (
                <>
                  <svg className="w-5 h-5 mr-3" viewBox="0 0 24 24">
                    <path
                      fill="#4285F4"
                      d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"
                    />
                    <path
                      fill="#34A853"
                      d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                    />
                    <path
                      fill="#FBBC05"
                      d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                    />
                    <path
                      fill="#EA4335"
                      d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                    />
                  </svg>
                  Continue with Google
                </>
              )}
            </button>

            <div
              className={`text-center text-sm ${
                isDarkMode ? "text-gray-500" : "text-gray-400"
              }`}
            >
              <div className="relative">
                <div className="absolute inset-0 flex items-center">
                  <div
                    className={`w-full border-t ${
                      isDarkMode ? "border-gray-700" : "border-gray-300"
                    }`}
                  ></div>
                </div>
                <div className="relative flex justify-center text-xs uppercase">
                  <span
                    className={`px-2 ${
                      isDarkMode
                        ? "bg-gray-800 text-gray-500"
                        : "bg-white text-gray-500"
                    }`}
                  >
                    More options coming soon
                  </span>
                </div>
              </div>
            </div>
          </div>

          <div
            className={`mt-6 text-center text-sm ${
              isDarkMode ? "text-gray-400" : "text-gray-600"
            }`}
          >
            <Link
              href="/"
              className={`inline-block mt-2 ${
                isDarkMode
                  ? "text-orange-400 hover:text-orange-300"
                  : "text-orange-600 hover:text-orange-700"
              }`}
            >
              ← Back to home
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
