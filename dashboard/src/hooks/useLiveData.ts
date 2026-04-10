import { useEffect, useRef } from "react";
import { useSystemStore } from "@/store/systemStore";
import { useSettingsStore } from "@/store/settingsStore";

const SCENARIOS = {
  NORMAL_BASELINE: { pressureA: 101325, pressureB: 98500, pressureC: 95800, variance: 200 },
  LEAK_INCIPIENT: { pressureA: 101000, pressureB: 95000, pressureC: 91000, variance: 800 },
  LEAK_MODERATE: { pressureA: 100000, pressureB: 90000, pressureC: 82000, variance: 1500 },
  LEAK_CRITICAL: { pressureA: 98000, pressureB: 82000, pressureC: 70000, variance: 3000 },
  BLOCKAGE_25: { pressureA: 102000, pressureB: 99000, pressureC: 94000, variance: 400 },
  BLOCKAGE_50: { pressureA: 104000, pressureB: 100000, pressureC: 91000, variance: 600 },
  BLOCKAGE_75: { pressureA: 106000, pressureB: 101000, pressureC: 87000, variance: 1000 },
};

let currentScenario: keyof typeof SCENARIOS = "NORMAL_BASELINE";

export function setSimulationScenario(scenario: keyof typeof SCENARIOS) {
  currentScenario = scenario;
}

export function useLiveData() {
  const { liveChartUpdates } = useSettingsStore();
  const { setNodeReadings, setStatus, addAlert, setLatency, setRecommendation } = useSystemStore();
  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);
  const prevPressureRef = useRef<{ A: number; B: number; C: number }>({ A: 101325, B: 98500, C: 95800 });

  useEffect(() => {
    if (!liveChartUpdates) return;

    intervalRef.current = setInterval(() => {
      const scenario = SCENARIOS[currentScenario];
      const variance = () => (Math.random() - 0.5) * 2 * scenario.variance;

      const newA = scenario.pressureA + variance();
      const newB = scenario.pressureB + variance();
      const newC = scenario.pressureC + variance();

      const getTrend = (current: number, prev: number): "stable" | "rising" | "falling" => {
        const diff = current - prev;
        if (Math.abs(diff) < 50) return "stable";
        return diff > 0 ? "rising" : "falling";
      };

      const now = new Date().toISOString();
      setNodeReadings([
        { nodeId: "A", nodeName: "Node A (Upstream)", pressure: newA, trend: getTrend(newA, prevPressureRef.current.A), timestamp: now },
        { nodeId: "B", nodeName: "Node B (Midstream)", pressure: newB, trend: getTrend(newB, prevPressureRef.current.B), timestamp: now },
        { nodeId: "C", nodeName: "Node C (Downstream)", pressure: newC, trend: getTrend(newC, prevPressureRef.current.C), timestamp: now },
      ]);

      prevPressureRef.current = { A: newA, B: newB, C: newC };

      setLatency({
        esp32: 0.3 + Math.random() * 0.2,
        ml: 0.9 + Math.random() * 0.4,
        llm: 0.6 + Math.random() * 0.4,
        total: 1.8 + Math.random() * 1.0,
      });

      if (currentScenario === "NORMAL_BASELINE") {
        setStatus("NORMAL_OPERATION");
        setRecommendation("Pipeline operating normally. All pressure readings within acceptable range. No anomalies detected. Continue routine monitoring.");
      } else if (currentScenario.startsWith("LEAK")) {
        setStatus("LEAK_DETECTED");
        const severity = currentScenario === "LEAK_CRITICAL" ? "CRITICAL" : currentScenario === "LEAK_MODERATE" ? "HIGH" : "MEDIUM";
        setRecommendation(`Leak detected at midstream junction. Pressure drop of ${Math.round(101325 - newB)} Pa detected between nodes A and B. Recommend immediate inspection of section AB. Isolate affected segment and dispatch maintenance team.`);
        if (Math.random() < 0.05) {
          addAlert({
            id: Date.now().toString(),
            faultClass: currentScenario,
            severity,
            confidence: 0.85 + Math.random() * 0.14,
            description: `Pressure anomaly detected - ${currentScenario.replace(/_/g, " ")}`,
            timestamp: now,
          });
        }
      } else if (currentScenario.startsWith("BLOCKAGE")) {
        setStatus("BLOCKAGE_DETECTED");
        setRecommendation(`Blockage detected downstream. Pressure buildup of ${Math.round(newA - 101325)} Pa above normal. Recommend checking for debris or valve obstruction at downstream segment. Reduce input pressure to prevent pipe stress.`);
        if (Math.random() < 0.04) {
          addAlert({
            id: Date.now().toString(),
            faultClass: currentScenario,
            severity: "HIGH",
            confidence: 0.80 + Math.random() * 0.18,
            description: `Blockage detected - ${currentScenario.replace(/_/g, " ")}`,
            timestamp: now,
          });
        }
      }
    }, 500);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [liveChartUpdates, setNodeReadings, setStatus, addAlert, setLatency, setRecommendation]);
}
