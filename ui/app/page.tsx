"use client";

import { useEffect, useState } from "react";
import { Header } from "./components/header";
import { ProbeCard } from "./components/probeCard";
import { Dashboard, Probe } from "./types/types";

export default function Home() {
  const [user, setUser] = useState(null);
  // const [probes, setProbes] = useState<Probe[]>([]);
  // const [hubs, setHubs] = useState<string[]>([]);
  // const [probes, setProbes] = useState<Probe[]>([]);
  const [dashboard, setDashboard] = useState<Dashboard | null>(null);

  async function getData(url: string) {
    const response = await fetch(url, {
      method: "GET",
      headers: {
        "Content-Type": "application/json",
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch data");
    }

    const data = await response.json();
    return data;
  }

  useEffect(() => {
    const fetchData = () => {
      getData("http://localhost:8080/api/v1/ui/dashboard?userId=1")
        .then((data) => {
          setDashboard(data);
        })
        .catch((error) => {
          console.error("Error fetching user:", error);
        });
    };

    fetchData();

    const intervalId = setInterval(fetchData, 30000);

    return () => clearInterval(intervalId);
  }, []);

  const handleUpdateTargetTemp = async (probeId: number, temp: number) => {};
  const handleUpdateName = async (probeId: number, name: string) => {};
  const onClick = async () => {};

  if (!dashboard) {
    return <p>Loading dashboard...</p>;
  }

  return (
    <main className="p-6">
      <Header />
      <div className="space-y-8">
        {dashboard.hubs.map((hub) => (
          <div key={hub.id}>
            {/* Hub name */}
            <h2 className="text-2xl font-semibold mb-4 text-gray-800">
              {hub.name}
            </h2>

            {/* Probe cards in a 2-per-row layout */}
            <div className="flex flex-wrap -mx-2">
              {hub.probes.map((probe) => (
                <div key={probe.id} className="w-full sm:w-1/2 px-2 mb-4">
                  <ProbeCard
                    probe={probe}
                    hubName={hub.name}
                    onUpdateTargetTemp={handleUpdateTargetTemp}
                    onUpdateName={handleUpdateName}
                    onClick={onClick}
                  />
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>
    </main>
  );
}
