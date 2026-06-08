import { useEffect, useRef, useState } from "react";
import { api } from "@/lib/api";

export interface PressurePoint {
  time:  string;
  nodeA: number;
  nodeB: number;
  nodeC: number;
  velA:  number;
  velB:  number;
  velC:  number;
}

const MAX_POINTS = 60;

export function usePressureHistory() {
  const [history, setHistory] = useState<PressurePoint[]>([]);
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  async function fetchHistory() {
    try {
      const { data } = await api.get("/api/sensors/readings/latest", {
        params: { page: 0, size: MAX_POINTS, sort: "readingTime,desc" },
      });

      const rows: any[] = data.content ?? data.readings ?? data.data ?? (Array.isArray(data) ? data : []);
      if (rows.length === 0) return;

      // Reverse so oldest→newest left→right on the chart
      const points: PressurePoint[] = [...rows].reverse().map((r: any) => ({
        time: new Date(r.readingTime ?? r.timestamp ?? r.createdAt ?? Date.now())
            .toLocaleTimeString([], { hour: "2-digit", minute: "2-digit", second: "2-digit" }),
        nodeA: parseFloat(r.nodeAPressure ?? r.node_a_pressure ?? 0),
        nodeB: parseFloat(r.nodeBPressure ?? r.node_b_pressure ?? 0),
        nodeC: parseFloat(r.nodeCPressure ?? r.node_c_pressure ?? 0),
        velA:  parseFloat(r.velocityA     ?? r.velocity_a      ?? 0),
        velB:  parseFloat(r.velocityB     ?? r.velocity_b      ?? 0),
        velC:  parseFloat(r.velocityC     ?? r.velocity_c      ?? 0),
      }));

      if (points.length > 0) setHistory(points);
    } catch {
      // silently retry on next interval
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