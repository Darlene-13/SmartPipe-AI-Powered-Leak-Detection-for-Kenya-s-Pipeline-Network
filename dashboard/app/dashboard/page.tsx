"use client"

import { useState, useEffect, useCallback, useRef } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { DashboardHeader } from "@/components/dashboard-header"
import { NodeStatusCard } from "@/components/node-status-card"
import { PressureChart } from "@/components/pressure-chart"
import { SystemStatusCard } from "@/components/system-status"
import { FaultDetectionCard } from "@/components/fault-detection"
import { AIRecommendationCard } from "@/components/ai-recommendation"
import { AlertsTable } from "@/components/alerts-table"
import { LatencyDisplay } from "@/components/latency-display"
import {
  generateReadings,
  getFaultDetection,
  getSystemStatus,
  getAIRecommendation,
  generateAlert,
  getLatencyStats,
  type SensorReading,
  type FaultDetection,
  type SystemStatus,
  type AIRecommendation,
  type Alert,
  type LatencyStats,
} from "@/lib/simulator"

interface PressureDataPoint {
  time: string
  nodeA: number
  nodeB: number
  nodeC: number
}

function DashboardContent() {
  const { isAuthenticated } = useAuth()
  const router = useRouter()
  const [readings, setReadings] = useState<SensorReading[]>([])
  const [pressureHistory, setPressureHistory] = useState<PressureDataPoint[]>([])
  const [fault, setFault] = useState<FaultDetection | null>(null)
  const [status, setStatus] = useState<SystemStatus | null>(null)
  const [recommendation, setRecommendation] = useState<AIRecommendation | null>(null)
  const [alerts, setAlerts] = useState<Alert[]>([])
  const [latency, setLatency] = useState<LatencyStats | null>(null)
  const [tickCount, setTickCount] = useState(0)
  const prevClassRef = useRef<string>("NORMAL")

  useEffect(() => {
    if (!isAuthenticated) {
      router.replace("/")
    }
  }, [isAuthenticated, router])

  const tick = useCallback(() => {
    const newReadings = generateReadings()
    const newFault = getFaultDetection(newReadings)
    const newStatus = getSystemStatus(newReadings)
    const newRec = getAIRecommendation(newFault)
    const stats = getLatencyStats()

    setReadings(newReadings)
    setFault(newFault)
    setStatus(newStatus)
    setRecommendation(newRec)
    setLatency(stats)
    setTickCount((c) => c + 1)

    setPressureHistory((prev) => {
      const point: PressureDataPoint = {
        time: new Date().toLocaleTimeString("en-GB", { hour12: false }),
        nodeA: newReadings.find((r) => r.nodeId === "A")?.pressure ?? 0,
        nodeB: newReadings.find((r) => r.nodeId === "B")?.pressure ?? 0,
        nodeC: newReadings.find((r) => r.nodeId === "C")?.pressure ?? 0,
      }
      const next = [...prev, point]
      return next.length > 120 ? next.slice(-120) : next
    })

    if (newFault.classification !== prevClassRef.current) {
      const alert = generateAlert(newFault)
      setAlerts((prev) => [alert, ...prev].slice(0, 50))
    }
    prevClassRef.current = newFault.classification
  }, [])

  useEffect(() => {
    tick()
    const id = setInterval(tick, 500)
    return () => clearInterval(id)
  }, [tick])

  if (!isAuthenticated || !readings.length || !fault || !status || !recommendation) return null

  const isCritical = status.level === "LEAK_DETECTED"
  const isBlockage = status.level === "BLOCKAGE_DETECTED"

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <main className="max-w-[1600px] mx-auto px-4 lg:px-6 py-5 flex flex-col gap-5">

        {/* Critical alert banner */}
        {(isCritical || isBlockage) && (
          <div className={`rounded-lg border px-4 py-3 flex items-center gap-3 font-mono text-xs tracking-wider ${
            isCritical
              ? "border-destructive/40 bg-destructive/10 text-destructive"
              : "border-warning/40 bg-warning/10 text-warning"
          }`}>
            <div className={`h-2.5 w-2.5 rounded-full animate-pulse ${isCritical ? "bg-destructive" : "bg-warning"}`} />
            <span className="font-bold">{status.level}</span>
            <span className="text-muted-foreground/60 mx-1">|</span>
            <span>{status.description}</span>
          </div>
        )}

        {/* Top row: summary bar */}
        <div className="rounded-lg border border-border bg-card p-3 flex flex-wrap items-center gap-x-6 gap-y-2 font-mono text-xs">
          <span className="text-muted-foreground/60 tracking-widest">SYSTEM_SUMMARY</span>
          <span className="text-muted-foreground/40">|</span>
          <span className="text-muted-foreground">Samples: <span className="text-foreground tabular-nums">{tickCount}</span></span>
          <span className="text-muted-foreground">Nodes: <span className="text-primary font-bold">3 ONLINE</span></span>
          <span className="text-muted-foreground">Pump: <span className="text-foreground tabular-nums">{status.pumpRpm} RPM</span></span>
          <span className="text-muted-foreground">Slurry SG: <span className="text-foreground tabular-nums">{status.slurryDensity}</span></span>
          <span className="text-muted-foreground">Uptime: <span className="text-primary tabular-nums">{status.uptime.toFixed(1)}%</span></span>
        </div>

        {/* Node status cards */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {readings.map((r) => (
            <NodeStatusCard
              key={r.nodeId}
              reading={r}
              isFault={fault.affectedNode === r.nodeId && fault.classification !== "NORMAL"}
              isWarning={
                fault.classification !== "NORMAL" &&
                fault.affectedNode !== r.nodeId &&
                r.dpdt < -30
              }
            />
          ))}
        </div>

        {/* Live pressure trace */}
        <PressureChart data={pressureHistory} />

        {/* System status + ML classifier side by side */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <SystemStatusCard status={status} />
          <FaultDetectionCard fault={fault} />
        </div>

        {/* AI Advisory */}
        <AIRecommendationCard recommendation={recommendation} />

        {/* Alerts log */}
        <AlertsTable alerts={alerts} maxRows={10} />

        {/* Latency footer bar */}
        {latency && (
          <div className="rounded-lg border border-border bg-card/50 p-3">
            <LatencyDisplay stats={latency} inline />
          </div>
        )}
      </main>
    </div>
  )
}

export default function DashboardPage() {
  return <DashboardContent />
}
