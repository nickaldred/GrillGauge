"use client";

import { Header } from "./components/header";
import { ProbeCard } from "./components/probeCard";
import { Probe } from "./types/types";

export default function Home() {
  const testProbe: Probe = {
    id: 1,
    name: "Probe 1",
    currentTemp: 55,
    targetTemp: 165,
    color: "red",
    connected: true,
  };

  const handleUpdateTargetTemp = async (probeId: number, temp: number) => {};
  const handleUpdateName = async (probeId: number, name: string) => {};
  const onClick = async () => {};

  return (
    <main className="p-6">
      <Header />
      <ProbeCard
        probe={testProbe}
        hubName="hub1"
        onUpdateTargetTemp={handleUpdateTargetTemp}
        onUpdateName={handleUpdateName}
        onClick={onClick}
      />
      <p className="text-xl font-semibold text-gray-700 mt-4">
        Welcome to the Meat Thermometer Dashboard!
      </p>
    </main>
  );
}
