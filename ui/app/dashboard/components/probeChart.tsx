"use client";

import { useEffect, useState } from "react";
import { useSession } from "next-auth/react";
import { BASE_URL } from "@/app/utils/envVars";
import { Reading } from "@/app/types/types";
import { getData } from "@/app/utils/requestUtils";
import TemperatureTimeSeriesChart, {
  type TemperatureSeries,
} from "./TemperatureTimeSeriesChart";

// Props for the ProbeChart component.
interface ProbeChartProps {
  probeId: number;
}

export default function ProbeChart({ probeId }: ProbeChartProps) {
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
    const probeIdsParam = [probeId].join(",");
    // @ts-expect-error apiToken is added in auth.ts session callback
    const token = session?.apiToken as string | undefined;

    getData(
      `${BASE_URL}/probe/readings/between?probeIds=${probeIdsParam}&start=${startISO}&end=${endISO}`,
      token
    )
      .then((data: Record<number, Reading[]>) => {
        setReadings(data[probeId] || []);
      })
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [probeId, timeframe, session]);
  const series: TemperatureSeries[] = [
    {
      id: `probe-${probeId}`,
      name: "Temperature (°F)",
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
