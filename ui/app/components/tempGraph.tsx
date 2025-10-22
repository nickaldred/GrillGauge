interface TempGraphProps {
  data: Array<{ time: string; temperature: number }>;
  probeId: number;
  probeName: string;
}

export function TempGraph({
  data,
  probeId,
  probeName,
}: Readonly<TempGraphProps>) {
  return <div className="w-full"></div>;
}
