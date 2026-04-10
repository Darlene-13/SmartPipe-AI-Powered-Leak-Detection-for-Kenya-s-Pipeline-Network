import { useEffect, useRef } from "react";
import { useSystemStore } from "@/store/systemStore";
import { useSettingsStore } from "@/store/settingsStore";
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

export function useLiveData() {
  const { liveChartUpdates } = useSettingsStore();
  const { setNodeReadings, setStatus, addAlert, setLatency, setRecommendation } = useSystemStore();
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const prevRef = useRef<Record<string, number>>({ A: 101325, B: 98500, C: 95800 });

  async function fetchAll() {
    try {
      const [statusRes, latencyRes, alertsRes] = await Promise.allSettled([
        api.get("/api/status/current"),
        api.get("/api/status/latency"),
        api.get("/api/alerts/recent"),
      ]);

      if (statusRes.status === "fulfilled") {
        const d = statusRes.value.data;

        const systemStatus = mapStatus(d.status ?? d.systemStatus ?? d.faultClass ?? "NORMAL");
        setStatus(systemStatus);

        if (d.recommendation ?? d.llmRecommendation ?? d.message) {
          setRecommendation(d.recommendation ?? d.llmRecommendation ?? d.message);
        }

        const nodes = d.sensorReadings ?? d.readings ?? d.nodes ?? [];
        if (Array.isArray(nodes) && nodes.length > 0) {
          const mapped = nodes.map((n: any) => {
            const id: "A" | "B" | "C" = n.nodeId ?? n.node_id ?? n.id ?? "A";
            const pressure = parseFloat(n.pressure ?? n.value ?? 101325);
            const trend = mapTrend(pressure, prevRef.current[id] ?? pressure);
            prevRef.current[id] = pressure;
            return {
              nodeId: id,
              nodeName: n.nodeName ?? n.node_name ?? `Node ${id}`,
              pressure,
              trend,
              timestamp: n.timestamp ?? new Date().toISOString(),
            };
          });
          setNodeReadings(mapped);
        } else {
          await fetchSensors();
        }
      } else {
        await fetchSensors();
      }

      if (latencyRes.status === "fulfilled") {
        const d = latencyRes.value.data;
        setLatency({
          total: parseFloat(d.total ?? d.totalLatency ?? d.end_to_end ?? 2.3),
          esp32: parseFloat(d.esp32 ?? d.esp ?? d.sensor ?? 0.4),
          ml: parseFloat(d.ml ?? d.mlLatency ?? d.inference ?? 1.1),
          llm: parseFloat(d.llm ?? d.llmLatency ?? d.reasoning ?? 0.8),
        });
      }

      if (alertsRes.status === "fulfilled") {
        const alerts = alertsRes.value.data;
        const list = Array.isArray(alerts) ? alerts : alerts?.alerts ?? alerts?.data ?? [];
        list.slice(0, 3).forEach((a: any) => {
          addAlert({
            id: a.id ?? a._id ?? String(Date.now() + Math.random()),
            faultClass: a.faultClass ?? a.fault_class ?? a.type ?? "UNKNOWN",
            severity: a.severity ?? "MEDIUM",
            confidence: parseFloat(a.confidence ?? a.score ?? 0.85),
            description: a.description ?? a.message ?? a.details ?? "Alert detected",
            timestamp: a.timestamp ?? a.createdAt ?? new Date().toISOString(),
          });
        });
      }
    } catch {
      // silently retry
    }
  }

  async function fetchSensors() {
    try {
      const { data } = await api.get("/api/sensors/readings/latest");
      const readings = Array.isArray(data) ? data : data?.readings ?? data?.data ?? [];
      if (readings.length > 0) {
        const mapped = readings.map((n: any) => {
          const id: "A" | "B" | "C" = n.nodeId ?? n.node_id ?? n.id ?? "A";
          const pressure = parseFloat(n.pressure ?? n.value ?? 101325);
          const trend = mapTrend(pressure, prevRef.current[id] ?? pressure);
          prevRef.current[id] = pressure;
          return {
            nodeId: id,
            nodeName: n.nodeName ?? n.node_name ?? `Node ${id}`,
            pressure,
            trend,
            timestamp: n.timestamp ?? new Date().toISOString(),
          };
        });
        setNodeReadings(mapped);
      }
    } catch {
      // ignore
    }
  }

  useEffect(() => {
    if (!liveChartUpdates) return;
    fetchAll();
    intervalRef.current = setInterval(fetchAll, 3000);
    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [liveChartUpdates]);
}
