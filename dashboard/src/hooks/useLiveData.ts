// @ts-ignore
import { useEffect, useRef } from "react";
import { useSystemStore } from "@/store/systemStore";
import { useSettingsStore } from "@/store/settingsStore";
import { api, BASE_URL } from "@/lib/api";

function mapStatus(raw: string): "NORMAL_OPERATION" | "LEAK_DETECTED" | "BLOCKAGE_DETECTED" | "OFFLINE" {
  if (!raw) return "OFFLINE";
  const s = raw.toUpperCase();
  if (s.includes("LEAK"))   return "LEAK_DETECTED";
  if (s.includes("BLOCK"))  return "BLOCKAGE_DETECTED";
  if (s.includes("NORMAL") || s.includes("OK") || s.includes("HEALTHY")) return "NORMAL_OPERATION";
  return "OFFLINE";
}

function mapTrend(val: number, prev: number): "stable" | "rising" | "falling" {
  const diff = val - prev;
  if (Math.abs(diff) < 50) return "stable";
  return diff > 0 ? "rising" : "falling";
}

// @ts-ignore
function mapSensorRow(row: any, prevRef: React.MutableRefObject<Record<string, number>>) {
  const nodes = [
    { id: "A" as const, name: "Node A (Upstream)",   pressure: parseFloat(row.nodeAPressure ?? row.node_a_pressure ?? 0), velocity: parseFloat(row.velocityA ?? row.velocity_a ?? 0) },
    { id: "B" as const, name: "Node B (Midstream)",  pressure: parseFloat(row.nodeBPressure ?? row.node_b_pressure ?? 0), velocity: parseFloat(row.velocityB ?? row.velocity_b ?? 0) },
    { id: "C" as const, name: "Node C (Downstream)", pressure: parseFloat(row.nodeCPressure ?? row.node_c_pressure ?? 0), velocity: parseFloat(row.velocityC ?? row.velocity_c ?? 0) },
  ];
  return nodes.map(({ id, name, pressure, velocity }) => {
    const trend = mapTrend(pressure, prevRef.current[id] ?? pressure);
    prevRef.current[id] = pressure;
    return {
      nodeId: id,
      nodeName: name,
      pressure,
      velocity,
      trend,
      timestamp: row.readingTime ?? row.timestamp ?? new Date().toISOString(),
    };
  });
}

function toWsUrl(baseUrl: string): string {
  return baseUrl.replace(/^https:\/\//, "wss://").replace(/^http:\/\//, "ws://");
}

export function useLiveData() {
  const { liveChartUpdates } = useSettingsStore();
  const { setNodeReadings, setStatus, setAlerts, setLatency, setRecommendation } = useSystemStore();
  const intervalRef    = useRef<ReturnType<typeof setInterval> | null>(null);
  const prevRef        = useRef<Record<string, number>>({ A: 101325, B: 98500, C: 95800 });
  const lastAlertId    = useRef<string | null>(null);
  const wsRef          = useRef<WebSocket | null>(null);
  const wsReconnectRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // ── WebSocket: receive live fault alerts AND clear events ─────────────────
  function connectWebSocket() {
    if (wsRef.current?.readyState === WebSocket.OPEN) return;

    const wsUrl = `${toWsUrl(BASE_URL)}/ws/alerts`;
    console.log("[WS] Connecting to", wsUrl);

    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen  = () => console.log("[WS] Connected");

    ws.onmessage = (event) => {
      try {
        const msg = JSON.parse(event.data);

        // ── CLEAR event: backend broadcast faultClass=NORMAL when readings normalise
        // This resets the banner immediately via WebSocket without waiting for REST poll
        if (msg.faultClass === "NORMAL" || msg.faultClass === "normal") {
          setStatus("NORMAL_OPERATION");
          if (msg.recommendation) setRecommendation(msg.recommendation);
          console.log("[WS] CLEAR received — banner reset to NORMAL");
          return;
        }

        // ── FAULT alert: deduplicate by id then push to alert feed
        const alertId = String(msg.id ?? "");
        if (alertId && alertId === lastAlertId.current) return;
        lastAlertId.current = alertId;

        const { alerts } = useSystemStore.getState();
        const newAlert = {
          id:          String(msg.id ?? Date.now()),
          faultClass:  msg.faultClass   ?? msg.fault_class ?? "UNKNOWN",
          severity:    msg.severityLevel ?? msg.severity   ?? "LOW",
          confidence:  parseFloat(msg.confidence ?? 0.85),
          description: msg.recommendation ?? msg.description ?? msg.message
              ?? `${msg.faultClass ?? "Fault"} detected`,
          timestamp:   msg.createdAt ?? msg.timestamp ?? new Date().toISOString(),
        };

        setAlerts([newAlert, ...alerts].slice(0, 20));

        // Drive status banner from fault WS message
        setStatus(mapStatus(msg.faultClass ?? ""));
        if (msg.recommendation) setRecommendation(msg.recommendation);

      } catch (e) {
        console.warn("[WS] Failed to parse message", e);
      }
    };

    ws.onclose = () => {
      console.warn("[WS] Disconnected — reconnecting in 5s");
      wsReconnectRef.current = setTimeout(connectWebSocket, 5000);
    };

    ws.onerror = (err) => {
      console.error("[WS] Error", err);
      ws.close();
    };
  }

  // ── REST polling: node pressures, latency, status (3s fallback) ──────────
  async function fetchAll() {
    try {
      const [statusRes, latencyRes, sensorsRes] = await Promise.allSettled([
        api.get("/api/status/current"),
        api.get("/api/status/latency"),
        api.get("/api/sensors/readings/latest", { params: { page: 0, size: 1, sort: "readingTime,desc" } }),
      ]);

      if (statusRes.status === "fulfilled") {
        const d = statusRes.value.data;
        setStatus(mapStatus(d.status ?? d.systemStatus ?? "NORMAL"));
        if (d.recommendation ?? d.description)
          setRecommendation(d.recommendation ?? d.description);
      }

      if (sensorsRes.status === "fulfilled") {
        const d    = sensorsRes.value.data;
        const rows = d.content ?? d.readings ?? d.data ?? (Array.isArray(d) ? d : []);
        if (rows.length > 0) setNodeReadings(mapSensorRow(rows[0], prevRef));
      }

      if (latencyRes.status === "fulfilled") {
        const d = latencyRes.value.data;
        setLatency({
          total: parseFloat(d.total      ?? d.totalLatency  ?? 2.3),
          esp32: parseFloat(d.esp32      ?? d.sensor        ?? 0.4),
          ml:    parseFloat(d.ml         ?? d.mlLatency     ?? 1.1),
          llm:   parseFloat(d.llm        ?? d.llmLatency    ?? 0.8),
        });
      }
    } catch { /* silently retry */ }
  }

  useEffect(() => {
    if (!liveChartUpdates) return;

    connectWebSocket();
    fetchAll();
    intervalRef.current = setInterval(fetchAll, 3000);

    return () => {
      if (intervalRef.current)    clearInterval(intervalRef.current);
      if (wsReconnectRef.current) clearTimeout(wsReconnectRef.current);
      if (wsRef.current) {
        wsRef.current.onclose = null;
        wsRef.current.close();
      }
    };
  }, [liveChartUpdates]);
}