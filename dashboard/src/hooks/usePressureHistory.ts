import { useRef, useEffect } from "react";
import { useSystemStore } from "@/store/systemStore";

export interface PressurePoint {
  time: string;
  nodeA: number;
  nodeB: number;
  nodeC: number;
}

const MAX_POINTS = 60;

let pressureHistory: PressurePoint[] = [];

for (let i = MAX_POINTS; i >= 0; i--) {
  const t = new Date(Date.now() - i * 500);
  pressureHistory.push({
    time: t.toLocaleTimeString(),
    nodeA: 101325 + (Math.random() - 0.5) * 400,
    nodeB: 98500 + (Math.random() - 0.5) * 400,
    nodeC: 95800 + (Math.random() - 0.5) * 400,
  });
}

export function usePressureHistory() {
  const { nodeReadings } = useSystemStore();
  const historyRef = useRef<PressurePoint[]>([...pressureHistory]);

  useEffect(() => {
    if (nodeReadings.length === 3) {
      const newPoint: PressurePoint = {
        time: new Date().toLocaleTimeString(),
        nodeA: nodeReadings.find(n => n.nodeId === "A")?.pressure ?? 101325,
        nodeB: nodeReadings.find(n => n.nodeId === "B")?.pressure ?? 98500,
        nodeC: nodeReadings.find(n => n.nodeId === "C")?.pressure ?? 95800,
      };
      historyRef.current = [...historyRef.current.slice(-MAX_POINTS + 1), newPoint];
      pressureHistory = historyRef.current;
    }
  }, [nodeReadings]);

  return historyRef.current;
}
