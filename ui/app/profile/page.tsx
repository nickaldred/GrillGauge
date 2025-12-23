"use client";

import React, { useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { PageHeader } from "../components/pageHeader";
import { useTheme } from "../providers/ThemeProvider";
import { BASE_URL } from "../utils/envVars";
import type { User } from "../types/types";
import { MailIcon, IdCard, UserIcon, ImageIcon } from "lucide-react";

/**
 * Profile page showing session info combined with user data
 * fetched from the backend controller.
 */
export default function Profile() {
  // ** Router & Theme **
  const router = useRouter();
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** Auth Session **
  const { data: session, status } = useSession({
    required: true,
    onUnauthenticated() {
      router.replace("/");
    },
  });

  // ** State **
  const [userData, setUserData] = useState<User | null>(null);
  const [isLoadingUser, setIsLoadingUser] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchUser = async () => {
      if (status !== "authenticated") return;

      const email = session?.user?.email;
      if (!email) {
        setError("Unable to read email from session.");
        setIsLoadingUser(false);
        return;
      }

      if (!BASE_URL) {
        setError("API base URL is not configured.");
        setIsLoadingUser(false);
        return;
      }

      try {
        const url = `${BASE_URL}/user?email=${encodeURIComponent(email)}`;
        const res = await fetch(url, {
          method: "GET",
          headers: { "Content-Type": "application/json" },
        });

        if (res.status === 404) {
          setError("User not found in Grill Gauge database.");
          setIsLoadingUser(false);
          return;
        }

        if (!res.ok) {
          throw new Error(`Failed to fetch user: ${res.status}`);
        }

        const data = await res.json();
        const userFromApi: User | null = Array.isArray(data)
          ? data[0] ?? null
          : data;

        setUserData(userFromApi);
      } catch (err) {
        console.error("Error fetching user profile:", err);
        setError("Error loading profile information.");
      } finally {
        setIsLoadingUser(false);
      }
    };

    fetchUser();
  }, [status, session]);

  // Avoid flash of content while auth is resolving.
  if (status !== "authenticated") {
    return null;
  }

  const profileImage = session?.user?.image ?? undefined;
  const displayName =
    (userData && `${userData.firstName} ${userData.lastName}`) ||
    session?.user?.name ||
    "User";
  const email = userData?.email ?? session?.user?.email ?? "Unknown";
  const userId = userData?.email ?? session?.user?.email ?? "Unknown";

  const firstName = userData?.firstName
    ? userData.firstName.charAt(0).toUpperCase() +
      userData.firstName.slice(1).toLowerCase()
    : "-";

  const lastName = userData?.lastName
    ? userData.lastName.charAt(0).toUpperCase() +
      userData.lastName.slice(1).toLowerCase()
    : "-";

  return (
    <div
      className={`${
        isDarkMode ? "bg-gray-900" : "bg-gray-100"
      } w-full flex-1 flex flex-col p-6`}
    >
      <div className="container mx-auto px-4 py-4">
        <PageHeader leftTitle="Profile" />

        <div
          className={`${
            isDarkMode
              ? "bg-gray-800 border-gray-700"
              : "bg-white border-gray-200"
          } rounded-xl shadow-lg p-6 border flex flex-col md:flex-row gap-8`}
        >
          {/* Left column: avatar & high-level info */}
          <div className="md:w-1/3 flex flex-col items-center md:border-r md:border-gray-700/40 pb-6 md:pb-0">
            <div className="w-28 h-28 rounded-full overflow-hidden border-4 border-orange-500/70 shadow-lg flex items-center justify-center bg-gray-900/40">
              {profileImage ? (
                // eslint-disable-next-line @next/next/no-img-element
                <img
                  src={profileImage}
                  alt={displayName}
                  className="w-full h-full object-cover"
                />
              ) : (
                <UserIcon
                  size={40}
                  className={isDarkMode ? "text-gray-300" : "text-gray-500"}
                />
              )}
            </div>
            <h2
              className={`mt-4 text-xl font-semibold ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              {displayName}
            </h2>
            <p
              className={`mt-1 text-sm ${
                isDarkMode ? "text-gray-300" : "text-gray-600"
              }`}
            >
              {email}
            </p>
          </div>

          {/* Right column: detailed fields */}
          <div className="md:w-2/3 flex flex-col gap-4">
            {isLoadingUser && !error && (
              <p className={isDarkMode ? "text-gray-300" : "text-gray-600"}>
                Loading your profile information...
              </p>
            )}

            {error && (
              <div
                className={`p-3 rounded-lg border ${
                  isDarkMode
                    ? "border-red-500/50 bg-red-500/10 text-red-300"
                    : "border-red-200 bg-red-50 text-red-700"
                }`}
              >
                {error}
              </div>
            )}

            {!isLoadingUser && !error && (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <ProfileField label="First Name" value={firstName} />
                <ProfileField label="Last Name" value={lastName} />
                <ProfileField
                  icon={<MailIcon size={16} />}
                  label="Email"
                  value={email}
                />
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

interface ProfileFieldProps {
  label: string;
  value: string;
  icon?: React.ReactNode;
}

function ProfileField({ label, value, icon }: ProfileFieldProps) {
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  return (
    <div
      className={`rounded-lg p-3 border flex flex-col gap-1 ${
        isDarkMode
          ? "border-gray-700 bg-gray-900/40"
          : "border-gray-200 bg-gray-50"
      }`}
    >
      <div className="flex items-center gap-2 text-xs font-medium uppercase tracking-wide text-gray-500">
        {icon && <span className="text-orange-500">{icon}</span>}
        <span className={isDarkMode ? "text-gray-400" : "text-gray-500"}>
          {label}
        </span>
      </div>
      <div
        className={`text-sm font-medium ${
          isDarkMode ? "text-white" : "text-gray-900"
        }`}
      >
        {value}
      </div>
    </div>
  );
}
