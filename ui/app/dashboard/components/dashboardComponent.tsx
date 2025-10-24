"use client";

import { useEffect, useState } from "react";
import { ProbeCard } from "../../components/probeCard";
import { DashboardType, Probe } from "../../types/types";
import Modal from "./modal";
import ProbeChart from "./probeChart";

const handleUpdateTargetTemp = async (probeId: number, temp: number) => {};
const handleUpdateName = async (probeId: number, name: string) => {};

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

export function DashboardComponent() {
  const [dashboard, setDashboard] = useState<DashboardType | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedProbe, setSelectedProbe] = useState<Probe | null>(null);

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

  const openProbeModal = (probe: Probe) => {
    setSelectedProbe(probe);
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedProbe(null);
  };

  if (!dashboard) return <p>Loading dashboard...</p>;

  return (
    <main className="p-6">
      <div className="space-y-8">
        {dashboard.hubs.map((hub) => (
          <div key={hub.id} className="mb-10">
            <div className="flex items-center mb-4">
              {/* Hub name */}
              <h2 className="text-2xl font-bold mb-4 text-gray-800">
                {hub.name}
              </h2>
              <span
                className={`ml-3 px-3 py-1 text-sm rounded-full ${
                  hub.connected
                    ? "bg-green-100 text-green-800"
                    : "bg-gray-100 text-gray-800"
                }`}
              >
                {hub.connected ? "Connected" : "Disconnected"}
              </span>
            </div>

            {/* Probe cards in a 2-per-row layout */}
            <div className="flex flex-wrap -mx-2">
              {hub.probes.map((probe) => (
                <div key={probe.id} className="w-full sm:w-1/2 px-2 mb-4">
                  <ProbeCard
                    probe={probe}
                    hubName={hub.name}
                    onUpdateTargetTemp={handleUpdateTargetTemp}
                    onUpdateName={handleUpdateName}
                    onClick={openProbeModal}
                  />
                </div>
              ))}
            </div>
          </div>
        ))}
      </div>

      {/* Modal — shows when a probe has been selected */}
      <Modal open={isModalOpen} onClose={closeModal}>
        <div className="p-6">
          <div className="flex justify-between items-start">
            <h3 className="text-xl font-semibold">
              {selectedProbe ? selectedProbe.name : "Probe"}
            </h3>
            <button
              onClick={closeModal}
              className="ext-gray-500 hover:text-gray-700"
            >
              Close
            </button>
          </div>

          {selectedProbe ? (
            <div>
              <p className="text-sm text-gray-600 mb-4">
                Probe ID: {selectedProbe.id} | Current Temp:{" "}
                {selectedProbe.currentTemp}°F
              </p>

              <div className="border rounded-lg p-4">
                <ProbeChart probeId={selectedProbe.id} />
              </div>
            </div>
          ) : (
            <p>Loading probe...</p>
          )}
        </div>
      </Modal>
    </main>
  );
}
