import React, { useEffect, useState } from "react";
import { Hub, Probe } from "@/app/types/types";
import { useTheme } from "@/app/providers/ThemeProvider";

/**
 * Props for the HubForm component.
 */
interface ProbeFormProps {
  probe?: Probe | null;
  onSubmit: (probe: Omit<Probe, "id">) => void;
  onCancel: () => void;
}

/**
 * The ProbeForm component allows users to add or edit a Probe.
 *
 * @param probe The probe to edit, or null for adding a new probe.
 * @returns The ProbeForm component.
 */
export function ProbeForm({
  probe = null,
  onSubmit,
  onCancel,
}: ProbeFormProps) {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** States **
  const [name, setName] = useState("");
  const [connected, setConnected] = useState(probe?.connected || false);
  const [errors, setErrors] = useState<{
    name?: string;
  }>({});

  useEffect(() => {
    if (probe) {
      setName(probe.name);
      setConnected(probe.connected);
    }
  }, [probe]);

  const validate = () => {
    const newErrors: {
      name?: string;
    } = {};
    if (!name.trim()) {
      newErrors.name = "Probe name is required";
    }
    if (name.length > 50) {
      newErrors.name = "Probe name must be less than 50 characters";
    }
    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!probe) return;
    if (validate()) {
      onSubmit({
        ...probe,
        name,
        connected,
        localId: probe.localId ?? 0, // fallback if missing
        targetTemp: probe.targetTemp ?? 0, // fallback
        currentTemp: probe.currentTemp ?? 0, // fallback
        colour: probe.colour ?? "white", // fallback
      });
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div>
        <label
          htmlFor="probe-name"
          className={`block text-sm font-medium mb-1 ${
            isDarkMode ? "text-gray-300" : "text-gray-700"
          }`}
        >
          Probe Name
        </label>
        <input
          id="probe-name"
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-red-500 focus:border-red-500 ${
            errors.name ? "border-red-500" : "border-gray-300"
          }`}
          placeholder="e.g., Main Grill Probe"
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
          {probe ? "Update Probe" : "Add Probe"}
        </button>
      </div>
    </form>
  );
}
