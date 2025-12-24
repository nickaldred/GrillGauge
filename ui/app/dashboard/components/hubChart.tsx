"use client";

import { useEffect, useState } from "react";
import { Hub, Reading } from "@/app/types/types";
import { BASE_URL } from "@/app/utils/envVars";
import TemperatureTimeSeriesChart, {
  type TemperatureSeries,
} from "./TemperatureTimeSeriesChart";

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
    if (!probeIds.length) {
      setReadings({});
      return;
    }

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

  const series: TemperatureSeries[] = Object.entries(readings).map(
    ([probeId, probeReadings]) => ({
      id: `probe-${probeId}`,
      name: `${probeIdToNameMap[Number(probeId)]} - Temperature (°F)`,
      points: probeReadings.map((r) => ({
        time: new Date(r.timestamp).getTime(),
        temperature: r.temperature,
      })),
    })
  );

  return (
    <TemperatureTimeSeriesChart
      series={series}
      timeframe={timeframe}
      onTimeframeChange={setTimeframe}
      loading={loading}
    />
  );
}
