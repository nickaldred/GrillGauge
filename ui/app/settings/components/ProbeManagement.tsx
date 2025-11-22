import { Hub } from "@/app/types/types";
import { EditIcon, TrashIcon } from "lucide-react";
import { useTheme } from "@/app/providers/ThemeProvider";

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
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // States

  /**
   * Handles opening the edit probe modal.
   * @param probe The probe to edit.
   * @param hubId The ID of the hub the probe belongs to.
   */
  const handleOpenEditProbeModal = (probe: any, hubId: number) =>
    console.log("Edit probe", probe, hubId);

  /**
   * Handles opening the delete probe modal.
   *
   * @param probe The probe to delete.
   * @param hubId The ID of the hub the probe belongs to.
   */
  const handleOpenDeleteProbeModal = (probe: any, hubId: number) =>
    console.log("Delete probe", probe, hubId);

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
    <div className="overflow-x-auto">
      <table
        className={`min-w-full border ${
          isDarkMode ? "border-gray-700" : "border-gray-200"
        } text-left`}
      >
        <thead
          className={`${
            isDarkMode
              ? "bg-gray-900 text-gray-100"
              : "bg-gray-50 text-gray-900"
          }`}
        >
          <tr>
            <th className="px-4 py-2">Name</th>
            <th className="px-4 py-2">Target Temp</th>
            <th className="px-4 py-2">Status</th>
            <th className="px-4 py-2">Actions</th>
          </tr>
        </thead>
        <tbody>
          {hub.probes.map((probe) => (
            <tr
              key={probe.id}
              className={`${
                isDarkMode
                  ? "bg-gray-800 text-gray-100"
                  : "bg-white text-gray-900"
              } border-b ${isDarkMode ? "border-gray-700" : "border-gray-200"}`}
            >
              <td className="px-4 py-2">{probe.name}</td>
              <td className="px-4 py-2">{probe.targetTemp}°F</td>
              <td className="px-4 py-2">
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
              <td className="px-4 py-2 flex space-x-2">
                <button
                  onClick={() => handleOpenEditProbeModal(probe, hub.id)}
                  title="Edit Probe"
                  className={`p-2 rounded transition cursor-pointer ${
                    isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-200"
                  }`}
                >
                  <EditIcon
                    size={18}
                    className={isDarkMode ? "text-blue-400" : "text-blue-600"}
                  />
                </button>
                <button
                  onClick={() => handleOpenDeleteProbeModal(probe, hub.id)}
                  title="Delete Probe"
                  className={`p-2 rounded transition ${
                    isDarkMode ? "hover:bg-gray-700" : "hover:bg-gray-200"
                  }`}
                >
                  <TrashIcon
                    size={18}
                    className={`${
                      isDarkMode ? "text-red-400" : "text-red-600"
                    } cursor-pointer`}
                  />
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
