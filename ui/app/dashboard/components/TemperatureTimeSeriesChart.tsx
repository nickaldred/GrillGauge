"use client";

import { ChangeEvent } from "react";
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip as RechartsTooltip,
  ResponsiveContainer,
  type TooltipProps,
} from "recharts";
import { format } from "date-fns";
import { useTheme } from "@/app/providers/ThemeProvider";

export interface TemperaturePoint {
  time: number; // Unix ms timestamp
  temperature: number;
}

export interface TemperatureSeries {
  id: string;
  name: string;
  colour?: string;
  points: TemperaturePoint[];
}

interface TemperatureTimeSeriesChartProps {
  series: TemperatureSeries[];
  timeframe: number; // minutes
  onTimeframeChange: (minutes: number) => void;
  loading: boolean;
}

const SLIDER_MIN = 5;
const SLIDER_MAX = 1440;

/** Convert slider value to timeframe in minutes */
const sliderToTimeframe = (sliderValue: number) => {
  const t = (sliderValue - SLIDER_MIN) / (SLIDER_MAX - SLIDER_MIN);
  const scaled = Math.pow(t, 2);
  return Math.round(SLIDER_MIN + scaled * (SLIDER_MAX - SLIDER_MIN));
};

/** Convert timeframe in minutes to slider value */
const timeframeToSlider = (timeframe: number) => {
  const t = (timeframe - SLIDER_MIN) / (SLIDER_MAX - SLIDER_MIN);
  const linear =
    Math.sqrt(Math.max(t, 0)) * (SLIDER_MAX - SLIDER_MIN) + SLIDER_MIN;
  return Math.round(linear);
};

/** Format the timeframe label for display */
const formatTimeframeLabel = (timeframeMinutes: number) => {
  if (!Number.isFinite(timeframeMinutes) || timeframeMinutes <= 0) {
    return "";
  }

  if (timeframeMinutes < 60) {
    return `${Math.round(timeframeMinutes)}m`;
  }

  const hours = timeframeMinutes / 60;
  const rounded = Math.round(hours * 10) / 10;

  return Number.isInteger(rounded)
    ? `${rounded.toFixed(0)}h`
    : `${rounded.toFixed(1)}h`;
};

export default function TemperatureTimeSeriesChart({
  series,
  timeframe,
  onTimeframeChange,
  loading,
}: TemperatureTimeSeriesChartProps) {
  // ** Theme **
  const { theme } = useTheme();
  const isDarkMode = theme === "dark";

  const hasData = series.some((s) => s.points.length > 0);

  const allTemps = series.flatMap((s) => s.points.map((p) => p.temperature));
  const minTemp = allTemps.length ? Math.min(...allTemps) : 0;
  const maxTemp = allTemps.length ? Math.max(...allTemps) : 0;
  const yDomain: [number, number] = [
    Math.floor(minTemp - 5),
    Math.ceil(maxTemp + 5),
  ];

  // ** Date Tick Format **
  const dateTickFormat = (value: number) => {
    const date = new Date(value);
    if (timeframe <= 60) return format(date, "HH:mm");
    if (timeframe <= 720) return format(date, "HH:mm");
    return format(date, "MMM d");
  };

  const timeSet = new Set<number>();
  series.forEach((s) => {
    s.points.forEach((p) => timeSet.add(p.time));
  });
  const sortedTimes = Array.from(timeSet).sort((a, b) => a - b);

  const chartData = sortedTimes.map((time) => {
    const row: { [key: string]: number | null } = { time };
    series.forEach((s) => {
      const point = s.points.find((p) => p.time === time);
      row[s.id] = point ? point.temperature : null;
    });
    return row;
  });

  const lightColors = ["#3b82f6", "#f59e0b", "#f97316", "#ef4444"];
  const darkColors = ["#38bdf8", "#facc15", "#fb923c", "#f97373"];
  const palette = isDarkMode ? darkColors : lightColors;

  const handleSliderChange = (e: ChangeEvent<HTMLInputElement>) => {
    const sliderValue = Number.parseInt(e.target.value, 10);
    const newTimeframe = sliderToTimeframe(sliderValue);
    onTimeframeChange(newTimeframe);
  };

  const chartContainerClass = isDarkMode
    ? "flex-1 w-full rounded-xl border overflow-hidden shadow-sm bg-gradient-to-b from-slate-900/80 via-slate-900/40 to-slate-900/0 border-slate-800"
    : "flex-1 w-full rounded-xl border overflow-hidden shadow-sm bg-gradient-to-b from-white via-slate-50 to-white border-gray-200";

  const CustomTooltip = ({
    active,
    payload,
    label,
  }: TooltipProps<number, string>) => {
    if (!active || !payload || !payload.length) return null;

    const date = new Date(label as number);

    return (
      <div
        className={`rounded-lg border px-3 py-2 shadow-lg text-xs ${
          isDarkMode
            ? "bg-slate-900/95 border-slate-700 text-slate-100"
            : "bg-white/95 border-slate-200 text-slate-900"
        }`}
      >
        <div className="font-medium mb-1">
          {format(date, "MMM d, yyyy HH:mm")}
        </div>
        <div className="space-y-0.5">
          {payload.map((item) => {
            const value = item.value as number | null;
            if (value == null || Number.isNaN(value)) return null;
            return (
              <div
                key={item.dataKey as string}
                className="flex items-center gap-2"
              >
                <span
                  className="inline-block w-2 h-2 rounded-full"
                  style={{ backgroundColor: item.color || "#6b7280" }}
                />
                <span className="text-[11px] opacity-80">{item.name}</span>
                <span
                  className={
                    isDarkMode
                      ? "text-sky-300 font-semibold ml-auto"
                      : "text-blue-600 font-semibold ml-auto"
                  }
                >
                  {value.toFixed(1)}°F
                </span>
              </div>
            );
          })}
        </div>
      </div>
    );
  };

  return (
    <div className="w-full h-[360px] sm:h-[420px] md:h-[480px] lg:h-[520px] flex flex-col">
      <div className="flex justify-between items-center mb-4">
        <span
          className={`text-sm ${
            isDarkMode ? "text-gray-300" : "text-gray-600"
          }`}
        >
          Timeframe: last {formatTimeframeLabel(timeframe)}
        </span>
        <input
          type="range"
          min={SLIDER_MIN}
          max={SLIDER_MAX}
          step={1}
          value={timeframeToSlider(timeframe)}
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
      ) : !hasData ? (
        <p
          className={`text-center ${
            isDarkMode ? "text-gray-400" : "text-gray-400"
          }`}
        >
          No readings available.
        </p>
      ) : (
        <div className={chartContainerClass}>
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart
              data={chartData}
              margin={{ top: 16, right: 24, left: 0, bottom: 8 }}
            >
              <defs>
                {series.map((s, index) => {
                  const colour =
                    s.colour?.trim() || palette[index % palette.length];
                  const gradientId = `tempGradient-${s.id}`;
                  return (
                    <linearGradient
                      key={gradientId}
                      id={gradientId}
                      x1="0"
                      y1="0"
                      x2="0"
                      y2="1"
                    >
                      <stop offset="0%" stopColor={colour} stopOpacity={0.3} />
                      <stop
                        offset="95%"
                        stopColor={isDarkMode ? "#0f172a" : "#ffffff"}
                        stopOpacity={0}
                      />
                    </linearGradient>
                  );
                })}
              </defs>

              <CartesianGrid
                stroke={
                  isDarkMode
                    ? "rgba(148, 163, 184, 0.15)"
                    : "rgba(148, 163, 184, 0.2)"
                }
                strokeDasharray="3 3"
                vertical={false}
              />

              <XAxis
                dataKey="time"
                type="number"
                domain={["dataMin", "dataMax"]}
                tickFormatter={dateTickFormat}
                tickMargin={8}
                minTickGap={40}
                stroke={isDarkMode ? "#9ca3af" : "#4b5563"}
                tick={{
                  fontSize: 11,
                  fill: isDarkMode ? "#e5e7eb" : "#111827",
                }}
                axisLine={{ stroke: isDarkMode ? "#4b5563" : "#d1d5db" }}
              />

              <YAxis
                domain={yDomain}
                tickMargin={8}
                stroke={isDarkMode ? "#9ca3af" : "#4b5563"}
                tick={{
                  fontSize: 11,
                  fill: isDarkMode ? "#e5e7eb" : "#111827",
                }}
                axisLine={{ stroke: isDarkMode ? "#4b5563" : "#d1d5db" }}
                width={40}
              />

              <RechartsTooltip
                content={<CustomTooltip />}
                cursor={{
                  stroke: isDarkMode ? "#38bdf8" : "#3b82f6",
                  strokeWidth: 1,
                  strokeDasharray: "3 3",
                }}
              />

              {series.map((s, index) => {
                const colour =
                  s.colour?.trim() || palette[index % palette.length];
                const gradientId = `tempGradient-${s.id}`;
                return (
                  <Area
                    key={s.id}
                    type="monotone"
                    dataKey={s.id}
                    name={s.name}
                    stroke={colour}
                    strokeWidth={2.4}
                    fill={`url(#${gradientId})`}
                    isAnimationActive
                    animationDuration={700}
                    dot={false}
                    activeDot={{
                      r: 5,
                      strokeWidth: 2,
                      stroke: isDarkMode ? "#0f172a" : "#ffffff",
                      fill: colour,
                    }}
                  />
                );
              })}
            </AreaChart>
          </ResponsiveContainer>
        </div>
      )}
    </div>
  );
}
