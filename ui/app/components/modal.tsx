import { useEffect } from "react";
import { useTheme } from "@/app/providers/ThemeProvider";

interface GraphModalProps {
  open: boolean;
  onClose: () => void;
  children?: React.ReactNode;
}

export default function Modal({ open, onClose, children }: GraphModalProps) {
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => {
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
    >
      <div
        className={`fixed inset-0 ${
          isDarkMode ? "bg-black/60" : "bg-black/40"
        }`}
        onClick={onClose}
        aria-hidden="true"
      />
      <div
        className={`relative z-10 w-full max-w-2xl mx-4 ${
          isDarkMode ? "bg-gray-800 text-gray-100" : "bg-white text-gray-900"
        } rounded-2xl shadow-lg`}
      >
        {children}
      </div>
    </div>
  );
}
