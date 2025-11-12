"use client";

import { signOut, useSession } from "next-auth/react";
import { useTheme } from "../providers/ThemeProvider";
import { LogOutIcon } from "lucide-react";
import { useRouter } from "next/navigation";

export const handleSignOut = async (e: React.MouseEvent<HTMLButtonElement>) => {
  e.preventDefault();
  await signOut();
};

export default function GoogleSignInButton() {
  const { data: session } = useSession();
  const router = useRouter();
  const user = session?.user;
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  const handleSignIn = () => {
    router.push("/login");
  };

  return user ? (
    <>
      <div className="flex items-center">
        <img
          src={user.image || "https://randomuser.me/api/portraits/lego/1.jpg"}
          alt="Profile"
          className="w-8 h-8 rounded-full mr-2"
        />
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
