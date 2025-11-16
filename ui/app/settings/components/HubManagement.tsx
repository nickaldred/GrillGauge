"use client";

import { useTheme } from "@/app/providers/ThemeProvider";
import { DashboardType, DashboardHub } from "@/app/types/types";
import { getData } from "@/app/utils/requestUtils";
import { EditIcon, PlusIcon, TrashIcon } from "lucide-react";
import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";

export function HubManagement() {
  const { data: session } = useSession();
  const user = session?.user;

  function handleOpenAddHubModal() {}
  function handleOpenDeleteHubModal(hub: DashboardHub) {}
  function handleOpenEditHubModal(hub: DashboardHub) {}

  const [dashboard, setDashboard] = useState<DashboardType | null>(null);

  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

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

  return (
    <div className="bg-white rounded-xl shadow-lg p-6 border border-gray-200 mb-6">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Hub Management</h1>
        <button
          onClick={handleOpenAddHubModal}
          className="px-4 py-2 bg-red-600 text-white rounded-lg flex items-center hover:bg-red-700"
        >
          <PlusIcon size={18} className="mr-2" />
          Add New Hub
        </button>
      </div>
      {!dashboard || dashboard.hubs.length === 0 ? (
        <div className="text-center py-12 text-gray-500">
          <p>No hubs registered yet. Click "Add New Hub" to get started.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-gray-50 text-left">
              <tr>
                <th className="px-4 py-3 text-gray-600">Hub Name</th>
                <th className="px-4 py-3 text-gray-600">Status</th>
                <th className="px-4 py-3 text-gray-600">Probes</th>
                <th className="px-4 py-3 text-gray-600">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {dashboard.hubs.map((hub) => (
                <tr key={hub.id} className="hover:bg-gray-50">
                  <td className="px-4 py-3 font-medium">{hub.name}</td>
                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-1 rounded-full text-xs ${
                        hub.connected
                          ? "bg-green-100 text-green-800"
                          : "bg-gray-100 text-gray-800"
                      }`}
                    >
                      {hub.connected ? "Connected" : "Disconnected"}
                    </span>
                  </td>
                  <td className="px-4 py-3">
                    {hub.probes.length}{" "}
                    {hub.probes.length === 1 ? "probe" : "probes"}
                  </td>
                  <td className="px-4 py-3">
                    <div className="flex space-x-2">
                      <button
                        onClick={() => handleOpenEditHubModal(hub)}
                        className="p-1 hover:bg-gray-100 rounded"
                        title="Edit Hub"
                      >
                        <EditIcon size={18} className="text-blue-600" />
                      </button>
                      <button
                        onClick={() => handleOpenDeleteHubModal(hub)}
                        className="p-1 hover:bg-gray-100 rounded"
                        title="Delete Hub"
                      >
                        <TrashIcon size={18} className="text-red-600" />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
