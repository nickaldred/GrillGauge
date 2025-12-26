"use client";

import { useEffect, useState } from "react";
import { ProbeCard } from "../../components/probeCard";
import { Hub, Probe } from "../../types/types";
import Modal from "../../components/modal";
import ProbeChart from "./probeChart";
import HubChart from "./hubChart";
import { useSession } from "next-auth/react";
import { useRouter } from "next/navigation";
import { useTheme } from "@/app/providers/ThemeProvider";
import { MdOutlineOutdoorGrill } from "react-icons/md";
import { getData, putRequest } from "@/app/utils/requestUtils";
import { BASE_URL } from "@/app/utils/envVars";

/**
 * The DashboardPage component displays the user's hubs and probes,
 * allowing them to view and manage their grill monitoring setup.
 *
 * @returns The DashboardPage component.
 */
export function DashboardPage() {
  // ** Session & Router **
  const { data: session } = useSession();
  const router = useRouter();
  const user = session?.user;

  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** States **
  const [hubs, setHubs] = useState<Hub[] | null>(null);
  const [isProbeModalOpen, setIsProbeModalOpen] = useState(false);
  const [isHubModalOpen, setIsHubModalOpen] = useState(false);
  const [selectedProbe, setSelectedProbe] = useState<Probe | null>(null);
  const [selectedHub, setSelectedHub] = useState<Hub | null>(null);

  // ** Fetch Hubs & Probes **
  useEffect(() => {
    if (!user?.email) return;

    const fetchData = () => {
      const email = user.email!;
      const url = `${BASE_URL}/ui/hubs?email=` + encodeURIComponent(email);

      // @ts-expect-error apiToken is added in auth.ts session callback
      const token = session?.apiToken as string | undefined;
      getData(url, token)
        .then((data) => {
          setHubs(data);
        })
        .catch((error) => {
          console.error("Error fetching user:", error);
        });
    };

    fetchData();
    const intervalId = setInterval(fetchData, 30000);
    return () => clearInterval(intervalId);
  }, [user?.email, session]);

  // ** Handlers **

  // Update probe target temperature
  const handleUpdateTargetTemp = async (
    probeId: number,
    updatedTargetTemp: number
  ) => {
    try {
      // @ts-expect-error apiToken is added in auth.ts session callback
      const token = session?.apiToken as string | undefined;
      await putRequest(
        `${BASE_URL}/probe/targetTemp/${probeId}?targetTemp=${updatedTargetTemp}`,
        {},
        token
      );

      setHubs((prev) =>
        prev
          ? prev.map((h) => ({
              ...h,
              probes: h.probes.map((p) =>
                p.id === probeId ? { ...p, targetTemp: updatedTargetTemp } : p
              ),
            }))
          : prev
      );
    } catch (error) {
      console.error(error);
    }
  };

  // Update probe name
  const handleUpdateName = async (probeId: number, updatedName: string) => {
    try {
      // @ts-expect-error apiToken is added in auth.ts session callback
      const token = session?.apiToken as string | undefined;
      await putRequest(
        `${BASE_URL}/probe/name/${probeId}?name=${encodeURIComponent(
          updatedName
        )}`,
        {},
        token
      );

      setHubs((prev) =>
        prev
          ? prev.map((h) => ({
              ...h,
              probes: h.probes.map((p) =>
                p.id === probeId ? { ...p, name: updatedName } : p
              ),
            }))
          : prev
      );
    } catch (error) {
      console.error(error);
    }
  };

  // ** Handle Modals **
  const openProbeModal = (probe: Probe) => {
    setSelectedProbe(probe);
    setIsProbeModalOpen(true);
  };

  const closeProbeModal = () => {
    setIsProbeModalOpen(false);
    setSelectedProbe(null);
  };

  const openHubModal = (hub: Hub) => {
    setSelectedHub(hub);
    setIsHubModalOpen(true);
  };

  const closeHubModal = () => {
    setIsHubModalOpen(false);
    setSelectedHub(null);
  };

  // If can't fetch hubs for any reason show loading screen.
  if (!hubs)
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
        {hubs.length === 0 ? (
          <div
            className={`${
              isDarkMode
                ? "bg-gray-800 border-gray-700"
                : "bg-white border-gray-200"
            } max-w-xl w-full mx-auto text-center rounded-xl shadow-lg p-5 mb-6 border`}
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
          hubs.map((hub) => (
            <div key={hub.id} className="mb-10">
              <div className="mb-4 flex flex-wrap items-center gap-3">
                <h2
                  className={`text-2xl font-bold ${
                    isDarkMode ? "text-white" : "text-gray-800"
                  }`}
                >
                  {hub.name}
                </h2>
                <span
                  className={`px-3 py-1 text-sm rounded-full ${
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
                <button
                  type="button"
                  onClick={() => hub.connected && openHubModal(hub)}
                  disabled={!hub.connected}
                  className={`inline-flex items-center px-3 py-1.5 rounded-md text-sm font-medium focus:outline-none focus:ring-2 focus:ring-offset-2 transition-colors ${
                    hub.connected
                      ? isDarkMode
                        ? "bg-amber-500/10 text-amber-300 border border-amber-400/60 hover:bg-amber-500/20 hover:border-amber-300 focus:ring-amber-400 cursor-pointer"
                        : "bg-amber-50 text-amber-800 border border-amber-200 hover:bg-amber-100 hover:border-amber-300 focus:ring-amber-400 cursor-pointer"
                      : isDarkMode
                      ? "bg-gray-800 text-gray-500 border border-gray-700 opacity-60 cursor-not-allowed focus:ring-transparent"
                      : "bg-gray-200 text-gray-500 border border-gray-300 opacity-60 cursor-not-allowed focus:ring-transparent"
                  }`}
                  aria-disabled={!hub.connected}
                >
                  View hub graph
                </button>
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
      <Modal
        open={isProbeModalOpen}
        onClose={closeProbeModal}
        wide
        title={`Temperature History - ${
          selectedProbe ? selectedProbe.name : "Probe"
        }`}
      >
        {selectedProbe ? (
          <div>
            <div className="mb-4">
              <p
                className={`text-xs uppercase tracking-wide mb-2 ${
                  isDarkMode ? "text-gray-400" : "text-gray-500"
                }`}
              >
                Probe overview
              </p>
              <div
                className={`flex items-center justify-between rounded-lg border px-3 py-2 ${
                  isDarkMode
                    ? "bg-gray-800/60 border-gray-700"
                    : "bg-gray-50 border-gray-200"
                }`}
              >
                <div>
                  <p
                    className={`text-sm font-medium ${
                      isDarkMode ? "text-gray-100" : "text-gray-900"
                    }`}
                  >
                    {selectedProbe.name || `Probe ${selectedProbe.id}`}
                  </p>
                  <p
                    className={`text-xs ${
                      isDarkMode ? "text-gray-400" : "text-gray-500"
                    }`}
                  >
                    ID: {selectedProbe.id}
                  </p>
                </div>
                <div className="text-right">
                  <span
                    className={`text-lg font-semibold ${
                      isDarkMode ? "text-white" : "text-gray-900"
                    }`}
                  >
                    {selectedProbe.currentTemp}
                  </span>
                  <span
                    className={`ml-1 text-xs ${
                      isDarkMode ? "text-gray-400" : "text-gray-500"
                    }`}
                  >
                    °F
                  </span>
                </div>
              </div>
            </div>

            <div className="border rounded-lg p-4">
              <ProbeChart probeId={selectedProbe.id} />
            </div>
          </div>
        ) : (
          <p>Loading probe...</p>
        )}
      </Modal>

      {/* Hub Modal — shows when a hub has been selected */}
      <Modal
        open={isHubModalOpen}
        onClose={closeHubModal}
        wide
        title={`Temperature History - ${
          selectedHub ? selectedHub.name : "Probe"
        }`}
      >
        {selectedHub ? (
          <div>
            <div className="mb-4">
              <p
                className={`text-xs uppercase tracking-wide mb-2 ${
                  isDarkMode ? "text-gray-400" : "text-gray-500"
                }`}
              >
                Probes overview
              </p>
              <div className="grid gap-2 sm:grid-cols-2">
                {selectedHub.probes.map((p) => (
                  <div
                    key={p.id}
                    className={`flex items-center justify-between rounded-lg border px-3 py-2 ${
                      isDarkMode
                        ? "bg-gray-800/60 border-gray-700"
                        : "bg-gray-50 border-gray-200"
                    }`}
                  >
                    <div>
                      <p
                        className={`text-sm font-medium ${
                          isDarkMode ? "text-gray-100" : "text-gray-900"
                        }`}
                      >
                        {p.name || `Probe ${p.id}`}
                      </p>
                      <p
                        className={`text-xs ${
                          isDarkMode ? "text-gray-400" : "text-gray-500"
                        }`}
                      >
                        ID: {p.id}
                      </p>
                    </div>
                    <div className="text-right">
                      <span
                        className={`text-lg font-semibold ${
                          isDarkMode ? "text-white" : "text-gray-900"
                        }`}
                      >
                        {p.currentTemp}
                      </span>
                      <span
                        className={`ml-1 text-xs ${
                          isDarkMode ? "text-gray-400" : "text-gray-500"
                        }`}
                      >
                        °F
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            </div>
            <div className="border rounded-lg p-4">
              <HubChart hub={selectedHub} />
            </div>
          </div>
        ) : (
          <p>Loading hub...</p>
        )}
      </Modal>
    </main>
  );
}

export default DashboardPage;
