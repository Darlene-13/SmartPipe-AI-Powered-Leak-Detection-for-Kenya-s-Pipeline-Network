"use client"

import { useMemo } from "react"
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  Legend,
} from "recharts"

interface PressureDataPoint {
  time: string
  nodeA: number
  nodeB: number
  nodeC: number
}

interface PressureChartProps {
  data: PressureDataPoint[]
}

const NODE_COLORS = {
  nodeA: "#22c55e",
  nodeB: "#3b82f6",
  nodeC: "#eab308",
}

export function PressureChart({ data }: PressureChartProps) {
  const chartData = useMemo(() => data.slice(-60), [data])

  return (
    <div className="rounded-lg border border-border bg-card p-4">
      <div className="flex items-center justify-between mb-4">
        <span className="font-mono text-xs tracking-widest text-muted-foreground">PRESSURE_TRACE</span>
        <div className="flex items-center gap-4 font-mono text-[10px]">
          <span className="flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full" style={{ backgroundColor: NODE_COLORS.nodeA }} />
            <span className="text-muted-foreground">NODE_A</span>
          </span>
          <span className="flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full" style={{ backgroundColor: NODE_COLORS.nodeB }} />
            <span className="text-muted-foreground">NODE_B</span>
          </span>
          <span className="flex items-center gap-1.5">
            <span className="h-2 w-2 rounded-full" style={{ backgroundColor: NODE_COLORS.nodeC }} />
            <span className="text-muted-foreground">NODE_C</span>
          </span>
        </div>
      </div>
      <ResponsiveContainer width="100%" height={260}>
        <LineChart data={chartData}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
          <XAxis
            dataKey="time"
            stroke="rgba(255,255,255,0.15)"
            tick={{ fontSize: 9, fill: "rgba(255,255,255,0.3)", fontFamily: "var(--font-mono)" }}
            interval="preserveStartEnd"
          />
          <YAxis
            stroke="rgba(255,255,255,0.15)"
            tick={{ fontSize: 9, fill: "rgba(255,255,255,0.3)", fontFamily: "var(--font-mono)" }}
            tickFormatter={(v: number) => `${(v / 1000).toFixed(0)}k`}
            domain={["auto", "auto"]}
          />
          <Tooltip
            contentStyle={{
              backgroundColor: "rgba(15,15,25,0.95)",
              border: "1px solid rgba(255,255,255,0.1)",
              borderRadius: "6px",
              fontSize: 11,
              fontFamily: "monospace",
              color: "rgba(255,255,255,0.85)",
            }}
            formatter={(value: number, name: string) => [
              `${value.toLocaleString()} Pa`,
              name === "nodeA" ? "NODE_A" : name === "nodeB" ? "NODE_B" : "NODE_C",
            ]}
          />
          <Legend content={() => null} />
          <Line type="monotone" dataKey="nodeA" stroke={NODE_COLORS.nodeA} strokeWidth={1.5} dot={false} isAnimationActive={false} />
          <Line type="monotone" dataKey="nodeB" stroke={NODE_COLORS.nodeB} strokeWidth={1.5} dot={false} isAnimationActive={false} />
          <Line type="monotone" dataKey="nodeC" stroke={NODE_COLORS.nodeC} strokeWidth={1.5} dot={false} isAnimationActive={false} />
        </LineChart>
      </ResponsiveContainer>
    </div>
  )
}
