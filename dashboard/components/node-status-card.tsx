"use client"

import { cn } from "@/lib/utils"
import type { SensorReading } from "@/lib/simulator"

interface NodeStatusCardProps {
  reading: SensorReading
  isFault: boolean
  isWarning: boolean
}

export function NodeStatusCard({ reading, isFault, isWarning }: NodeStatusCardProps) {
  const statusLabel = isFault ? "FAULT" : isWarning ? "WATCH" : "NOMINAL"
  const statusColor = isFault ? "text-destructive" : isWarning ? "text-warning" : "text-primary"
  const borderColor = isFault
    ? "border-destructive/40"
    : isWarning
      ? "border-warning/30"
      : "border-border"
  const bgColor = isFault
    ? "bg-destructive/5"
    : isWarning
      ? "bg-warning/5"
      : "bg-card"

  return (
    <div className={cn("rounded-lg border p-4 transition-all duration-300", borderColor, bgColor)}>
      {/* Header row */}
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className={cn(
            "h-2.5 w-2.5 rounded-full",
            isFault ? "bg-destructive animate-pulse" : isWarning ? "bg-warning" : "bg-primary"
          )} />
          <span className="font-mono text-xs tracking-widest text-muted-foreground">
            NODE_{reading.nodeId}
          </span>
        </div>
        <span className={cn("font-mono text-[10px] font-bold tracking-widest", statusColor)}>
          [{statusLabel}]
        </span>
      </div>

      {/* Primary reading */}
      <div className="mb-3">
        <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider">Pressure</span>
        <p className={cn(
          "font-mono text-2xl font-bold tracking-tight leading-tight",
          isFault ? "text-destructive" : "text-foreground"
        )}>
          {reading.pressure.toLocaleString()}
          <span className="text-xs font-normal text-muted-foreground ml-1">Pa</span>
        </p>
      </div>

      {/* Metrics grid */}
      <div className="grid grid-cols-2 gap-x-4 gap-y-2 pt-3 border-t border-border/50">
        <div>
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block">Flow Velocity</span>
          <span className="font-mono text-sm text-foreground">{reading.flowRate} m/s</span>
        </div>
        <div>
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block">dP/dt</span>
          <span className={cn("font-mono text-sm", reading.dpdt < -100 ? "text-destructive" : "text-foreground")}>
            {reading.dpdt > 0 ? "+" : ""}{reading.dpdt} Pa/s
          </span>
        </div>
      </div>
    </div>
  )
}
