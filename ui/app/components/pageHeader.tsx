"use client";

import React, { useEffect, useState } from "react";
import { ThermometerIcon } from "lucide-react";
import { useSession } from "next-auth/react";
import { useTheme } from "@/app/providers/ThemeProvider";
import { BASE_URL } from "@/app/utils/envVars";
import { putRequest } from "@/app/utils/requestUtils";
import {
  defaultTemperatureUnit,
  temperatureUnitSymbol,
} from "@/app/utils/temperature";
import type { TemperatureUnit } from "@/app/types/types";

/**
 * PageHeader component props.
 *
 * @param leftTitle - The title to display on the left side.
 * @param rightTitle - (Optional) The title to display on the right side.
 * @param showTemperatureToggle - (Optional) Show unit toggle in the header.
 */
interface PageHeaderProps {
  leftTitle: string;
  rightTitle?: string;
  showTemperatureToggle?: boolean;
}

export function PageHeader({
  leftTitle,
  rightTitle,
  showTemperatureToggle,
}: PageHeaderProps) {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";
  const { data: session, update } = useSession();
  const [temperatureUnit, setTemperatureUnit] = useState<TemperatureUnit>(
    defaultTemperatureUnit
  );
  const [isSaving, setIsSaving] = useState(false);

  useEffect(() => {
    if (session?.user?.temperatureUnit) {
      setTemperatureUnit(session.user.temperatureUnit);
    }
  }, [session?.user?.temperatureUnit]);

  const handleUnitChange = async (unit: TemperatureUnit) => {
    if (!session?.user?.email) return;
    if (unit === temperatureUnit) return;

    try {
      setIsSaving(true);
      const token = session.apiToken as string | undefined;
      await putRequest(
        `${BASE_URL}/user/temperature-unit?email=${encodeURIComponent(
          session.user.email
        )}&temperatureUnit=${unit}`,
        {},
        token
      );
      setTemperatureUnit(unit);
      await update?.({ temperatureUnit: unit, refreshUser: true });
    } catch (error) {
      console.error("Failed to update temperature unit", error);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <header
      className={`${
        isDarkMode ? "bg-gray-800 border-gray-700" : "bg-white border-gray-200"
      } rounded-xl shadow-lg p-5 mb-6 border`}
    >
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <ThermometerIcon
            size={32}
            className={`mr-3 ${isDarkMode ? "text-red-500" : "text-red-400"}`}
          />
          <h1
            className={`text-2xl font-bold ${
              isDarkMode ? "text-white" : "text-gray-900"
            }`}
          >
            {leftTitle}
          </h1>
        </div>
        {showTemperatureToggle ? (
          <div className="flex items-center gap-3">
            <div
              className={`flex rounded-full border px-1 py-1 ${
                isDarkMode
                  ? "border-gray-700 bg-gray-900/80"
                  : "border-gray-200 bg-gray-50"
              }`}
            >
              <button
                type="button"
                onClick={() => handleUnitChange("FAHRENHEIT")}
                className={`px-3 py-1 text-sm font-medium rounded-full transition-colors cursor-pointer ${
                  temperatureUnit === "FAHRENHEIT"
                    ? isDarkMode
                      ? "bg-amber-500 text-black"
                      : "bg-amber-500 text-white"
                    : isDarkMode
                    ? "text-gray-300 hover:bg-gray-800"
                    : "text-gray-700 hover:bg-gray-100"
                }`}
                aria-pressed={temperatureUnit === "FAHRENHEIT"}
                disabled={isSaving}
              >
                °F
              </button>
              <button
                type="button"
                onClick={() => handleUnitChange("CELSIUS")}
                className={`px-3 py-1 text-sm font-medium rounded-full transition-colors cursor-pointer ${
                  temperatureUnit === "CELSIUS"
                    ? isDarkMode
                      ? "bg-amber-500 text-black"
                      : "bg-amber-500 text-white"
                    : isDarkMode
                    ? "text-gray-300 hover:bg-gray-800"
                    : "text-gray-700 hover:bg-gray-100"
                }`}
                aria-pressed={temperatureUnit === "CELSIUS"}
                disabled={isSaving}
              >
                °C
              </button>
            </div>
          </div>
        ) : (
          <div className="flex items-center gap-4">
            <p
              className={`text-sm ${
                isDarkMode ? "text-gray-300" : "text-gray-500"
              }`}
            >
              {rightTitle}
            </p>
          </div>
        )}
      </div>
    </header>
  );
}
