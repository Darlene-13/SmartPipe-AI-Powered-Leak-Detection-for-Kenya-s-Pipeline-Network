import { useEffect, useRef } from "react";
import { useSystemStore } from "@/store/systemStore";
import { useAuthStore } from "@/store/authStore";
import { api } from "@/lib/api";

function mapStatus(raw: string): "NORMAL_OPERATION" | "LEAK_DETECTED" | "BLOCKAGE_DETECTED" | "OFFLINE" {
  if (!raw) return "OFFLINE";
  const s = raw.toUpperCase();
  if (s.includes("LEAK")) return "LEAK_DETECTED";
  if (s.includes("BLOCK")) return "BLOCKAGE_DETECTED";
  if (s.includes("NORMAL") || s.includes("OK") || s.includes("HEALTHY")) return "NORMAL_OPERATION";
  return "OFFLINE";
}

function mapTrend(val: number, prev: number): "stable" | "rising" | "falling" {
  const diff = val - prev;
  if (Math.abs(diff) < 50) return "stable";
  return diff > 0 ? "rising" : "falling";
}

function mapSensorRowToNodes(
    row: any,
    prevRef: React.MutableRefObject<Record<string, number>>
) {
  const nodes = [
    { id: "A" as const, name: "Node A (Upstream)",   pressure: parseFloat(row.nodeAPressure ?? row.node_a_pressure ?? 0) },
    { id: "B" as const, name: "Node B (Midstream)",  pressure: parseFloat(row.nodeBPressure ?? row.node_b_pressure ?? 0) },
    { id: "C" as const, name: "Node C (Downstream)", pressure: parseFloat(row.nodeCPressure ?? row.node_c_pressure ?? 0) },
  ];

  return nodes.map(({ id, name, pressure }) => {
    const trend = mapTrend(pressure, prevRef.current[id] ?? pressure);
    prevRef.current[id] = pressure;
    return {
      nodeId: id,
      nodeName: name,
      pressure,
      trend,
      timestamp: row.readingTime ?? row.timestamp ?? row.createdAt ?? new Date().toISOString(),
    };
  });
}

export function useLiveData() {
  const { liveUpdates, setNodeReadings, setStatus, addAlert, setLatency, setRecommendation } = useSystemStore();
  const { token, isAuthenticated } = useAuthStore();
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const prevRef = useRef<Record<string, number>>({ A: 101325, B: 98500, C: 95800 });

  async function fetchAll() {
    try {
      const [statusRes, latencyRes, alertsRes, sensorsRes] = await Promise.allSettled([
        api.get("/api/status/current"),
        api.get("/api/status/latency"),
        api.get("/api/alerts/recent"),
        api.get("/api/sensors/readings/latest", { params: { page: 0, size: 1 } }),
      ]);

      if (statusRes.status === "fulfilled") {
        const d = statusRes.value.data;
        setStatus(mapStatus(d.status ?? d.systemStatus ?? "NORMAL"));
        if (d.recommendation ?? d.description) {
          setRecommendation(d.recommendation ?? d.description);
        }
      }

      if (sensorsRes.status === "fulfilled") {
        const d = sensorsRes.value.data;
        const rows = d.content ?? d.readings ?? d.data ?? (Array.isArray(d) ? d : []);
        if (rows.length > 0) {
          const nodeReadings = mapSensorRowToNodes(rows[0], prevRef);
          setNodeReadings(nodeReadings);
        }
      }

      if (latencyRes.status === "fulfilled") {
        const d = latencyRes.value.data;
        setLatency({
          total: parseFloat(d.total ?? d.totalLatency ?? d.end_to_end ?? 2.3),
          esp32: parseFloat(d.esp32 ?? d.esp ?? d.sensor ?? 0.4),
          ml:    parseFloat(d.ml   ?? d.mlLatency  ?? d.inference ?? 1.1),
          llm:   parseFloat(d.llm  ?? d.llmLatency ?? d.reasoning ?? 0.8),
        });
      }

      if (alertsRes.status === "fulfilled") {
        const d = alertsRes.value.data;
        const list = d.content ?? (Array.isArray(d) ? d : d?.alerts ?? d?.data ?? []);
        list.slice(0, 5).forEach((a: any) => {
          addAlert({
            id:          String(a.id ?? a._id ?? `${a.createdAt ?? a.timestamp}-${a.faultClass}`),
            faultClass:  a.faultClass  ?? a.fault_class ?? "UNKNOWN",
            severity:    a.severityLevel ?? a.severity  ?? "LOW",
            confidence:  parseFloat(a.confidence ?? 0.85),
            description: a.recommendation ?? a.description ?? a.message ?? `${a.faultClass ?? "Fault"} detected`,
            timestamp:   a.createdAt ?? a.timestamp ?? new Date().toISOString(),
          });
        });
      }
    } catch {
    }
  }

  useEffect(() => {
    if (!liveUpdates || !isAuthenticated || !token) return;
    fetchAll();
    intervalRef.current = setInterval(fetchAll, 3000);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [liveUpdates, isAuthenticated, token]);
}