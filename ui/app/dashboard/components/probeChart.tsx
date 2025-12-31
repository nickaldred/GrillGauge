"use client";

import { useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import { BASE_URL } from "@/app/utils/envVars";
import { Probe, Reading } from "@/app/types/types";
import { getData } from "@/app/utils/requestUtils";
import TemperatureTimeSeriesChart, {
  type TemperatureSeries,
} from "./TemperatureTimeSeriesChart";

// Props for the ProbeChart component.
interface ProbeChartProps {
  probe: Probe;
}

export default function ProbeChart({ probe }: ProbeChartProps) {
  const { data: session } = useSession();
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
    const probeIdsParam = [probe.id].join(",");
    const token = session?.apiToken as string | undefined;

    getData<Record<number, Reading[]>>(
      `${BASE_URL}/probe/readings/between?probeIds=${probeIdsParam}&start=${startISO}&end=${endISO}`,
      token
    )
      .then((data) => setReadings(data[probe.id] || []))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [probe.id, timeframe, session?.apiToken]);
  const series: TemperatureSeries[] = [
    {
      id: `probe-${probe.id}`,
      name: "Temperature (°F)",
      colour: probe.colour,
      points: readings.map((r) => ({
        time: new Date(r.timestamp).getTime(),
        temperature: r.temperature,
      })),
    },
  ];

  return (
    <TemperatureTimeSeriesChart
      series={series}
      timeframe={timeframe}
      onTimeframeChange={setTimeframe}
      loading={loading}
    />
  );
}
