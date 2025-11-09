"use client";
import React, { createContext, useContext, useEffect, useState, ReactNode, useMemo } from "react";

type Theme = "light" | "dark";

type ThemeContextShape = {
  theme: Theme;
  toggle: () => void;
};

const ThemeContext = createContext<ThemeContextShape>({
  theme: "light",
  toggle: () => {},
});

export function useTheme() {
  return useContext(ThemeContext);
}

export default function ThemeProvider({ children }: { children: ReactNode }) {
  const [theme, setTheme] = useState<Theme>("light");

  useEffect(() => {
    try {
      const stored = localStorage.getItem("theme") as Theme | null;
      if (stored === "light" || stored === "dark") {
        setTheme(stored);
        document.documentElement.dataset.theme = stored;
        document.documentElement.classList.toggle("dark", stored === "dark");
        return;
      }

      const prefersDark = globalThis.matchMedia?.("(prefers-color-scheme: dark)")?.matches;
      if (prefersDark) {
        setTheme("dark");
        document.documentElement.dataset.theme = "dark";
        document.documentElement.classList.add("dark");
      } else {
        setTheme("light");
        document.documentElement.dataset.theme = "light";
        document.documentElement.classList.remove("dark");
      }
    } catch (err) {
      // ignore storage errors; keep a tiny debug log for visibility
      // eslint-disable-next-line no-console
      console.debug("init theme failed", err);
    }
  }, []);

  const toggle = () => {
    const next: Theme = theme === "dark" ? "light" : "dark";
    setTheme(next);
    try {
      localStorage.setItem("theme", next);
    } catch (err) {
      // ignore storage errors
      // eslint-disable-next-line no-console
      console.debug("persist theme failed", err);
    }
    document.documentElement.dataset.theme = next;
    document.documentElement.classList.toggle("dark", next === "dark");
  };

  const value = useMemo(() => ({ theme, toggle }), [theme]);

  return <ThemeContext.Provider value={value}>{children}</ThemeContext.Provider>;
}
