"use client";

import React, { useEffect, useState } from "react";
import { useRouter, useSearchParams } from "next/navigation";
import Link from "next/link";
import { useSession, signOut } from "next-auth/react";
import { AlertCircleIcon, FlameIcon, UserIcon } from "lucide-react";
import { useTheme } from "../providers/ThemeProvider";
import { BASE_URL } from "../utils/envVars";
import { postRequest } from "../utils/requestUtils";

/** Signup page component */
export default function Signup() {
  // ** Session and Router **
  const { data: session, status } = useSession();
  const router = useRouter();
  const searchParams = useSearchParams();
  const from = searchParams.get("from") || "/dashboard";

  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** State **
  const [acceptedTerms, setAcceptedTerms] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    if (status === "unauthenticated") {
      router.replace("/login?from=/signup");
    }
  }, [status, router]);

  const handleCreateAccount = async (
    e: React.MouseEvent<HTMLButtonElement>
  ) => {
    e.preventDefault();
    setError("");

    if (!acceptedTerms) {
      setError("You must agree to the terms and conditions to continue.");
      return;
    }

    const email = session?.user?.email;
    const displayName = session?.user?.name || "";

    if (!email) {
      setError("Unable to read your email from the session.");
      return;
    }

    let firstName = "";
    let lastName = "";

    if (displayName.trim()) {
      const parts = displayName.trim().split(/\s+/);
      firstName = parts[0];
      lastName = parts.slice(1).join(" ") || parts[0];
    } else {
      const localPart = email.split("@")[0];
      firstName = localPart || "GrillGauge";
      lastName = "User";
    }

    try {
      setIsSubmitting(true);

      await postRequest(`${BASE_URL}/user`, {
        email,
        firstName,
        lastName,
      });

      router.replace(from);
    } catch (err) {
      console.error("Error creating user", err);
      setError("Failed to create your account. Please try again.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const fullName = session?.user?.name;
  const email = session?.user?.email;

  const handleCancelSignup = async (e: React.MouseEvent<HTMLAnchorElement>) => {
    e.preventDefault();
    await signOut({ callbackUrl: "/" });
  };

  return (
    <div
      className={`${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      } w-full flex-1 flex flex-col items-center justify-center p-4 py-10 transition-colors duration-500`}
    >
      <div className="w-full max-w-md">
        <div className="flex justify-center mb-8">
          <div className="bg-gradient-to-br from-orange-500 to-red-600 p-4 rounded-full shadow-md flex items-center justify-center">
            <FlameIcon size={32} className="text-white mr-2" />
            <UserIcon size={24} className="text-white" />
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
              Welcome to Grill Gauge
            </h1>
            <p
              className={`mt-2 text-sm ${
                isDarkMode ? "text-gray-400" : "text-gray-600"
              }`}
            >
              You&apos;re almost ready to start tracking your cooks. We&apos;ll
              create your Grill Gauge account using your Google profile.
            </p>
          </div>

          {status === "loading" && (
            <p
              className={`mb-4 text-sm text-center ${
                isDarkMode ? "text-gray-400" : "text-gray-600"
              }`}
            >
              Checking your session...
            </p>
          )}

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

          <div
            className={`mb-4 rounded-lg p-3 text-sm ${
              isDarkMode
                ? "bg-gray-900/40 border border-gray-700 text-gray-200"
                : "bg-gray-50 border border-gray-200 text-gray-800"
            }`}
          >
            <p className="font-medium mb-1">Signing up as:</p>
            <p className="truncate">
              <span className="font-semibold">{fullName || "Google user"}</span>
            </p>
            {email && (
              <p className="text-xs mt-1 text-gray-500 truncate">{email}</p>
            )}
            <p className="text-xs mt-2 text-gray-500">
              You don&apos;t need to enter any details now. We&apos;ll use this
              information to create your Grill Gauge account.
            </p>
          </div>

          <div className="space-y-4">
            <label className="flex items-start space-x-2 text-sm cursor-pointer">
              <input
                type="checkbox"
                className="mt-1 h-4 w-4"
                checked={acceptedTerms}
                onChange={(e) => setAcceptedTerms(e.target.checked)}
              />
              <span className={isDarkMode ? "text-gray-300" : "text-gray-700"}>
                I agree to the
                <span className="font-semibold"> Terms &amp; Conditions </span>
                and
                <span className="font-semibold"> Privacy Policy </span>
                of Grill Gauge.
              </span>
            </label>

            <button
              onClick={handleCreateAccount}
              disabled={
                !acceptedTerms || isSubmitting || status !== "authenticated"
              }
              className={`w-full py-3 px-4 rounded-lg font-medium flex items-center justify-center transition-all ${
                isDarkMode
                  ? "bg-orange-500 text-white hover:bg-orange-400"
                  : "bg-orange-600 text-white hover:bg-orange-500"
              } ${
                !acceptedTerms || isSubmitting || status !== "authenticated"
                  ? "opacity-70 cursor-not-allowed"
                  : "cursor-pointer shadow-sm hover:shadow-md"
              }`}
            >
              {isSubmitting ? (
                <span className="flex items-center">
                  <span className="animate-spin h-5 w-5 border-2 border-white border-t-transparent rounded-full mr-3"></span>
                  Creating your account...
                </span>
              ) : (
                "Create my Grill Gauge account"
              )}
            </button>
          </div>

          <div
            className={`mt-6 text-center text-xs ${
              isDarkMode ? "text-gray-500" : "text-gray-500"
            }`}
          >
            <p>
              You can manage your account details later from the dashboard
              settings.
            </p>
            <Link
              href="/"
              onClick={handleCancelSignup}
              className={`inline-block mt-3 ${
                isDarkMode
                  ? "text-orange-400 hover:text-orange-300"
                  : "text-orange-600 hover:text-orange-700"
              }`}
            >
              ← Cancel signup and sign out
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
