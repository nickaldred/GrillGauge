"use client";

import Modal from "@/app/components/modal";
import { useTheme } from "@/app/providers/ThemeProvider";
import { Hub } from "@/app/types/types";
import { deleteHub, getData } from "@/app/utils/requestUtils";
import { EditIcon, PlusIcon, TrashIcon } from "lucide-react";
import { useSession } from "next-auth/react";
import { useEffect, useState } from "react";
import { HubForm } from "./hubForm";

/**
 * Component to allow users to manage Hubs.
 */
export function HubManagement() {
  const { data: session } = useSession();
  const user = session?.user;
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  const [isDeleteHubModalOpen, setIsDeleteHubModalOpen] = useState(false);
  const [hubToDelete, setHubToDelete] = useState<Hub | null>(null);
  const [isEditHubModalOpen, setIsEditHubModalOpen] = useState(false);
  const [hubToEdit, setHubToEdit] = useState<Hub | null>(null);

  function handleOpenAddHubModal() {}

  const handleOpenDeleteHubModal = (hub: Hub) => {
    setHubToDelete(hub);
    setIsDeleteHubModalOpen(true);
  };

  const handleDeleteHubConfirm = async () => {
    if (!hubToDelete) return;
    try {
      await deleteHub(hubToDelete.id);
      setHubs(
        (prevHubs) =>
          prevHubs?.filter((hub) => hub.id !== hubToDelete.id) || null
      );
      setIsDeleteHubModalOpen(false);
      setHubToDelete(null);
    } catch (error) {
      console.error("Error deleting hub:", error);
    }
  };

  function handleOpenEditHubModal(hub: Hub) {
    setHubToEdit(hub);
    setIsEditHubModalOpen(true);
  }

  const [hubs, setHubs] = useState<Hub[] | null>(null);

  useEffect(() => {
    if (!user?.email) return;

    const fetchData = () => {
      const email = user.email!;
      const url =
        "http://localhost:8080/api/v1/ui/hubs?email=" +
        encodeURIComponent(email);

      getData(url)
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
  }, [user?.email]);

  return (
    <div
      className={`${
        isDarkMode ? "bg-gray-800 border-gray-700" : "bg-white border-gray-200"
      } rounded-xl shadow-lg p-5 mb-6 border`}
    >
      <div className="flex justify-between items-center mb-6">
        <h1
          className={`text-2xl font-bold ${
            isDarkMode ? "text-white" : "text-gray-900"
          }`}
        >
          Hub Management
        </h1>
        <button
          onClick={handleOpenAddHubModal}
          className={`px-4 py-2 bg-red-600 text-white rounded-lg flex items-center hover:bg-red-700 cursor-pointer`}
        >
          <PlusIcon size={18} className="mr-2" />
          Add New Hub
        </button>
      </div>

      {!hubs || hubs.length === 0 ? (
        <div
          className={`text-center py-12 ${
            isDarkMode ? "text-gray-400" : "text-gray-500"
          }`}
        >
          <p>No hubs registered yet. Click "Add New Hub" to get started.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead
              className={`${
                isDarkMode ? "bg-gray-700" : "bg-gray-50"
              } text-left`}
            >
              <tr>
                <th
                  className={`px-4 py-3 ${
                    isDarkMode ? "text-gray-300" : "text-gray-600"
                  }`}
                >
                  Hub Name
                </th>
                <th
                  className={`px-4 py-3 ${
                    isDarkMode ? "text-gray-300" : "text-gray-600"
                  }`}
                >
                  Status
                </th>
                <th
                  className={`px-4 py-3 ${
                    isDarkMode ? "text-gray-300" : "text-gray-600"
                  }`}
                >
                  Probes
                </th>
                <th
                  className={`px-4 py-3 ${
                    isDarkMode ? "text-gray-300" : "text-gray-600"
                  }`}
                >
                  Actions
                </th>
              </tr>
            </thead>

            <tbody
              className={`${
                isDarkMode ? "divide-gray-700" : "divide-gray-200"
              } divide-y`}
            >
              {hubs.map((hub) => (
                <tr
                  key={hub.id}
                  className={`${
                    isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-50"
                  }`}
                >
                  <td className="px-4 py-3 font-medium">{hub.name}</td>

                  <td className="px-4 py-3">
                    <span
                      className={`px-2 py-1 rounded-full text-xs ${
                        hub.connected
                          ? isDarkMode
                            ? "bg-green-900 text-green-300"
                            : "bg-green-100 text-green-800"
                          : isDarkMode
                          ? "bg-gray-700 text-gray-300"
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
                        className={`p-1 rounded cursor-pointer ${
                          isDarkMode ? "hover:bg-gray-600" : "hover:bg-gray-100"
                        }`}
                        title="Edit Hub"
                      >
                        <EditIcon
                          size={18}
                          className={
                            isDarkMode ? "text-blue-400" : "text-blue-600"
                          }
                        />
                      </button>

                      <button
                        onClick={() => handleOpenDeleteHubModal(hub)}
                        className={`p-1 rounded cursor-pointer ${
                          isDarkMode ? "hover:bg-gray-600" : "hover:bg-gray-100"
                        }`}
                        title="Delete Hub"
                      >
                        <TrashIcon
                          size={18}
                          className={
                            isDarkMode ? "text-red-400" : "text-red-600"
                          }
                        />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal
        open={isDeleteHubModalOpen}
        onClose={() => setIsDeleteHubModalOpen(false)}
        title="Delete Hub"
      >
        <div className="space-y-4">
          <p className={`${isDarkMode ? "text-gray-300" : "text-gray-600"}`}>
            Are you sure you want to delete{" "}
            <span className="font-semibold">{hubToDelete?.name}</span>? This
            will also delete all probes associated with this hub. This action
            cannot be undone.
          </p>
          <div className="flex justify-end space-x-3">
            <button
              onClick={() => setIsDeleteHubModalOpen(false)}
              className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 cursor-pointer"
            >
              Cancel
            </button>
            <button
              onClick={handleDeleteHubConfirm}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 cursor-pointer"
            >
              Delete
            </button>
          </div>
        </div>
      </Modal>

      <Modal
        open={isEditHubModalOpen}
        onClose={() => setIsEditHubModalOpen(false)}
        title="Edit Hub"
      >
        <HubForm
          hub={hubToEdit}
          onSubmit={() => setIsEditHubModalOpen(false)}
          onCancel={() => setIsEditHubModalOpen(false)}
        ></HubForm>
      </Modal>
    </div>
  );
}
