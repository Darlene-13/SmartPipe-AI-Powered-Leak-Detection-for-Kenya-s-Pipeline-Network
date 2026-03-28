"use client"

import { useState, useMemo, useEffect } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { DashboardHeader } from "@/components/dashboard-header"
import { AlertsTable } from "@/components/alerts-table"
import { generateHistoricalData, type NodeId } from "@/lib/simulator"
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts"

const PIE_COLORS = ["#22c55e", "#ef4444", "#eab308"]

function HistoryContent() {
  const { isAuthenticated } = useAuth()
  const router = useRouter()

  const [fromDate, setFromDate] = useState(() => {
    const d = new Date()
    d.setMonth(d.getMonth() - 1)
    return d.toISOString().split("T")[0]
  })
  const [toDate, setToDate] = useState(() => new Date().toISOString().split("T")[0])

  useEffect(() => {
    if (!isAuthenticated) router.replace("/")
  }, [isAuthenticated, router])

  const data = useMemo(() => {
    return generateHistoricalData(new Date(fromDate), new Date(toDate))
  }, [fromDate, toDate])

  const pieData = useMemo(() => {
    return [
      { name: "NORMAL", value: data.faultCounts.NORMAL },
      { name: "LEAK", value: data.faultCounts.LEAK },
      { name: "BLOCKAGE", value: data.faultCounts.BLOCKAGE },
    ]
  }, [data.faultCounts])

  const pressureOverTime = useMemo(() => {
    const grouped: Record<string, { time: string; nodeA: number; nodeB: number; nodeC: number }> = {}
    for (const r of data.readings) {
      const key = r.timestamp
      if (!grouped[key]) {
        grouped[key] = { time: new Date(key).toLocaleDateString(), nodeA: 0, nodeB: 0, nodeC: 0 }
      }
      const nodeKey = `node${r.nodeId}` as `node${NodeId}`
      grouped[key][nodeKey] = r.pressure
    }
    return Object.values(grouped)
  }, [data.readings])

  if (!isAuthenticated) return null

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <main className="max-w-[1600px] mx-auto px-4 lg:px-6 py-5 flex flex-col gap-5">

        {/* Page header */}
        <div className="rounded-lg border border-border bg-card p-3 flex flex-wrap items-center gap-x-6 gap-y-2 font-mono text-xs">
          <span className="text-muted-foreground/60 tracking-widest">HISTORY_LOG</span>
          <span className="text-muted-foreground/40">|</span>
          <span className="text-muted-foreground">Records: <span className="text-foreground tabular-nums">{data.readings.length}</span></span>
          <span className="text-muted-foreground">Alerts: <span className="text-foreground tabular-nums">{data.alerts.length}</span></span>
        </div>

        {/* Date filter */}
        <div className="rounded-lg border border-border bg-card p-4 flex flex-wrap items-end gap-4">
          <div>
            <label htmlFor="fromDate" className="block font-mono text-[10px] text-muted-foreground/60 mb-1.5 tracking-widest uppercase">
              From
            </label>
            <input
              id="fromDate"
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="h-9 px-3 rounded-md border border-border bg-secondary/80 text-foreground font-mono text-xs focus:outline-none focus:ring-2 focus:ring-primary/50 transition-colors"
            />
          </div>
          <div>
            <label htmlFor="toDate" className="block font-mono text-[10px] text-muted-foreground/60 mb-1.5 tracking-widest uppercase">
              To
            </label>
            <input
              id="toDate"
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="h-9 px-3 rounded-md border border-border bg-secondary/80 text-foreground font-mono text-xs focus:outline-none focus:ring-2 focus:ring-primary/50 transition-colors"
            />
          </div>
        </div>

        {/* Charts row */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
          {/* Fault Distribution Pie */}
          <div className="rounded-lg border border-border bg-card p-4">
            <span className="font-mono text-xs tracking-widest text-muted-foreground block mb-4">FAULT_DISTRIBUTION</span>
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie
                  data={pieData}
                  cx="50%"
                  cy="50%"
                  innerRadius={55}
                  outerRadius={85}
                  dataKey="value"
                  labelLine={false}
                  label={({ name, value }: { name: string; value: number }) => `${name}: ${value}`}
                  style={{ fontFamily: "monospace", fontSize: 10 }}
                >
                  {pieData.map((_, index) => (
                    <Cell key={`cell-${index}`} fill={PIE_COLORS[index]} />
                  ))}
                </Pie>
                <Legend wrapperStyle={{ fontSize: 10, fontFamily: "monospace" }} />
                <Tooltip
                  contentStyle={{
                    backgroundColor: "rgba(15,15,25,0.95)",
                    border: "1px solid rgba(255,255,255,0.1)",
                    borderRadius: "6px",
                    fontSize: 11,
                    fontFamily: "monospace",
                    color: "rgba(255,255,255,0.85)",
                  }}
                />
              </PieChart>
            </ResponsiveContainer>
          </div>

          {/* Summary stats */}
          <div className="rounded-lg border border-border bg-card p-4">
            <span className="font-mono text-xs tracking-widest text-muted-foreground block mb-4">PERIOD_STATISTICS</span>
            <div className="grid grid-cols-2 gap-4 font-mono">
              <div className="rounded-md border border-border bg-secondary/30 p-3">
                <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Normal Readings</span>
                <span className="text-xl font-bold text-primary">{data.faultCounts.NORMAL}</span>
              </div>
              <div className="rounded-md border border-border bg-secondary/30 p-3">
                <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Leak Events</span>
                <span className="text-xl font-bold text-destructive">{data.faultCounts.LEAK}</span>
              </div>
              <div className="rounded-md border border-border bg-secondary/30 p-3">
                <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Blockage Events</span>
                <span className="text-xl font-bold text-warning">{data.faultCounts.BLOCKAGE}</span>
              </div>
              <div className="rounded-md border border-border bg-secondary/30 p-3">
                <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Total Alerts</span>
                <span className="text-xl font-bold text-foreground">{data.alerts.length}</span>
              </div>
            </div>
          </div>
        </div>

        {/* Pressure History */}
        <div className="rounded-lg border border-border bg-card p-4">
          <span className="font-mono text-xs tracking-widest text-muted-foreground block mb-4">PRESSURE_HISTORY</span>
          <ResponsiveContainer width="100%" height={280}>
            <LineChart data={pressureOverTime}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.05)" />
              <XAxis
                dataKey="time"
                stroke="rgba(255,255,255,0.15)"
                tick={{ fontSize: 9, fill: "rgba(255,255,255,0.3)", fontFamily: "monospace" }}
                interval="preserveStartEnd"
              />
              <YAxis
                stroke="rgba(255,255,255,0.15)"
                tick={{ fontSize: 9, fill: "rgba(255,255,255,0.3)", fontFamily: "monospace" }}
                tickFormatter={(v: number) => `${(v / 1000).toFixed(0)}k`}
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
              <Line type="monotone" dataKey="nodeA" stroke="#22c55e" strokeWidth={1.5} dot={false} />
              <Line type="monotone" dataKey="nodeB" stroke="#3b82f6" strokeWidth={1.5} dot={false} />
              <Line type="monotone" dataKey="nodeC" stroke="#eab308" strokeWidth={1.5} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Alerts table */}
        <AlertsTable alerts={data.alerts} title="ALERT_ARCHIVE" />
      </main>
    </div>
  )
}

export default function HistoryPage() {
  return <HistoryContent />
}
