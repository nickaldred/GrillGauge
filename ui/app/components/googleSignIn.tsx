"use client";

import { signOut, useSession } from "next-auth/react";
import { useTheme } from "../providers/ThemeProvider";
import { LogOutIcon } from "lucide-react";
import { useRouter } from "next/navigation";
import Image from "next/image";

export const handleSignOut = async (e: React.MouseEvent<HTMLButtonElement>) => {
  e.preventDefault();
  await signOut();
};

export default function GoogleSignInButton() {
  // ** Session & Router **
  const { data: session } = useSession();
  const router = useRouter();
  const user = session?.user;

  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** Handlers **
  const handleSignIn = () => {
    router.push("/login");
  };

  const handleProfileClick = () => {
    router.push("/profile");
  };

  const profileImage =
    // @ts-expect-error Custom session field from auth callback
    user?.imageData ?? user?.image ?? undefined;

  return user ? (
    <>
      <div
        className="flex items-center cursor-pointer "
        onClick={handleProfileClick}
      >
        {profileImage ? (
          <Image
            src={profileImage}
            alt="Profile"
            width={32}
            height={32}
            className="w-8 h-8 rounded-full mr-2 object-cover"
          />
        ) : (
          <div className="w-8 h-8 rounded-full mr-2 bg-gradient-to-br from-orange-500 to-red-600 text-white flex items-center justify-center text-sm font-semibold">
            {(user.name || "?").charAt(0).toUpperCase()}
          </div>
        )}
        <span
          className={`text-sm font-medium hidden md:inline ${
            isDarkMode ? "text-gray-300" : "text-gray-900"
          }`}
        >
          {user.name}
        </span>
      </div>
      <button
        onClick={handleSignOut}
        className={`${
          isDarkMode
            ? "text-gray-300 hover:text-orange-400 hover:bg-gray-700 cursor-pointer"
            : "text-gray-600 hover:text-red-600 hover:bg-gray-100 cursor-pointer"
        } p-2 rounded-lg flex items-center transition-colors`}
        aria-label="Logout"
      >
        <LogOutIcon size={18} />
      </button>
    </>
  ) : (
    <button
      onClick={handleSignIn}
      className="px-4 py-2 rounded-lg font-medium bg-gradient-to-r from-orange-500 to-red-600 text-white cursor-pointer"
    >
      Sign In
    </button>
  );
}
