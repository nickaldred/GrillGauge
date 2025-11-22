"use client";

import { useState, useEffect } from "react";
import { useSession } from "next-auth/react";
import { motion, AnimatePresence } from "framer-motion";
import { Hub } from "@/app/types/types";
import { deleteRequest, getData } from "@/app/utils/requestUtils";
import { useTheme } from "@/app/providers/ThemeProvider";
import { PlusIcon, EditIcon, TrashIcon } from "lucide-react";
import Modal from "@/app/components/modal";
import { HubForm } from "./HubForm";

import { ProbeManagement } from "./ProbeManagement";
import { BASE_URL } from "@/app/utils/envVars";

export function HubManagement() {
  const { data: session } = useSession();
  const user = session?.user;
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  const [hubs, setHubs] = useState<Hub[] | null>(null);
  const [expandedHubId, setExpandedHubId] = useState<number | null>(null);

  const [isDeleteHubModalOpen, setIsDeleteHubModalOpen] = useState(false);
  const [hubToDelete, setHubToDelete] = useState<Hub | null>(null);

  const [isEditHubModalOpen, setIsEditHubModalOpen] = useState(false);
  const [hubToEdit, setHubToEdit] = useState<Hub | null>(null);

  // Fetch hubs
  useEffect(() => {
    if (!user?.email) return;

    const fetchData = () => {
      getData(`${BASE_URL}/ui/hubs?email=${user.email}`)
        .then(setHubs)
        .catch(console.error);
    };

    fetchData();
    const interval = setInterval(fetchData, 30000);
    return () => clearInterval(interval);
  }, [user?.email]);

  // Toggle expandable row
  const toggleHub = (hubId: number) =>
    setExpandedHubId((prev) => (prev === hubId ? null : hubId));

  /**
   * Handles confirming the deletion of a hub.
   */
  const handleDeleteHubConfirm = async () => {
    if (!hubToDelete) return;
    try {
      await deleteRequest(`${BASE_URL}/hub/${hubToDelete.id}`);
      setHubs((prev) => prev?.filter((h) => h.id !== hubToDelete.id) || null);
      setIsDeleteHubModalOpen(false);
      setHubToDelete(null);
    } catch (e) {
      console.error(e);
    }
  };

  /**
   * Handles submitting the edited hub data.
   *
   * @param updatedHubData The updated hub data.
   */
  const handleSubmitEditHub = async (updatedHubData: Omit<Hub, "id">) => {
    if (!hubToEdit) return;

    try {
      const hubToSend: Hub = { ...hubToEdit, ...updatedHubData };

      const res = await fetch(`${BASE_URL}/hub`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(hubToSend),
      });

      if (!res.ok) throw new Error("Failed to update hub");

      const updatedHub = await res.json();

      setHubs((prev) =>
        prev ? prev.map((h) => (h.id === updatedHub.id ? updatedHub : h)) : prev
      );
      setIsEditHubModalOpen(false);
      setHubToEdit(null);
    } catch (error) {
      console.error(error);
    }
  };

  return (
    <div
      className={`rounded-xl shadow-lg p-5 mb-6 border ${
        isDarkMode ? "bg-gray-800 border-gray-700" : "bg-white border-gray-200"
      }`}
    >
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-6 gap-4">
        <h1
          className={`text-2xl font-bold ${
            isDarkMode ? "text-white" : "text-gray-900"
          }`}
        >
          Hub Management
        </h1>
        <button
          className="px-4 py-2 bg-red-600 text-white rounded-lg flex items-center hover:bg-red-700"
          onClick={() => {}}
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
          No hubs registered yet. Click "Add New Hub" to get started.
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full table-auto border-collapse">
            <thead
              className={`${
                isDarkMode
                  ? "bg-gray-700 text-gray-300"
                  : "bg-gray-50 text-gray-600"
              }`}
            >
              <tr>
                <th className="px-4 py-3 text-left">Hub Name</th>
                <th className="px-4 py-3 text-left">Status</th>
                <th className="px-4 py-3 text-left">Probes</th>
                <th className="px-4 py-3 text-left">Actions</th>
              </tr>
            </thead>
            <tbody
              className={`${
                isDarkMode ? "divide-gray-700" : "divide-gray-200"
              } divide-y`}
            >
              {hubs.map((hub) => (
                <motion.tr
                  key={hub.id}
                  layout
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  exit={{ opacity: 0 }}
                  className={`cursor-pointer hover:${
                    isDarkMode ? "bg-gray-700" : "bg-gray-50"
                  }`}
                >
                  <td
                    className="px-4 py-3 font-medium"
                    onClick={() => toggleHub(hub.id)}
                  >
                    {hub.name}
                  </td>

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

                  <td className="px-4 py-3">{hub.probes.length}</td>

                  <td className="px-4 py-3">
                    <div className="flex space-x-2">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          setHubToEdit(hub);
                          setIsEditHubModalOpen(true);
                        }}
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
                        onClick={(e) => {
                          e.stopPropagation();
                          setHubToDelete(hub);
                          setIsDeleteHubModalOpen(true);
                        }}
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
                </motion.tr>
              ))}

              {/* Expandable Probe Rows */}
              {hubs.map(
                (hub) =>
                  expandedHubId === hub.id && (
                    <AnimatePresence key={`expand-${hub.id}`}>
                      <motion.tr
                        initial={{ opacity: 0, height: 0 }}
                        animate={{ opacity: 1, height: "auto" }}
                        exit={{ opacity: 0, height: 0 }}
                        transition={{ duration: 0.3 }}
                        className={`${
                          isDarkMode ? "bg-gray-900" : "bg-gray-50"
                        }`}
                      >
                        <td colSpan={4} className="p-4">
                          <ProbeManagement hub={hub} />
                        </td>
                      </motion.tr>
                    </AnimatePresence>
                  )
              )}
            </tbody>
          </table>
        </div>
      )}

      {/* Modals */}
      <Modal
        open={isDeleteHubModalOpen}
        onClose={() => setIsDeleteHubModalOpen(false)}
        title="Delete Hub"
      >
        <div className="space-y-4">
          <p className={isDarkMode ? "text-gray-300" : "text-gray-600"}>
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
          onSubmit={handleSubmitEditHub}
          onCancel={() => setIsEditHubModalOpen(false)}
        />
      </Modal>
    </div>
  );
}
