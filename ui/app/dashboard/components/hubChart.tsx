"use client";

import { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";
import {
  Chart as ChartJS,
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Tooltip,
  Legend,
  ChartOptions,
  ChartData,
  ChartDataset,
  Color,
} from "chart.js";
import "chartjs-adapter-date-fns";
import { useTheme } from "@/app/providers/ThemeProvider";
import { Hub, Reading } from "@/app/types/types";
import { BASE_URL } from "@/app/utils/envVars";

ChartJS.register(
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Tooltip,
  Legend
);

// Props for the HubChart component.
interface HubChartProps {
  hub: Hub;
}

/**
 * The HubChart component displays a line chart of temperature readings
 * for all probes associated with a given hub over a selectable timeframe.
 *
 * @param hub The hub whose probes' readings are to be displayed.
 * @returns The HubChart component.
 */
export default function HubChart({ hub }: HubChartProps) {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** States **
  const [readings, setReadings] = useState<Record<string, Reading[]>>({});
  const [timeframe, setTimeframe] = useState<number>(60); // minutes, default = 1 hour
  const [loading, setLoading] = useState(false);

  const probeIds = hub.probes.map((probe) => probe.id);

  const probeIdToNameMap: Record<number, string> = {};
  hub.probes.forEach((probe) => {
    probeIdToNameMap[probe.id] = probe.name;
  });

  // ** Fetch Readings **
  useEffect(() => {
    const end = new Date();
    const start = new Date(end.getTime() - timeframe * 60 * 1000);
    const startISO = start.toISOString();
    const endISO = end.toISOString();

    setLoading(true);
    fetch(
      `${BASE_URL}/probe/readings/between?probeIds=${probeIds.join(
        ","
      )}&start=${startISO}&end=${endISO}`
    )
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch readings");
        return res.json();
      })
      .then((data: Record<string, Reading[]>) => setReadings(data || {}))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [probeIds.join(","), timeframe]);

  const chartDataSets: ChartDataset<"line">[] = [];

  const chartDataSetColours: Array<string> = [
    "#3b82f6",
    "#f59e0b",
    "#f97316",
    "#ef4444",
  ];

  const hexToRgba = (hex: string, alpha: number) => {
    // Normalise hex (#rrggbb or rrggbb or #rrggbbaa)
    let h = hex.replace(/^#/, "");
    if (h.length === 8) h = h.slice(0, 6); // drop alpha if present
    if (h.length === 3)
      h = h
        .split("")
        .map((c) => c + c)
        .join("");
    const r = parseInt(h.slice(0, 2), 16);
    const g = parseInt(h.slice(2, 4), 16);
    const b = parseInt(h.slice(4, 6), 16);
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  };

  let dataCount = 0;
  for (const [probeId, probeReadings] of Object.entries(readings)) {
    const baseColor =
      chartDataSetColours[dataCount % chartDataSetColours.length];
    chartDataSets.push({
      label: probeIdToNameMap[Number(probeId)] + " - Temperature (°F)",
      data: probeReadings.map((r) => r.temperature),
      fill: false,
      borderColor: baseColor,
      backgroundColor: hexToRgba(baseColor, 0.12),
      tension: 0.4,
      pointRadius: 0,
      pointHoverRadius: 6,
      borderWidth: 2,
    });
    dataCount += 1;
  }

  const allTimestamps = Object.values(readings)
    .flat()
    .map((r) => new Date(r.timestamp).getTime());
  const uniqueSortedTimestamps = Array.from(new Set(allTimestamps)).sort(
    (a, b) => a - b
  );
  const labels = uniqueSortedTimestamps.map((ts) => new Date(ts));

  const chartData: ChartData<"line"> = {
    labels,
    datasets: chartDataSets,
  };

  const end = new Date();
  const start = new Date(end.getTime() - timeframe * 60 * 1000);

  // choose a sensible time unit for the X axis
  let timeUnit: "minute" | "hour" | "day" = "day";
  if (timeframe <= 60) timeUnit = "minute";
  else if (timeframe <= 720) timeUnit = "hour";

  const options: ChartOptions<"line"> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        type: "time",
        time: {
          unit: timeUnit,
        },
        min: start.getTime(),
        max: end.getTime(),
        title: {
          display: true,
          text: "Time",
          color: isDarkMode ? "#94a3b8" : "#374151",
        },
        grid: {
          color: isDarkMode ? "rgba(255,255,255,0.03)" : "rgba(0,0,0,0.04)",
        },
        ticks: { color: isDarkMode ? "#cbd5e1" : "#374151" },
      },
      y: {
        title: {
          display: true,
          text: "Temperature (°F)",
          color: isDarkMode ? "#94a3b8" : "#374151",
        },
        grid: {
          color: isDarkMode ? "rgba(255,255,255,0.03)" : "rgba(0,0,0,0.04)",
        },
        ticks: { color: isDarkMode ? "#cbd5e1" : "#374151" },
      },
    },
    plugins: {
      legend: { display: false },
      tooltip: {
        mode: "index",
        intersect: false,
        backgroundColor: isDarkMode ? "#0f172a" : "#ffffff",
        titleColor: isDarkMode ? "#e6eef8" : "#111827",
        bodyColor: isDarkMode ? "#cbd5e1" : "#111827",
        padding: 8,
      },
    },
    interaction: { mode: "index", intersect: false },
    layout: { padding: { top: 6, right: 6, bottom: 6, left: 6 } },
  };

  const sliderToTimeframe = (sliderValue: number) => {
    const min = 5;
    const max = 1440;
    const t = (sliderValue - min) / (max - min);
    const scaled = Math.pow(t, 2);

    return Math.round(min + scaled * (max - min));
  };

  const handleSliderChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const linearValue = Number.parseInt(e.target.value, 10);
    const newTimeframe = sliderToTimeframe(linearValue);
    setTimeframe(newTimeframe);
  };

  return (
    <div className="w-full">
      <div className="flex justify-between items-center mb-4">
        <span
          className={`text-sm ${
            isDarkMode ? "text-gray-300" : "text-gray-600"
          }`}
        >
          Timeframe: last{" "}
          {timeframe >= 60 ? `${timeframe / 60}h` : `${timeframe}m`}
        </span>
        <input
          type="range"
          min="5"
          max="1440"
          step="1"
          value={Math.round(
            Math.sqrt((timeframe - 5) / (1440 - 5)) * (1440 - 5) + 5
          )}
          onChange={handleSliderChange}
          className={`w-1/2 ${
            isDarkMode ? "accent-sky-400" : "accent-blue-600"
          }`}
        />
      </div>

      {(() => {
        if (loading)
          return (
            <p
              className={`text-center ${
                isDarkMode ? "text-gray-300" : "text-gray-500"
              }`}
            >
              Loading data...
            </p>
          );
        if (Object.values(readings).every((arr) => arr.length === 0))
          return (
            <p
              className={`text-center ${
                isDarkMode ? "text-gray-400" : "text-gray-400"
              }`}
            >
              No readings available.
            </p>
          );
        return (
          <div style={{ height: 300 }}>
            <Line data={chartData} options={options} />
          </div>
        );
      })()}
    </div>
  );
}
