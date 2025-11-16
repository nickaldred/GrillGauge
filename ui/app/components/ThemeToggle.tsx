"use client";
import React from "react";
import { Sun, Moon } from "lucide-react";
import { useTheme } from "../providers/ThemeProvider";

export default function ThemeToggle() {
  const { theme, toggle } = useTheme();

  return (
    <button
      aria-label="Toggle color theme"
      onClick={toggle}
      className="flex items-center gap-2 px-3 py-2 rounded-full bg-theme-toggle-bg text-theme-toggle-fg shadow-md ring-1 ring-theme-toggle-ring"
    >
      {theme === "dark" ? <Sun size={16} /> : <Moon size={16} />}
      <span className="text-sm font-medium">{theme === "dark" ? "Light" : "Dark"}</span>
    </button>
  );
}
