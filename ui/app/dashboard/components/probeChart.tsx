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
} from "chart.js";
import "chartjs-adapter-date-fns";
import { useTheme } from "@/app/providers/ThemeProvider";
import { BASE_URL } from "@/app/utils/envVars";
import { Reading } from "@/app/types/types";

ChartJS.register(
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Tooltip,
  Legend
);

// Props for the ProbeChart component.
interface ProbeChartProps {
  probeId: number;
}

export default function ProbeChart({ probeId }: ProbeChartProps) {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  // ** States **
  const [readings, setReadings] = useState<Reading[]>([]);
  const [timeframe, setTimeframe] = useState<number>(60); // minutes, default = 1 hour
  const [loading, setLoading] = useState(false);

  // ** Fetch Readings **
  useEffect(() => {
    const end = new Date();
    const start = new Date(end.getTime() - timeframe * 60 * 1000);
    const startISO = start.toISOString();
    const endISO = end.toISOString();

    setLoading(true);
    const probeIdsParam = [probeId].join(",");
    fetch(
      `${BASE_URL}/probe/readings/between?probeIds=${probeIdsParam}&start=${startISO}&end=${endISO}`
    )
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch readings");
        return res.json();
      })
      .then((data: Record<number, Reading[]>) => {
        setReadings(data[probeId] || []);
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [probeId, timeframe]);

  const data: ChartData<"line"> = {
    labels: readings.map((r) => new Date(r.timestamp)),
    datasets: [
      {
        label: "Temperature (°F)",
        data: readings.map((r) => r.temperature),
        fill: true,
        borderColor: isDarkMode ? "#60a5fa" : "#3b82f6",
        backgroundColor: isDarkMode
          ? "rgba(96,165,250,0.12)"
          : "rgba(59,130,246,0.12)",
        tension: 0.4,
        pointRadius: 0,
        pointHoverRadius: 6,
        pointHoverBackgroundColor: isDarkMode ? "#60a5fa" : "#3b82f6",
        borderWidth: 2,
      },
    ],
  };

  const end = new Date();
  const start = new Date(end.getTime() - timeframe * 60 * 1000);

  const options: ChartOptions<"line"> = {
    responsive: true,
    maintainAspectRatio: false,
    scales: {
      x: {
        type: "time",
        time: {
          unit: timeframe <= 60 ? "minute" : timeframe <= 720 ? "hour" : "day",
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
        boxPadding: 6,
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
    const linearValue = parseInt(e.target.value);
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

      {loading ? (
        <p
          className={`text-center ${
            isDarkMode ? "text-gray-300" : "text-gray-500"
          }`}
        >
          Loading data...
        </p>
      ) : readings.length === 0 ? (
        <p
          className={`text-center ${
            isDarkMode ? "text-gray-400" : "text-gray-400"
          }`}
        >
          No readings available.
        </p>
      ) : (
        <div style={{ height: 300 }}>
          <Line data={data} options={options} />
        </div>
      )}
    </div>
  );
}
