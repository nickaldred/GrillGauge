"use client";

import { useEffect, useMemo, useState } from "react";
import { useSession } from "next-auth/react";
import { Hub, Reading } from "@/app/types/types";
import { BASE_URL } from "@/app/utils/envVars";
import { getData } from "@/app/utils/requestUtils";
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
  const { data: session } = useSession();
  // ** States **
  const [readings, setReadings] = useState<Record<string, Reading[]>>({});
  const [timeframe, setTimeframe] = useState<number>(60); // minutes, default = 1 hour
  const [loading, setLoading] = useState(false);

  const probeIds = useMemo(
    () => hub.probes.map((probe) => probe.id),
    [hub.probes]
  );
  const probeIdsKey = useMemo(() => probeIds.join(","), [probeIds]);

  const probeIdToNameMap: Record<number, string> = {};
  const probeIdToColourMap: Record<number, string | undefined> = {};
  hub.probes.forEach((probe) => {
    probeIdToNameMap[probe.id] = probe.name;
    probeIdToColourMap[probe.id] = probe.colour;
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
    const token = session?.apiToken as string | undefined;

    getData<Record<string, Reading[]>>(
      `${BASE_URL}/probe/readings/between?probeIds=${probeIdsKey}&start=${startISO}&end=${endISO}`,
      token
    )
      .then((data) => setReadings(data || {}))
      .catch((err) => console.error(err))
      .finally(() => setLoading(false));
  }, [probeIdsKey, probeIds.length, timeframe, session?.apiToken]);

  const series: TemperatureSeries[] = Object.entries(readings).map(
    ([probeId, probeReadings]) => {
      const numericId = Number(probeId);
      const displayName = probeIdToNameMap[numericId] || `Probe ${probeId}`;
      return {
        id: `probe-${probeId}`,
        name: `${displayName} - Temperature (°F)`,
        colour: probeIdToColourMap[numericId],
        points: probeReadings.map((r) => ({
          time: new Date(r.timestamp).getTime(),
          temperature: r.temperature,
        })),
      };
    }
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
