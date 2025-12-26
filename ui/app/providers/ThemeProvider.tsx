"use client";
import React, {
  createContext,
  useContext,
  useEffect,
  useState,
  ReactNode,
  useMemo,
} from "react";
import { SessionProvider } from "next-auth/react";

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

const getInitialTheme = (): Theme => {
  if (typeof document === "undefined") return "light";

  const doc = document.documentElement;
  const datasetTheme = doc.dataset.theme as Theme | undefined;
  if (datasetTheme === "light" || datasetTheme === "dark") return datasetTheme;

  try {
    const stored = localStorage.getItem("theme") as Theme | null;
    if (stored === "light" || stored === "dark") return stored;
  } catch (err) {
    // ignore storage errors;
    // eslint-disable-next-line no-console
    console.debug("read theme failed", err);
  }

  const prefersDark = globalThis.matchMedia?.(
    "(prefers-color-scheme: dark)"
  )?.matches;
  return prefersDark ? "dark" : "light";
};

export default function ThemeProvider({
  children,
  initialTheme = "light",
}: Readonly<{ children: ReactNode; initialTheme?: Theme }>) {
  const [theme, setTheme] = useState<Theme>(() => initialTheme);

  useEffect(() => {
    const resolved = getInitialTheme();
    setTheme(resolved);
    document.documentElement.dataset.theme = resolved;
    document.documentElement.classList.toggle("dark", resolved === "dark");
  }, []);

  const toggle = () => {
    const next: Theme = theme === "dark" ? "light" : "dark";
    setTheme(next);
    try {
      localStorage.setItem("theme", next);
      document.cookie = `theme=${next}; path=/; max-age=${
        60 * 60 * 24 * 365
      }; samesite=lax`;
    } catch (err) {
      // ignore storage errors
      // eslint-disable-next-line no-console
      console.debug("persist theme failed", err);
    }
    document.documentElement.dataset.theme = next;
    document.documentElement.classList.toggle("dark", next === "dark");
  };

  const value = useMemo(() => ({ theme, toggle }), [theme]);

  return (
    <ThemeContext.Provider value={value}>
      <SessionProvider>{children}</SessionProvider>
    </ThemeContext.Provider>
  );
}
