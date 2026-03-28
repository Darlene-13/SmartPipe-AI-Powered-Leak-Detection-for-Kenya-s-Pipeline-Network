"use client"

import { useState, useEffect, useCallback, useRef } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { DashboardHeader } from "@/components/dashboard-header"
import { LatencyDisplay } from "@/components/latency-display"
import { PressureChart } from "@/components/pressure-chart"
import { NodeStatusCard } from "@/components/node-status-card"
import { cn } from "@/lib/utils"
import {
  setScenario,
  getScenario,
  generateReadings,
  getFaultDetection,
  getLatencyStats,
  type FaultClass,
  type Severity,
  type SensorReading,
  type FaultDetection,
  type LatencyStats,
} from "@/lib/simulator"
import { Play, AlertTriangle, Ban } from "lucide-react"

interface PressureDataPoint {
  time: string
  nodeA: number
  nodeB: number
  nodeC: number
}

const SCENARIOS: Array<{
  id: FaultClass
  label: string
  code: string
  description: string
  icon: typeof Play
}> = [
  { id: "NORMAL", label: "Normal Operation", code: "NRM", description: "Baseline pipeline conditions - no faults injected", icon: Play },
  { id: "LEAK", label: "Inject Leak", code: "LK", description: "Abrasive leak signature - pressure drop at target node", icon: AlertTriangle },
  { id: "BLOCKAGE", label: "Inject Blockage", code: "BLK", description: "Partial blockage - flow restriction and pressure build-up", icon: Ban },
]

function SimulationContent() {
  const { isAuthenticated } = useAuth()
  const router = useRouter()
  const [activeScenario, setActiveScenario] = useState<FaultClass>("NORMAL")
  const [severity, setSeverity] = useState<Severity>("critical")
  const [readings, setReadings] = useState<SensorReading[]>([])
  const [fault, setFault] = useState<FaultDetection | null>(null)
  const [latency, setLatency] = useState<LatencyStats | null>(null)
  const [pressureHistory, setPressureHistory] = useState<PressureDataPoint[]>([])
  const [tickCount, setTickCount] = useState(0)
  const tickRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    if (!isAuthenticated) router.replace("/")
  }, [isAuthenticated, router])

  const tick = useCallback(() => {
    const newReadings = generateReadings()
    const newFault = getFaultDetection(newReadings)
    const stats = getLatencyStats()

    setReadings(newReadings)
    setFault(newFault)
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
  }, [])

  useEffect(() => {
    tick()
    tickRef.current = setInterval(tick, 500)
    return () => {
      if (tickRef.current) clearInterval(tickRef.current)
    }
  }, [tick])

  function activateScenario(scenario: FaultClass) {
    setActiveScenario(scenario)
    setPressureHistory([])
    setTickCount(0)
    setScenario(scenario, severity)
  }

  function handleSeverityChange(s: Severity) {
    setSeverity(s)
    const { scenario } = getScenario()
    if (scenario !== "NORMAL") {
      setScenario(scenario, s)
    }
  }

  if (!isAuthenticated) return null

  return (
    <div className="min-h-screen bg-background">
      <DashboardHeader />
      <main className="max-w-[1600px] mx-auto px-4 lg:px-6 py-5 flex flex-col gap-5">

        {/* Summary bar */}
        <div className="rounded-lg border border-border bg-card p-3 flex flex-wrap items-center gap-x-6 gap-y-2 font-mono text-xs">
          <span className="text-muted-foreground/60 tracking-widest">HIL_SIMULATION</span>
          <span className="text-muted-foreground/40">|</span>
          <span className="text-muted-foreground">Active: <span className={cn(
            "font-bold",
            activeScenario === "NORMAL" ? "text-primary" : "text-destructive"
          )}>{activeScenario}</span></span>
          <span className="text-muted-foreground">Severity: <span className="text-foreground uppercase">{severity}</span></span>
          <span className="text-muted-foreground">Ticks: <span className="text-foreground tabular-nums">{tickCount}</span></span>
        </div>

        {/* Fault injection controls */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {SCENARIOS.map(({ id, label, code, description, icon: Icon }) => (
            <button
              key={id}
              onClick={() => activateScenario(id)}
              className={cn(
                "rounded-lg border p-4 text-left transition-all",
                activeScenario === id
                  ? id === "NORMAL"
                    ? "border-primary/40 bg-primary/5"
                    : "border-destructive/40 bg-destructive/5"
                  : "border-border bg-card hover:border-muted-foreground/30"
              )}
            >
              <div className="flex items-center gap-2 mb-2">
                <Icon className={cn(
                  "h-4 w-4",
                  activeScenario === id
                    ? id === "NORMAL" ? "text-primary" : "text-destructive"
                    : "text-muted-foreground"
                )} />
                <span className={cn(
                  "font-mono text-xs font-medium tracking-wider",
                  activeScenario === id ? "text-foreground" : "text-muted-foreground"
                )}>
                  {label}
                </span>
                <span className="font-mono text-[10px] text-muted-foreground/40 ml-auto">[{code}]</span>
              </div>
              <p className="font-mono text-[10px] text-muted-foreground/60 leading-relaxed">{description}</p>
              <div className="mt-3">
                <span className={cn(
                  "font-mono text-[10px] px-2.5 py-1 rounded-md font-bold tracking-widest",
                  activeScenario === id
                    ? id === "NORMAL"
                      ? "bg-primary/10 text-primary border border-primary/20"
                      : "bg-destructive/10 text-destructive border border-destructive/20"
                    : "bg-secondary text-muted-foreground"
                )}>
                  {activeScenario === id ? "ACTIVE" : "INJECT"}
                </span>
              </div>
            </button>
          ))}
        </div>

        {/* Severity selector */}
        <div className="rounded-lg border border-border bg-card p-4">
          <span className="font-mono text-xs tracking-widest text-muted-foreground block mb-3">SEVERITY_LEVEL</span>
          <div className="flex flex-wrap gap-3">
            {(["incipient", "moderate", "critical"] as Severity[]).map((s) => (
              <button
                key={s}
                onClick={() => handleSeverityChange(s)}
                className={cn(
                  "flex items-center gap-2 px-4 py-2 rounded-md font-mono text-xs transition-all border tracking-wider",
                  severity === s
                    ? "border-primary/40 bg-primary/10 text-primary font-bold"
                    : "border-border bg-secondary text-muted-foreground hover:text-foreground"
                )}
              >
                <span className={cn(
                  "h-2 w-2 rounded-full",
                  severity === s
                    ? s === "critical" ? "bg-destructive" : s === "moderate" ? "bg-warning" : "bg-primary"
                    : "bg-muted-foreground/30"
                )} />
                <span className="uppercase">{s}</span>
              </button>
            ))}
          </div>
        </div>

        {/* Latency metrics */}
        {latency && <LatencyDisplay stats={latency} />}

        {/* Live response section */}
        <div>
          <span className="font-mono text-xs tracking-widest text-muted-foreground block mb-3">LIVE_RESPONSE</span>
          {readings.length > 0 && fault && (
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
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
          )}
          <PressureChart data={pressureHistory} />
        </div>
      </main>
    </div>
  )
}

export default function SimulationPage() {
  return <SimulationContent />
}
