import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";

export interface PressurePoint {
  time: string;
  nodeA: number;
  nodeB: number;
  nodeC: number;
}

const MAX_POINTS = 60;

export function usePressureHistory() {
  const [history, setHistory] = useState<PressurePoint[]>([]);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  async function fetchHistory() {
    try {
      const { data } = await api.get("/api/sensors/readings/history");
      const raw = Array.isArray(data) ? data : data?.history ?? data?.readings ?? data?.data ?? [];

      if (raw.length === 0) return;

      const grouped: Record<string, { A: number; B: number; C: number }> = {};

      raw.forEach((r: any) => {
        const ts = r.timestamp ?? r.createdAt ?? new Date().toISOString();
        const time = new Date(ts).toLocaleTimeString();
        const id: string = (r.nodeId ?? r.node_id ?? r.id ?? "A").toString().toUpperCase();
        const pressure = parseFloat(r.pressure ?? r.value ?? 0);
        if (!grouped[time]) grouped[time] = { A: 0, B: 0, C: 0 };
        if (id === "A" || id.includes("A")) grouped[time].A = pressure;
        else if (id === "B" || id.includes("B")) grouped[time].B = pressure;
        else if (id === "C" || id.includes("C")) grouped[time].C = pressure;
      });

      const points: PressurePoint[] = Object.entries(grouped)
          .slice(-MAX_POINTS)
          .map(([time, vals]) => ({ time, nodeA: vals.A, nodeB: vals.B, nodeC: vals.C }));

      if (points.length > 0) setHistory(points);
    } catch {
      // ignore
    }
  }

  useEffect(() => {
    fetchHistory();
    intervalRef.current = setInterval(fetchHistory, 3000);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, []);

  return history;
}
