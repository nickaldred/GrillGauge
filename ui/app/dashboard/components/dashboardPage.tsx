"use client";

import { useEffect, useState } from "react";
import { ProbeCard } from "../../components/probeCard";
import { DashboardHub, DashboardType, Probe } from "../../types/types";
import Modal from "../../components/modal";
import ProbeChart from "./probeChart";
import HubChart from "./hubChart";
import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useTheme } from "@/app/providers/ThemeProvider";
import { MdOutlineOutdoorGrill } from "react-icons/md";
import { getData } from "@/app/utils/requestUtils";

const handleUpdateTargetTemp = async (probeId: number, temp: number) => {};
const handleUpdateName = async (probeId: number, name: string) => {};

export function DashboardPage() {
  const { data: session } = useSession();
  const router = useRouter();
  const user = session?.user;
  const [dashboard, setDashboard] = useState<DashboardType | null>(null);
  const [isProbeModalOpen, setIsProbeModalOpen] = useState(false);
  const [isHubModalOpen, setIsHubModalOpen] = useState(false);
  const [selectedProbe, setSelectedProbe] = useState<Probe | null>(null);
  const [selectedHub, setSelectedHub] = useState<DashboardHub | null>(null);

  useEffect(() => {
    if (!user?.email) return;

    const fetchData = () => {
      const email = user.email!;
      const url =
        "http://localhost:8080/api/v1/ui/dashboard?email=" +
        encodeURIComponent(email);

      getData(url)
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
  }, [user?.email]);

  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  const openProbeModal = (probe: Probe) => {
    setSelectedProbe(probe);
    setIsProbeModalOpen(true);
  };

  const closeProbeModal = () => {
    setIsProbeModalOpen(false);
    setSelectedProbe(null);
  };

  const openHubModal = (hub: DashboardHub) => {
    setSelectedHub(hub);
    setIsHubModalOpen(true);
  };

  const closeHubModal = () => {
    setIsHubModalOpen(false);
    setSelectedHub(null);
  };

  // Loading state: centered smoker SVG with animated smoke wisps
  if (!dashboard)
    return (
      <div className="flex items-center justify-center min-h-screen">
        <div
          role="status"
          aria-live="polite"
          className="flex flex-col items-center"
        >
          <div className="flex flex-col items-center gap-1 animate-pulse opacity-90">
            <MdOutlineOutdoorGrill
              className={`w-20 h-20 ${
                isDarkMode ? "text-white" : "text-gray-800"
              }`}
              aria-hidden
            />
            <div
              className={`font-medium ${
                isDarkMode ? "text-gray-200" : "text-gray-700"
              }`}
            >
              Loading dashboard
            </div>
          </div>
        </div>
      </div>
    );

  return (
    <main className="p-6">
      <div className="space-y-8">
        {dashboard.hubs.length === 0 ? (
          <div
            className={`${
              isDarkMode
                ? "bg-gray-800 border-gray-700"
                : "bg-white border-gray-200"
            } max-w-xl w-full mx-auto text-center rounded-xl shadow-lg p-5 mb-6 border`}
            // className="max-w-xl w-full mx-auto text-center px-6 py-8 border rounded-lg shadow-sm"
            aria-live="polite"
          >
            <h2
              className={`text-2xl font-semibold ${
                isDarkMode ? "text-white" : "text-gray-900"
              }`}
            >
              Welcome to GrillGauge
            </h2>
            <p
              className={`mt-2 text-sm ${
                isDarkMode ? "text-gray-300" : "text-gray-600"
              }`}
            >
              Let's get you started — add a hub to begin monitoring your probes
              and view live charts. You can always manage hubs later in
              settings.
            </p>

            <div className="mt-6 flex items-center justify-center space-x-3">
              <button
                type="button"
                onClick={() => router.push("/settings")}
                className={`inline-flex items-center px-4 py-2 rounded-md shadow-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 ${
                  isDarkMode
                    ? "bg-gradient-to-r from-amber-500 to-red-500 text-black hover:from-amber-400 cursor-pointer"
                    : "bg-gradient-to-r from-red-500 to-yellow-400 text-white hover:from-red-600 cursor-pointer"
                }`}
              >
                Add your first hub
              </button>
            </div>
          </div>
        ) : (
          dashboard.hubs.map((hub) => (
            <div key={hub.id} className="mb-10">
              <div className="flex items-center mb-4">
                {/* Hub name (clickable to open hub modal) */}
                <h2 className="mb-4">
                  <button
                    type="button"
                    onClick={() => openHubModal(hub)}
                    onKeyDown={(e) => {
                      if (e.key === "Enter" || e.key === " ") openHubModal(hub);
                    }}
                    className={`text-2xl font-bold ${
                      isDarkMode ? "text-white" : "text-gray-800"
                    } cursor-pointer`}
                  >
                    {hub.name}
                  </button>
                </h2>
                <span
                  className={`ml-3 px-3 py-1 text-sm rounded-full ${
                    hub.connected
                      ? isDarkMode
                        ? "bg-green-500/20 text-green-400"
                        : "bg-green-100 text-green-800"
                      : isDarkMode
                      ? "bg-red-700 text-red-100"
                      : "bg-red-100 text-red-800"
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
          ))
        )}
      </div>

      {/* Probe Modal — shows when a probe has been selected */}
      <Modal open={isProbeModalOpen} onClose={closeProbeModal}>
        <div className="p-6">
          <div className="flex justify-between items-start">
            <h3 className="text-xl font-semibold">
              {selectedProbe ? selectedProbe.name : "Probe"}
            </h3>
            <button
              onClick={closeProbeModal}
              className={`${
                isDarkMode
                  ? "text-gray-300 hover:text-white"
                  : "text-gray-500 hover:text-gray-700"
              }`}
            >
              Close
            </button>
          </div>

          {selectedProbe ? (
            <div>
              <p
                className={`text-sm ${
                  isDarkMode ? "text-gray-300" : "text-gray-600"
                } mb-4`}
              >
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

      {/* Hub Modal — shows when a hub has been selected */}
      <Modal open={isHubModalOpen} onClose={closeHubModal}>
        <div className="p-6">
          <div className="flex justify-between items-start">
            <h3 className="text-xl font-semibold">
              {selectedHub ? selectedHub.name : "Hub"}
            </h3>
            <button
              onClick={closeHubModal}
              className={`${
                isDarkMode
                  ? "text-gray-300 hover:text-white"
                  : "text-gray-500 hover:text-gray-700"
              }`}
            >
              Close
            </button>
          </div>

          {selectedHub ? (
            <div>
              <div
                className={`text-sm ${
                  isDarkMode ? "text-gray-300" : "text-gray-600"
                } mb-4`}
              >
                <p className="font-medium mb-2">Probes temperatures:</p>
                <ul className="list-disc list-inside">
                  {selectedHub.probes.map((p) => (
                    <li key={p.id}>
                      {p.name ? `${p.name} ` : ""}(ID: {p.id}): {p.currentTemp}
                      °F
                    </li>
                  ))}
                </ul>
              </div>
              <div className="border rounded-lg p-4">
                <HubChart hub={selectedHub} />
              </div>
            </div>
          ) : (
            <p>Loading hub...</p>
          )}
        </div>
      </Modal>
    </main>
  );
}

export default DashboardPage;
