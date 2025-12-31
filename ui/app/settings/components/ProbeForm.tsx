import React, { useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import { Probe } from "@/app/types/types";
import { useTheme } from "@/app/providers/ThemeProvider";
import { BASE_URL } from "@/app/utils/envVars";
import { getData } from "@/app/utils/requestUtils";

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

  // ** Auth **
  const { data: session } = useSession();

  // ** States **
  const [name, setName] = useState("");
  const [connected, setConnected] = useState(probe?.connected || false);
  const [colour, setColour] = useState<string>(probe?.colour || "");
  const [colourOptions, setColourOptions] = useState<string[]>([]);
  const [errors, setErrors] = useState<{
    name?: string;
  }>({});

  useEffect(() => {
    // Fetch available default probe colours from backend so UI stays in sync.
    const fetchColours = async () => {
      try {
        const token = session?.apiToken as string | undefined;
        const data: string[] = await getData(
          `${BASE_URL}/ui/probe-colours`,
          token
        );
        setColourOptions(data);

        // If no colour currently set default to first value.
        if (!colour && data.length > 0) {
          setColour(data[0]);
        }
      } catch (e) {
        console.error(e);
      }
    };

    if (colour) return;
    fetchColours();
  }, [session, colour]);

  useEffect(() => {
    if (probe) {
      setName(probe.name);
      setConnected(probe.connected);
      setColour(probe.colour || "");
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
        localId: probe.localId ?? 0,
        targetTemp: probe.targetTemp ?? 0,
        currentTemp: probe.currentTemp ?? 0,
        colour,
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
      <div>
        <label
          className={`block text-sm font-medium mb-1 ${
            isDarkMode ? "text-gray-300" : "text-gray-700"
          }`}
        >
          Probe Colour
        </label>
        <div className="mt-2 flex flex-wrap gap-3">
          {colourOptions.map((value, index) => {
            const isSelected = colour === value;
            return (
              <button
                key={value}
                type="button"
                onClick={() => setColour(value)}
                className={`relative h-8 w-8 rounded-full border-2 transition-transform focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 focus:ring-offset-background cursor-pointer ${
                  isSelected
                    ? "scale-110 border-white shadow-[0_0_0_2px_rgba(248,113,113,0.8)]"
                    : "border-gray-300 hover:scale-105"
                }`}
                style={{ backgroundColor: value }}
                aria-label={`Colour ${index + 1}`}
              >
                {isSelected && (
                  <span className="absolute inset-0 flex items-center justify-center text-xs font-bold text-white">
                    ✓
                  </span>
                )}
              </button>
            );
          })}
        </div>
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
