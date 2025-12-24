"use client";

import { useEffect } from "react";
import { createPortal } from "react-dom";
import { useTheme } from "@/app/providers/ThemeProvider";
import { XIcon } from "lucide-react";

/**
 * Modal Component
 *
 * @param open - Whether the modal is open.
 * @param onClose - Function to call when closing the modal.
 * @param title - Title of the modal.
 * @param children - Content of the modal.
 */
interface Modal {
  open: boolean;
  onClose: () => void;
  title: string;
  children?: React.ReactNode;
  /** Optional flag to render a wider modal (useful for charts, tables, etc.) */
  wide?: boolean;
}

/**
 * Modal component for displaying content in a dialog.
 *
 * @param {boolean} open - Whether the modal is open.
 * @param {function} onClose - Function to call when closing the modal.
 * @param {string} title - Title of the modal.
 * @param {React.ReactNode} children - Content of the modal.
 * @param {boolean} wide - If true, use a wider layout.
 * @returns The Modal component.
 */
export default function Modal({ open, onClose, title, children, wide }: Modal) {
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

  const widthClass = wide ? "w-full max-w-5xl" : "w-full max-w-xl";

  const modalContent = (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
    >
      {/* Blur the background of the modal; clicking it closes the modal */}
      <div
        className="absolute inset-0 bg-black/40 backdrop-blur-sm"
        onClick={onClose}
      />

      <div
        className={`relative z-10 p-5 overflow-auto max-h-[calc(90vh-80px)] ${widthClass} ${
          isDarkMode ? "bg-gray-800 text-gray-100" : "bg-white text-gray-900"
        } rounded-2xl shadow-lg`}
      >
        <div
          className={`flex items-center justify-between p-5 border-b ${
            isDarkMode ? "border-gray-700" : "border-gray-200"
          }`}
        >
          <h3 className="text-xl font-semibold">{title}</h3>
          <button
            onClick={onClose}
            className={`${
              isDarkMode
                ? "text-gray-300 hover:text-white"
                : "text-gray-500 hover:text-gray-700"
            } cursor-pointer`}
          >
            <XIcon size={20} />
          </button>
        </div>
        <div className="p-5 overflow-auto max-h-[calc(90vh-80px)]">
          {children}
        </div>
      </div>
    </div>
  );

  return createPortal(modalContent, document.body);
}
