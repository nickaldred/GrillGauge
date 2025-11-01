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

ChartJS.register(
  LineElement,
  PointElement,
  LinearScale,
  TimeScale,
  Tooltip,
  Legend
);

interface Reading {
  id: number;
  timestamp: string;
  temperature: number;
}

interface ProbeChartProps {
  probeId: number;
}

export default function ProbeChart({ probeId }: ProbeChartProps) {
  const [readings, setReadings] = useState<Reading[]>([]);
  const [timeframe, setTimeframe] = useState<number>(60); // minutes, default = 1 hour
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const end = new Date();
    const start = new Date(end.getTime() - timeframe * 60 * 1000);
    const startISO = start.toISOString();
    const endISO = end.toISOString();

    setLoading(true);
    const probeIdsParam = [probeId].join(",");
    fetch(
      `http://localhost:8080/api/v1/probe/readings/between?probeIds=${probeIdsParam}&start=${startISO}&end=${endISO}`
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
        fill: false,
        borderColor: "#3b82f6",
        backgroundColor: "#3b82f6",
        tension: 0.3,
        pointRadius: 0,
      },
    ],
  };

  const end = new Date();
  const start = new Date(end.getTime() - timeframe * 60 * 1000);

  const options: ChartOptions<"line"> = {
    responsive: true,
    scales: {
      x: {
        type: "time",
        time: {
          unit: timeframe <= 60 ? "minute" : timeframe <= 720 ? "hour" : "day",
        },
        min: start.getTime(),
        max: end.getTime(),
        title: { display: true, text: "Time" },
      },
      y: {
        title: { display: true, text: "Temperature (°F)" },
      },
    },
    plugins: {
      legend: { display: false },
      tooltip: { mode: "index", intersect: false },
    },
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
        <span className="text-sm text-gray-600">
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
          className="w-1/2 accent-blue-600"
        />
      </div>

      {loading ? (
        <p className="text-center text-gray-500">Loading data...</p>
      ) : readings.length === 0 ? (
        <p className="text-center text-gray-400">No readings available.</p>
      ) : (
        <Line data={data} options={options} />
      )}
    </div>
  );
}
