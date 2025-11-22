"use client";

import React, { useState } from "react";
import { CheckIcon, EditIcon, ThermometerIcon, XIcon } from "lucide-react";
import { Probe } from "../types/types";
import { useTheme } from "../providers/ThemeProvider";

interface ProbeProps {
  readonly probe: Probe;
  readonly hubName: string;
  onUpdateTargetTemp: (probeId: number, temp: number) => void;
  onUpdateName: (probeId: number, name: string) => void;
  onClick?: (probe: Probe) => void;
}

export function ProbeCard({
  probe,
  hubName,
  onUpdateTargetTemp,
  onUpdateName,
  onClick,
}: Readonly<ProbeProps>) {
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";
  const [isEditingName, setIsEditingName] = useState(false);
  const [tempName, setTempName] = useState(probe.name);
  const [isEditingTarget, setIsEditingTarget] = useState(false);
  const [tempTarget, setTempTarget] = useState(probe.targetTemp);

  const progress = probe.connected
    ? Math.max(Math.round((probe.currentTemp / probe.targetTemp) * 100), 0)
    : 0;

  const getTemperatureStatus = () => {
    if (!probe.connected)
      return {
        text: "Disconnected",
        class: isDarkMode
          ? "bg-red-700 text-red-100"
          : "bg-red-100 text-red-800",
      };
    if (progress >= 115)
      return {
        text: "Over target temp",
        class: isDarkMode ? "text-red-400 font-bold" : "text-red-600 font-bold",
      };
    if (progress >= 100)
      return {
        text: "At target",
        class: isDarkMode
          ? "text-green-400 font-bold"
          : "text-green-600 font-bold",
      };
    if (progress >= 90)
      return {
        text: "Almost at target",
        class: isDarkMode
          ? "text-yellow-400 font-bold"
          : "text-yellow-600 font-bold",
      };
    if (progress >= 50)
      return {
        text: "Half way to target",
        class: isDarkMode ? "text-orange-400" : "text-orange-500",
      };
    return {
      text: "Heating",
      class: isDarkMode ? "text-blue-400" : "text-blue-500",
    };
  };

  const status = getTemperatureStatus();

  const handleSaveName = () => {
    onUpdateName(probe.id, tempName);
    setIsEditingName(false);
  };

  const handleSaveTarget = () => {
    onUpdateTargetTemp(probe.id, Number.parseInt(tempTarget.toString()));
    setIsEditingTarget(false);
  };

  const handleClick = (e: React.MouseEvent) => {
    // Don't trigger click when editing fields
    if (
      isEditingName ||
      isEditingTarget ||
      (e.target instanceof HTMLElement &&
        (e.target.tagName.toLowerCase() === "input" ||
          e.target.tagName.toLowerCase() === "button"))
    ) {
      return;
    }
    onClick?.(probe);
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (!probe.connected) return;

    // Don't trigger when editing or focused on an input/button inside the card
    if (
      isEditingName ||
      isEditingTarget ||
      (e.target instanceof HTMLElement &&
        (e.target.tagName.toLowerCase() === "input" ||
          e.target.tagName.toLowerCase() === "button"))
    ) {
      return;
    }

    if (e.key === "Enter" || e.key === " ") {
      e.preventDefault();
      onClick?.(probe);
    }
  };

  return (
    <div
      role={probe.connected ? "button" : undefined}
      tabIndex={probe.connected ? 0 : -1}
      onKeyDown={handleKeyDown}
      className={`${
        isDarkMode ? "bg-gray-800 border-gray-700" : "bg-white border-gray-200 "
      } rounded-xl shadow-lg p-6 border transition-all duration-500 ${
        probe.connected
          ? `cursor-pointer ${
              isDarkMode ? "hover:border-gray-200" : "hover:shadow-xl"
            }`
          : "opacity-75"
      }`}
      onClick={probe.connected ? handleClick : undefined}
    >
      <div className="flex justify-between items-start mb-4">
        <div className="flex items-center flex-1">
          {isEditingName ? (
            <div className="flex items-center space-x-2">
              <input
                type="text"
                value={tempName}
                onChange={(e) => setTempName(e.target.value)}
                className={`px-2 py-1 border rounded ${
                  isDarkMode
                    ? "bg-gray-700 border-gray-600 text-white"
                    : "border-gray-300"
                }`}
                onClick={(e) => e.stopPropagation()}
                onKeyDown={(e) => {
                  if (e.key === "Enter") handleSaveName();
                }}
                autoFocus
              />
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleSaveName();
                }}
                className={`p-1 rounded ${
                  isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-100"
                }`}
              >
                <CheckIcon
                  size={16}
                  className={isDarkMode ? "text-green-400" : "text-green-600"}
                />
              </button>
            </div>
          ) : (
            <>
              <h3
                className={`text-xl font-semibold ${
                  isDarkMode ? "text-white" : "text-gray-900"
                }`}
              >
                {probe.name}
              </h3>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setIsEditingName(true);
                }}
                className={`ml-2 p-1 rounded ${
                  isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-100"
                }`}
              >
                <EditIcon
                  size={16}
                  className={`${
                    isDarkMode ? "text-gray-400" : "text-gray-500"
                  } cursor-pointer`}
                />
              </button>
            </>
          )}
        </div>
        <span
          className={`px-3 py-1 text-sm rounded-full ${
            probe.connected
              ? isDarkMode
                ? "bg-green-500/20 text-green-400"
                : "bg-green-100 text-green-800"
              : isDarkMode
              ? "bg-red-700 text-red-100"
              : "bg-red-100 text-red-800"
          }`}
        >
          {probe.connected ? "Connected" : "Disconnected"}
        </span>
      </div>
      <div className="mb-4">
        <div className="flex items-baseline">
          <span
            className={`text-5xl font-bold ${
              isDarkMode ? "text-white" : "text-gray-900"
            }`}
          >
            {probe.connected ? probe.currentTemp : "--"}
          </span>
          <span
            className={`text-2xl ml-1 ${
              isDarkMode ? "text-gray-400" : "text-gray-500"
            }`}
          >
            °F
          </span>
        </div>
        {probe.connected && (
          <div
            className={`mt-2 text-sm ${
              isDarkMode ? "text-gray-400" : "text-gray-500"
            }`}
          >
            {progress >= 90 ? (
              <span
                className={isDarkMode ? "text-green-400" : "text-green-600"}
              >
                ✓ Near target temperature
              </span>
            ) : (
              <span>{probe.targetTemp - probe.currentTemp}°F to target</span>
            )}
          </div>
        )}
      </div>
      <div className="mb-4">
        <div
          className={`w-full ${
            isDarkMode ? "bg-gray-700" : "bg-gray-200"
          } rounded-full h-3 overflow-hidden`}
        >
          <div
            className="h-full transition-all duration-500 rounded-full"
            style={{
              width: `${progress}%`,
              backgroundColor: probe.colour,
            }}
          />
        </div>
      </div>
      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <ThermometerIcon
            size={18}
            className={isDarkMode ? "text-gray-400" : "text-gray-500"}
          />
          <span
            className={`ml-2 text-sm ${
              isDarkMode ? "text-gray-400" : "text-gray-500"
            }`}
          >
            Target:
          </span>
          {isEditingTarget ? (
            <div className="flex items-center ml-2 space-x-2">
              <input
                type="number"
                value={tempTarget}
                onChange={(e) => setTempTarget(Number.parseInt(e.target.value))}
                className={`w-20 px-2 py-1 border rounded ${
                  isDarkMode
                    ? "bg-gray-700 border-gray-600 text-white"
                    : "border-gray-300"
                }`}
                onClick={(e) => e.stopPropagation()}
                onKeyDown={(e) => {
                  if (e.key === "Enter") handleSaveTarget();
                }}
                autoFocus
              />
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  handleSaveTarget();
                }}
                className={`p-1 rounded ${
                  isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-100"
                }`}
              >
                <CheckIcon
                  size={16}
                  className={isDarkMode ? "text-green-400" : "text-green-600"}
                />
              </button>
            </div>
          ) : (
            <>
              <span
                className={`ml-1 font-medium ${
                  isDarkMode ? "text-white" : "text-gray-900"
                }`}
              >
                {probe.targetTemp}°F
              </span>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  setIsEditingTarget(true);
                }}
                className={`ml-2 p-1 rounded ${
                  isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-100"
                }`}
              >
                <EditIcon
                  size={14}
                  className={`${
                    isDarkMode ? "text-gray-400" : "text-gray-500"
                  } cursor-pointer`}
                />
              </button>
            </>
          )}
        </div>
        <div
          className={`text-sm ${
            isDarkMode ? "text-gray-400" : "text-gray-500"
          }`}
        >
          {hubName}
        </div>
      </div>
    </div>
  );
}
