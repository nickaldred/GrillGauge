"use client";

import React, { useState } from "react";
import { CheckIcon, EditIcon, ThermometerIcon, XIcon } from "lucide-react";
import { Probe } from "../types/types";

interface ProbeProps {
  readonly probe: Probe;
  readonly hubName: string;
  onUpdateTargetTemp: (probeId: number, temp: number) => void;
  onUpdateName: (probeId: number, name: string) => void;
  onClick?: () => void;
}

export function ProbeCard({
  probe,
  hubName,
  onUpdateTargetTemp,
  onUpdateName,
  onClick,
}: Readonly<ProbeProps>) {
  const [isEditingName, setIsEditingName] = useState(false);
  const [tempName, setTempName] = useState(probe.name);
  const [isEditingTarget, setIsEditingTarget] = useState(false);
  const [tempTarget, setTempTarget] = useState(probe.targetTemp);
  const [showGraph, setShowGraph] = useState(false);

  const progress = probe.connected
    ? Math.max(Math.round((probe.currentTemp / probe.targetTemp) * 100), 0)
    : 0;

  const getTemperatureStatus = () => {
    if (!probe.connected)
      return {
        text: "Disconnected",
        class: "text-gray-500",
      };
    if (progress >= 115)
      return {
        text: "Over target temp",
        class: "text-red-600 font-bold",
      };
    if (progress >= 100)
      return {
        text: "At target",
        class: "text-green-600 font-bold",
      };
    if (progress >= 90)
      return {
        text: "Almost at target",
        class: "text-yellow-600 font-bold",
      };
    if (progress >= 50)
      return {
        text: "Half way to target",
        class: "text-orange-500",
      };
    return {
      text: "Heating",
      class: "text-blue-500",
    };
  };

  const status = getTemperatureStatus();

  const handleSaveName = () => {
    onUpdateName(probe.id, tempName);
    setIsEditingName(false);
  };

  const handleSaveTarget = () => {
    onUpdateTargetTemp(probe.id, parseInt(tempTarget.toString()));
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
    onClick && onClick();
  };

  return (
    <div
      className={`bg-white rounded-xl shadow-lg overflow-hidden border border-gray-200 ${
        probe.connected
          ? "cursor-pointer hover:shadow-xl transition-shadow"
          : ""
      }`}
      onClick={handleClick}
    >
      <div
        className="p-5 flex justify-between items-center"
        style={{
          borderTop: `4px solid ${probe.colour}`,
        }}
      >
        <div className="flex items-center">
          {isEditingName ? (
            <div className="flex items-center">
              <input
                type="text"
                value={tempName}
                onChange={(e) => setTempName(e.target.value)}
                className="border rounded px-2 py-1 mr-2 text-lg font-medium"
                autoFocus
              />
              <button onClick={handleSaveName} className="text-green-500 mr-1">
                <CheckIcon size={18} />
              </button>
              <button
                onClick={() => setIsEditingName(false)}
                className="text-red-500"
              >
                <XIcon size={18} />
              </button>
            </div>
          ) : (
            <div className="flex items-center">
              <h3 className="text-xl font-medium mr-2">{probe.name}</h3>
              <button
                onClick={() => setIsEditingName(true)}
                className="text-gray-400 hover:text-gray-600"
              >
                <EditIcon size={16} />
              </button>
            </div>
          )}
        </div>
        <span
          className={`px-3 py-1 rounded-full text-sm ${status.class} bg-opacity-10`}
        >
          {status.text}
        </span>
      </div>
      <div className="p-5 pt-0">
        <div className="text-sm text-gray-500 mb-3">Hub: {hubName}</div>
        <div className="flex justify-between items-end mb-2">
          <div>
            <p className="text-sm text-gray-500">Current</p>
            <div className="flex items-center">
              <ThermometerIcon size={24} className="text-red-500 mr-2" />
              <span className="text-4xl font-bold">
                {probe.connected ? `${probe.currentTemp}°F` : "--"}
              </span>
            </div>
          </div>
          <div className="text-right">
            <p className="text-sm text-gray-500">Target</p>
            {isEditingTarget ? (
              <div className="flex items-center">
                <input
                  type="number"
                  value={tempTarget}
                  onChange={(e) => setTempTarget(parseInt(e.target.value))}
                  className="border rounded w-20 px-2 py-1 mr-2 text-xl font-medium"
                  autoFocus
                />
                <button
                  onClick={handleSaveTarget}
                  className="text-green-500 mr-1"
                >
                  <CheckIcon size={18} />
                </button>
                <button
                  onClick={() => setIsEditingTarget(false)}
                  className="text-red-500"
                >
                  <XIcon size={18} />
                </button>
              </div>
            ) : (
              <div className="flex items-center justify-end">
                <span className="text-2xl font-medium mr-2">
                  {probe.targetTemp}°F
                </span>
                <button
                  onClick={() => setIsEditingTarget(true)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <EditIcon size={16} />
                </button>
              </div>
            )}
          </div>
        </div>
        <div className="w-full bg-gray-200 rounded-full h-3 mt-4">
          <div
            className="h-3 rounded-full transition-all duration-500"
            style={{
              width: `${progress}%`,
              backgroundColor: probe.colour,
            }}
          ></div>
        </div>
        {probe.connected && (
          <div className="mt-3 text-center text-xs text-gray-500">
            Click for temperature history
          </div>
        )}
      </div>
    </div>
  );
}
