import { create } from "zustand";

export type SystemStatus = "NORMAL_OPERATION" | "LEAK_DETECTED" | "BLOCKAGE_DETECTED" | "OFFLINE";

export interface Alert {
  id: string;
  faultClass: string;
  severity: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
  confidence: number;
  description: string;
  timestamp: string;
}

export interface NodeReading {
  nodeId: "A" | "B" | "C";
  nodeName: string;
  pressure: number;
  trend: "stable" | "rising" | "falling";
  timestamp: string;
}

interface SystemStore {
  status: SystemStatus;
  alerts: Alert[];
  nodeReadings: NodeReading[];
  latency: { total: number; esp32: number; ml: number; llm: number };
  recommendation: string;
  liveUpdates: boolean;
  setStatus: (status: SystemStatus) => void;
  addAlert: (alert: Alert) => void;
  setNodeReadings: (readings: NodeReading[]) => void;
  setLatency: (latency: { total: number; esp32: number; ml: number; llm: number }) => void;
  setRecommendation: (rec: string) => void;
  setLiveUpdates: (val: boolean) => void;
}

export const useSystemStore = create<SystemStore>((set) => ({
  status: "NORMAL_OPERATION",
  alerts: [],
  nodeReadings: [
    { nodeId: "A", nodeName: "Node A (Upstream)", pressure: 101325, trend: "stable", timestamp: new Date().toISOString() },
    { nodeId: "B", nodeName: "Node B (Midstream)", pressure: 98500, trend: "stable", timestamp: new Date().toISOString() },
    { nodeId: "C", nodeName: "Node C (Downstream)", pressure: 95800, trend: "stable", timestamp: new Date().toISOString() },
  ],
  latency: { total: 2.3, esp32: 0.4, ml: 1.1, llm: 0.8 },
  recommendation: "Pipeline operating normally. All pressure readings within acceptable range. No anomalies detected.",
  liveUpdates: true,
  setStatus: (status) => set({ status }),
  addAlert: (alert) => set((state) => ({ alerts: [alert, ...state.alerts].slice(0, 50) })),
  setNodeReadings: (nodeReadings) => set({ nodeReadings }),
  setLatency: (latency) => set({ latency }),
  setRecommendation: (recommendation) => set({ recommendation }),
  setLiveUpdates: (liveUpdates) => set({ liveUpdates }),
}));
