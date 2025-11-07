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

interface HubChartProps {
  hub: DashboardHub;
}

export default function HubChart({ hub }: HubChartProps) {
  const [readings, setReadings] = useState<Record<string, Reading[]>>({});
  const [timeframe, setTimeframe] = useState<number>(60); // minutes, default = 1 hour
  const [loading, setLoading] = useState(false);

  const probeIds = hub.probes.map((probe) => probe.id);

  const probeIdToNameMap: Record<number, string> = {};
  hub.probes.forEach((probe) => {
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
      .then((data: Record<string, Reading[]>) => setReadings(data || {}))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [probeIds.join(","), timeframe]);

  let chartDataSets: ChartDataset<"line">[] = [];

  let chartDataSetColours: Array<Color> = [
    "#3b82f6",
    "#f0f63bff",
    "#f6b23bff",
    "#fd0404ff",
  ];

  let dataCount = 0;
  for (const [probeId, probeReadings] of Object.entries(readings)) {
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
  }

  // build sorted, unique labels (Date objects) from all readings' timestamps
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
    scales: {
      x: {
        type: "time",
        time: {
          unit: timeUnit,
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
    const linearValue = Number.parseInt(e.target.value, 10);
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

      {(() => {
        if (loading)
          return <p className="text-center text-gray-500">Loading data...</p>;
        if (Object.values(readings).every((arr) => arr.length === 0))
          return (
            <p className="text-center text-gray-400">No readings available.</p>
          );
        return <Line data={chartData} options={options} />;
      })()}
    </div>
  );
}
