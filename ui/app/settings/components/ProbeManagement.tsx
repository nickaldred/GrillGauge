import { Hub, Probe } from "@/app/types/types";
import { EditIcon, TrashIcon, Eye, EyeOff } from "lucide-react";
import { useTheme } from "@/app/providers/ThemeProvider";
import Modal from "@/app/components/modal";
import { useState, useEffect } from "react";
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
  const [probes, setProbes] = useState<Probe[]>(hub.probes);

  useEffect(() => {
    setProbes(hub.probes);
  }, [hub.probes]);

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
      setProbes((prev) => prev.filter((p) => p.id !== probeToDelete.id));
      setIsDeleteProbeModalOpen(false);
      setProbeToDelete(null);
    } catch (e) {
      console.error(e);
    }
  };

  /**
   * Handles submitting the edited probe data.
   *
   * @param updatedProbeData The updated probe data.
   */
  const handleSubmitEditProbe = async (updatedProbeData: Omit<Probe, "id">) => {
    if (!probeToEdit) return;
    try {
      const probeToSend: Probe = { ...probeToEdit, ...updatedProbeData };
      await updateProbeOnServer(probeToSend);
      setIsEditProbeModalOpen(false);
      setProbeToEdit(null);
    } catch (error) {
      console.error(error);
    }
  };

  /**
   * Updates a probe on the server and updates the local hub state.
   *
   * @param probeToSend The probe data to send to the server.
   * @returns The updated probe from the server.
   */
  const updateProbeOnServer = async (probeToSend: Probe) => {
    const token = session?.apiToken as string | undefined;
    const updatedProbe = (await putRequest(
      `${BASE_URL}/probe`,
      probeToSend,
      token
    )) as Probe;

    hub.probes = hub.probes.map((p) =>
      p.id === updatedProbe.id ? updatedProbe : p
    );
    setProbes((prev) =>
      prev.map((p) => (p.id === updatedProbe.id ? updatedProbe : p))
    );
    return updatedProbe;
  };

  /**
   * Handles toggling the visibility of a probe.
   *
   * @param probe The probe to toggle visibility for.
   */
  const handleToggleVisibility = async (probe: Probe) => {
    try {
      const updatedProbe: Probe = { ...probe, visible: !probe.visible };
      await updateProbeOnServer(updatedProbe);
    } catch (error) {
      console.error(error);
    }
  };

  // *** Render ***
  if (probes.length === 0) {
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
      className={`space-y-4 text-sm ${
        isDarkMode ? "text-gray-200" : "text-gray-800"
      }`}
    >
      <h3
        className={`text-base font-semibold tracking-tight ${
          isDarkMode ? "text-gray-100" : "text-gray-900"
        }`}
      >
        Probe Management
      </h3>
      <div className="overflow-x-auto">
        <table className="w-full table-auto border-collapse">
          <thead
            className={`${
              isDarkMode
                ? "text-gray-400 border-b border-gray-700"
                : "text-gray-500 border-b border-gray-200"
            }`}
          >
            <tr>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide">
                Name
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide">
                Status
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide">
                Target Temp
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide">
                Visible
              </th>
              <th className="px-4 py-3 text-left text-xs font-semibold uppercase tracking-wide">
                Actions
              </th>
            </tr>
          </thead>

          <tbody
            className={`${
              isDarkMode ? "divide-gray-700" : "divide-gray-200"
            } divide-y`}
          >
            {probes.map((probe) => (
              <tr
                key={probe.id}
                className={
                  isDarkMode ? "hover:bg-gray-800" : "hover:bg-gray-100"
                }
              >
                <td className="px-4 py-3 font-medium">
                  <span
                    className={isDarkMode ? "text-gray-100" : "text-gray-900"}
                  >
                    {probe.name}
                  </span>
                </td>

                <td className="px-4 py-3">
                  <span
                    className={`px-2 py-1 rounded-full text-xs ${
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
                </td>

                <td className="px-4 py-3">
                  <span
                    className={isDarkMode ? "text-gray-300" : "text-gray-800"}
                  >
                    {probe.targetTemp}°F
                  </span>
                </td>

                <td className="px-4 py-3">
                  <button
                    type="button"
                    onClick={() => handleToggleVisibility(probe)}
                    className={`inline-flex items-center px-2 py-1 rounded-lg text-xs font-medium border cursor-pointer transition-colors ${
                      probe.visible
                        ? isDarkMode
                          ? "border-green-400 text-green-300 bg-green-900/20 hover:bg-green-900/30"
                          : "border-green-500 text-green-700 bg-green-50 hover:bg-green-100"
                        : isDarkMode
                        ? "border-gray-600 text-gray-500 bg-gray-800/60 hover:bg-gray-700/80"
                        : "border-gray-300 text-gray-400 bg-gray-100 hover:bg-gray-200"
                    }`}
                    title={probe.visible ? "Hide probe" : "Show probe"}
                  >
                    {probe.visible ? (
                      <Eye className="w-4 h-4 mr-1" />
                    ) : (
                      <EyeOff className="w-4 h-4 mr-1" />
                    )}
                    <span>{probe.visible ? "Visible" : "Hidden"}</span>
                  </button>
                </td>

                <td className="px-4 py-3">
                  <div className="flex space-x-2">
                    <button
                      onClick={() => handleOpenEdit(probe)}
                      className={`p-1 rounded cursor-pointer ${
                        isDarkMode ? "hover:bg-gray-600" : "hover:bg-gray-100"
                      }`}
                      title="Edit"
                    >
                      <EditIcon
                        size={18}
                        className={
                          isDarkMode ? "text-blue-400" : "text-blue-600"
                        }
                      />
                    </button>

                    <button
                      onClick={() => handleOpenDelete(probe)}
                      className={`p-1 rounded cursor-pointer ${
                        isDarkMode ? "hover:bg-gray-600" : "hover:bg-gray-100"
                      }`}
                      title="Delete"
                    >
                      <TrashIcon
                        size={18}
                        className={isDarkMode ? "text-red-400" : "text-red-600"}
                      />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
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
