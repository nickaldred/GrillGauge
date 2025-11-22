import { useEffect } from "react";
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
}

/**
 * Modal component for displaying content in a dialog.
 *
 * @param {boolean} open - Whether the modal is open.
 * @param {function} onClose - Function to call when closing the modal.
 * @param {string} title - Title of the modal.
 * @param {React.ReactNode} children - Content of the modal.
 * @returns The Modal component.
 */
export default function Modal({ open, onClose, title, children }: Modal) {
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
        className={`"p-5 overflow-auto max-h-[calc(90vh-80px)] w-full sm:w-[650px] md:w-[550px]" ${
          isDarkMode ? "bg-gray-800 text-gray-100" : "bg-white text-gray-900"
        } rounded-2xl shadow-lg`}
      >
        <div className="flex items-center justify-between p-5 border-b">
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
}
