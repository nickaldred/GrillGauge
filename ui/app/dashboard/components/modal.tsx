import { useEffect } from "react";

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

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      role="dialog"
      aria-modal="true"
    >
      <div
        className="fixed inset-0 bg-black/40"
        onClick={onClose}
        aria-hidden="true"
      />
      <div className="relative z-10 w-full max-w-2xl mx-4 bg-white rounded-2xl shadow-lg">
        {children}
      </div>
    </div>
  );
}
