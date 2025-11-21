import React, { useEffect, useState } from "react";
import { Hub } from "@/app/types/types";
import { useTheme } from "@/app/providers/ThemeProvider";

interface HubFormProps {
  hub?: Hub | null;
  onSubmit: (hub: Omit<Hub, "id">) => void;
  onCancel: () => void;
}

export function HubForm({ hub = null, onSubmit, onCancel }: HubFormProps) {
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";
  const [name, setName] = useState("");
  const [connected, setConnected] = useState(true);

  const [errors, setErrors] = useState<{
    name?: string;
  }>({});

  // If editing an existing hub, populate the form
  useEffect(() => {
    if (hub) {
      setName(hub.name);
      setConnected(hub.connected);
    }
  }, [hub]);

  const validate = () => {
    const newErrors: {
      name?: string;
    } = {};
    if (!name.trim()) {
      newErrors.name = "Hub name is required";
    }
    if (name.length > 50) {
      newErrors.name = "Hub name must be less than 50 characters";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validate()) {
      onSubmit({
        ...(hub || {
          probes: [],
        }),
        name,
        connected,
      });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div>
        <label
          htmlFor="hub-name"
          className={`block text-sm font-medium mb-1 ${
            isDarkMode ? "text-gray-300" : "text-gray-700"
          }`}
        >
          Hub Name
        </label>
        <input
          id="hub-name"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 ${
            errors.name ? "border-red-500" : "border-gray-300"
          }`}
          placeholder="e.g., Kitchen Hub"
        />
        {errors.name && (
          <p className="mt-1 text-sm text-red-500">{errors.name}</p>
        )}
      </div>
      <div className="flex justify-end space-x-3">
        <button
          type="button"
          onClick={onCancel}
          className={`px-4 py-2 border rounded-lg cursor-pointer ${
            isDarkMode
              ? "bg-gray-600 border-gray-600 text-gray-100 hover:bg-gray-700"
              : "bg-white border-gray-200 hover:bg-gray-50"
          } `}
        >
          Cancel
        </button>
        <button
          type="submit"
          className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 cursor-pointer"
        >
          {hub ? "Update Hub" : "Add Hub"}
        </button>
      </div>
    </form>
  );
}
