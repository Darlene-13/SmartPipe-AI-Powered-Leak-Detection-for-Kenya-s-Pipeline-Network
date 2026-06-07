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

function mapSensorRow(row: any, prevRef: React.MutableRefObject<Record<string, number>>) {
  const nodes = [
    { id: "A" as const, name: "Node A (Upstream)",   pressure: parseFloat(row.nodeAPressure ?? row.node_a_pressure ?? 0) },
    { id: "B" as const, name: "Node B (Midstream)",  pressure: parseFloat(row.nodeBPressure ?? row.node_b_pressure ?? 0) },
    { id: "C" as const, name: "Node C (Downstream)", pressure: parseFloat(row.nodeCPressure ?? row.node_c_pressure ?? 0) },
  ];
  return nodes.map(({ id, name, pressure }) => {
    const trend = mapTrend(pressure, prevRef.current[id] ?? pressure);
    prevRef.current[id] = pressure;
    return { nodeId: id, nodeName: name, pressure, trend,
      timestamp: row.readingTime ?? row.timestamp ?? new Date().toISOString() };
  });
}

// Convert https://... to wss://... (or http to ws)
function toWsUrl(baseUrl: string): string {
  return baseUrl.replace(/^https:\/\//, "wss://").replace(/^http:\/\//, "ws://");
}

export function useLiveData() {
  const { liveChartUpdates } = useSettingsStore();
  const { setNodeReadings, setStatus, setAlerts, setLatency, setRecommendation } = useSystemStore();
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const prevRef     = useRef<Record<string, number>>({ A: 101325, B: 98500, C: 95800 });
  const lastAlertId = useRef<string | null>(null);
  const wsRef       = useRef<WebSocket | null>(null);
  const wsReconnectRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // ── WebSocket: receive live fault alerts ──────────────────────────────────
  function connectWebSocket() {
    if (wsRef.current?.readyState === WebSocket.OPEN) return;

    const wsUrl = `${toWsUrl(BASE_URL)}/ws/alerts`;
    console.log("[WS] Connecting to", wsUrl);

    const ws = new WebSocket(wsUrl);
    wsRef.current = ws;

    ws.onopen = () => {
      console.log("[WS] Connected");
    };

    ws.onmessage = (event) => {
      try {
        const alert = JSON.parse(event.data);
        const alertId = String(alert.id ?? "");

        if (alertId && alertId === lastAlertId.current) return;
        lastAlertId.current = alertId;

        // Push new alert to the top of the store list
        const { alerts } = useSystemStore.getState();
        const newAlert = {
          id:          String(alert.id ?? Date.now()),
          faultClass:  alert.faultClass   ?? alert.fault_class ?? "UNKNOWN",
          severity:    alert.severityLevel ?? alert.severity   ?? "LOW",
          confidence:  parseFloat(alert.confidence ?? 0.85),
          description: alert.recommendation ?? alert.description ?? alert.message
              ?? `${alert.faultClass ?? "Fault"} detected`,
          timestamp:   alert.createdAt ?? alert.timestamp ?? new Date().toISOString(),
        };

        setAlerts([newAlert, ...alerts].slice(0, 20));

        // NOTE: Do NOT call setStatus here. The REST poll (fetchAll every 3s) is the
        // sole source of truth for the status banner. Setting status from the WS alert
        // locks the banner on LEAK permanently because no "all-clear" WS message is
        // ever sent when readings return to NORMAL.
        if (alert.recommendation) {
          setRecommendation(alert.recommendation);
        }
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

  // ── REST polling: node pressures, latency, status ─────────────────────────
  async function fetchAll() {
    try {
      const [statusRes, latencyRes, sensorsRes] = await Promise.allSettled([
        api.get("/api/status/current"),
        api.get("/api/status/latency"),
        api.get("/api/sensors/readings/latest", { params: { page: 0, size: 1 } }),
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

    // Start WebSocket for live alerts
    connectWebSocket();

    // Poll REST for sensor readings + latency (still needed for node cards + chart)
    fetchAll();
    intervalRef.current = setInterval(fetchAll, 3000);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
      if (wsReconnectRef.current) clearTimeout(wsReconnectRef.current);
      if (wsRef.current) {
        wsRef.current.onclose = null; // prevent reconnect loop on unmount
        wsRef.current.close();
      }
    };
  }, [liveChartUpdates]);
}