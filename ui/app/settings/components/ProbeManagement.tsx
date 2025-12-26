import { Hub, Probe } from "@/app/types/types";
import { EditIcon, TrashIcon } from "lucide-react";
import { useTheme } from "@/app/providers/ThemeProvider";
import Modal from "@/app/components/modal";
import { useState } from "react";
import { useSession } from "next-auth/react";
import { deleteRequest, putRequest } from "@/app/utils/requestUtils";
import { BASE_URL } from "@/app/utils/envVars";
import { ProbeForm } from "./ProbeForm";

// Props for the ProbeManagement component.
interface ProbeManagementProps {
  hub: Hub;
}
/**
 * The ProbeManagement component displays and manages probes for a given hub.
 *
 * @param hub The hub whose probes are to be managed.
 * @returns The ProbeManagement component.
 */
export function ProbeManagement({ hub }: ProbeManagementProps) {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** Auth **
  const { data: session } = useSession();

  // ** States **
  const [isEditProbeModalOpen, setIsEditProbeModalOpen] = useState(false);
  const [probeToEdit, setProbeToEdit] = useState<Probe | null>(null);
  const [isDeleteProbeModalOpen, setIsDeleteProbeModalOpen] = useState(false);
  const [probeToDelete, setProbeToDelete] = useState<Probe | null>(null);

  // *** Handle Modals ***

  /**
   * Handles opening the edit probe modal.
   * @param probe The probe to edit.
   */
  const handleOpenEdit = (probe: Probe) => {
    setProbeToEdit(probe);
    setIsEditProbeModalOpen(true);
  };

  /**
   * Handles opening the delete probe modal.
   *
   * @param probe The probe to delete.
   */
  const handleOpenDelete = (probe: Probe) => {
    setProbeToDelete(probe);
    setIsDeleteProbeModalOpen(true);
  };

  // *** Utility Functions ***

  /**
   * Handles confirming the deletion of a probe.
   */
  const handleDeleteConfirm = async () => {
    if (!probeToDelete) return;
    try {
      const token = session?.apiToken as string | undefined;
      await deleteRequest(`${BASE_URL}/probe/${probeToDelete.id}`, token);
      hub.probes = hub.probes.filter((p) => p.id !== probeToDelete.id);
      setIsDeleteProbeModalOpen(false);
      setProbeToDelete(null);
    } catch (e) {
      console.error(e);
    }
  };

  const handleSubmitEditProbe = async (updatedProbeData: Omit<Probe, "id">) => {
    if (!probeToEdit) return;
    try {
      const probeToSend: Probe = { ...probeToEdit, ...updatedProbeData };
      const token = session?.apiToken as string | undefined;
      const updatedProbe = (await putRequest(
        `${BASE_URL}/probe`,
        probeToSend,
        token
      )) as Probe;
      hub.probes = hub.probes.map((p) =>
        p.id === updatedProbe.id ? updatedProbe : p
      );
      setIsEditProbeModalOpen(false);
      setProbeToEdit(null);
    } catch (error) {
      console.error(error);
    }
  };

  // *** Render ***
  if (hub.probes.length === 0) {
    return (
      <div
        className={`text-center py-4 ${
          isDarkMode ? "text-gray-400" : "text-gray-500"
        }`}
      >
        No probes yet.
      </div>
    );
  }

  return (
    <div
      className={`space-y-5 p-6 rounded-xl border shadow-sm backdrop-blur ${
        isDarkMode ? "bg-gray-800 border-gray-700" : "bg-white border-gray-200"
      }`}
    >
      <h3
        className={`text-lg font-semibold tracking-tight ${
          isDarkMode ? "text-gray-100" : "text-gray-900"
        }`}
      >
        Probe Management
      </h3>

      <div className="grid grid-cols-4 gap-4 text-xs font-semibold uppercase tracking-wide text-gray-500 dark:text-gray-400 pb-2 border-b border-gray-200 dark:border-gray-700">
        <span>Name</span>
        <span>Target Temp</span>
        <span>Status</span>
        <span>Actions</span>
      </div>

      <div className="space-y-3">
        {hub.probes.map((probe) => (
          <div
            key={probe.id}
            className={`grid grid-cols-4 gap-4 items-center p-4 rounded-lg border shadow-sm hover:shadow transition duration-200 ${
              isDarkMode
                ? "bg-gray-700 border-gray-600"
                : "bg-gray-50 border-gray-200"
            }`}
          >
            <span
              className={`font-medium ${
                isDarkMode ? "text-gray-100" : "text-gray-900"
              }`}
            >
              {probe.name}
            </span>
            <span
              className={`${isDarkMode ? "text-gray-300" : "text-gray-800"}`}
            >
              {probe.targetTemp}°F
            </span>
            <span
              className={`px-3 py-1 rounded-full text-xs font-medium w-fit ${
                probe.connected
                  ? isDarkMode
                    ? "bg-green-900 text-green-300"
                    : "bg-green-100 text-green-800"
                  : isDarkMode
                  ? "bg-gray-700 text-gray-300"
                  : "bg-gray-100 text-gray-800"
              }`}
            >
              {probe.connected ? "Connected" : "Disconnected"}
            </span>
            <div className="flex space-x-2">
              <button
                onClick={() => handleOpenEdit(probe)}
                className={`p-2 rounded-lg transition cursor-pointer ${
                  isDarkMode ? "hover:bg-gray-600" : "hover:bg-gray-100"
                }`}
                title="Edit"
              >
                <EditIcon
                  className={`${
                    isDarkMode ? "text-blue-400" : "text-blue-600"
                  }`}
                  size={18}
                />
              </button>
              <button
                onClick={() => handleOpenDelete(probe)}
                className={`p-2 rounded-lg transition cursor-pointer ${
                  isDarkMode ? "hover:bg-gray-600" : "hover:bg-gray-100"
                }`}
                title="Delete"
              >
                <TrashIcon
                  className={`${isDarkMode ? "text-red-400" : "text-red-600"}`}
                  size={18}
                />
              </button>
            </div>
          </div>
        ))}
      </div>

      <Modal
        open={isEditProbeModalOpen}
        onClose={() => setIsEditProbeModalOpen(false)}
        title="Edit Probe"
      >
        <ProbeForm
          probe={probeToEdit}
          onSubmit={handleSubmitEditProbe}
          onCancel={() => setIsEditProbeModalOpen(false)}
        />
      </Modal>

      <Modal
        open={isDeleteProbeModalOpen}
        onClose={() => setIsDeleteProbeModalOpen(false)}
        title="Delete Probe"
      >
        <div className="space-y-4">
          <p className={isDarkMode ? "text-gray-300" : "text-gray-600"}>
            Are you sure you want to delete{" "}
            <span className="font-semibold">{probeToDelete?.name}</span>? This
            will also delete all readings associated with this probe.
          </p>
          <div className="flex justify-end space-x-3">
            <button
              onClick={() => setIsDeleteProbeModalOpen(false)}
              className={`px-4 py-2 border rounded-lg ${
                isDarkMode
                  ? "border-gray-600 text-gray-300 hover:bg-gray-600"
                  : "border-gray-300 text-gray-700 hover:bg-gray-50"
              }`}
            >
              Cancel
            </button>
            <button
              onClick={handleDeleteConfirm}
              className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
            >
              Delete
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
}
