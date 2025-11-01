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
import { DashboardHub } from "@/app/types/types";

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

interface ProbesChartProps {
  hubs: DashboardHub;
}

export default function ProbesChart({ hubs }: ProbesChartProps) {
  const [readings, setReadings] = useState<Map<String, Reading[]>>(new Map());
  const [timeframe, setTimeframe] = useState<number>(60); // minutes, default = 1 hour
  const [loading, setLoading] = useState(false);

  const probeIds = hubs.probes.map((probe) => probe.id);

  const probeIdToNameMap: Record<number, string> = {};
  hubs.probes.forEach((probe) => {
    probeIdToNameMap[probe.id] = probe.name;
  });

  useEffect(() => {
    const end = new Date();
    const start = new Date(end.getTime() - timeframe * 60 * 1000);
    const startISO = start.toISOString();
    const endISO = end.toISOString();

    setLoading(true);
    fetch(
      `http://localhost:8080/api/v1/probe/readings/between?probeIds=${probeIds.join(
        ","
      )}&start=${startISO}&end=${endISO}`
    )
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch readings");
        return res.json();
      })
      .then((data) => setReadings(data))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [probeIds.keys, timeframe]);

  let chartDataSets: ChartDataset<"line">[] = [];

  let chartDataSetColours: Array<Color> = [
    "#3b82f6",
    "#f0f63bff",
    "#f6b23bff",
    "#fd0404ff",
  ];

  let dataCount = 0;
  readings.forEach((probeReadings, probeId) => {
    chartDataSets.push({
      label: probeIdToNameMap[Number(probeId)] + " - Temperature (°F)",
      data: probeReadings.map((r) => r.temperature),
      fill: false,
      borderColor: chartDataSetColours[dataCount],
      backgroundColor: chartDataSetColours[dataCount],
      tension: 0.3,
      pointRadius: 0,
    });
    dataCount += 1;
  });

  const chartData: ChartData<"line"> = {
    labels: Array.from(new Set(Array.from(readings.values()).flat())),
    datasets: chartDataSets,
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
      ) : Array.from(readings.values()).every((arr) => arr.length === 0) ? (
        <p className="text-center text-gray-400">No readings available.</p>
      ) : (
        <Line data={chartData} options={options} />
      )}
    </div>
  );
}
