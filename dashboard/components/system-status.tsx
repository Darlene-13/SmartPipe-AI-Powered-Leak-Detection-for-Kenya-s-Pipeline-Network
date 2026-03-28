"use client"

import { cn } from "@/lib/utils"
import type { SystemStatus } from "@/lib/simulator"

interface SystemStatusProps {
  status: SystemStatus
}

export function SystemStatusCard({ status }: SystemStatusProps) {
  const isNormal = status.level === "NORMAL"
  const isCritical = status.level === "LEAK_DETECTED"
  const isBlockage = status.level === "BLOCKAGE_DETECTED"

  const levelColor = isCritical
    ? "text-destructive"
    : isBlockage
      ? "text-warning"
      : "text-primary"

  const borderColor = isCritical
    ? "border-destructive/40"
    : isBlockage
      ? "border-warning/30"
      : "border-border"

  return (
    <div className={cn("rounded-lg border p-4", borderColor, isCritical ? "bg-destructive/5" : isBlockage ? "bg-warning/5" : "bg-card")}>
      <div className="flex items-center justify-between mb-4">
        <span className="font-mono text-xs tracking-widest text-muted-foreground">SYS_STATUS</span>
        <div className={cn("h-2.5 w-2.5 rounded-full", isCritical ? "bg-destructive animate-pulse" : isBlockage ? "bg-warning" : "bg-primary")} />
      </div>

      <div className="flex items-center gap-2 mb-2">
        <span className={cn("font-mono text-sm font-bold tracking-wider", levelColor)}>
          {status.level}
        </span>
      </div>
      <p className="text-xs text-muted-foreground mb-4 leading-relaxed">
        {status.description}
      </p>

      {status.requiresAction && (
        <div className="mb-4 font-mono text-[10px] text-destructive bg-destructive/10 border border-destructive/20 rounded px-2.5 py-1.5 tracking-wider">
          ACTION_REQUIRED: OPERATOR INTERVENTION NEEDED
        </div>
      )}

      <div className="grid grid-cols-2 gap-3 pt-3 border-t border-border/50">
        <div>
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block">Flow Rate</span>
          <span className="font-mono text-sm text-foreground">{status.flowRate} m/s</span>
        </div>
        <div>
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block">dP/dt</span>
          <span className={cn("font-mono text-sm", status.dpdt < -100 ? "text-destructive" : "text-foreground")}>
            {status.dpdt > 0 ? "+" : ""}{status.dpdt} Pa/s
          </span>
        </div>
        <div>
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block">Pump RPM</span>
          <span className="font-mono text-sm text-foreground">{status.pumpRpm}</span>
        </div>
        <div>
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block">Slurry SG</span>
          <span className="font-mono text-sm text-foreground">{status.slurryDensity}</span>
        </div>
        <div>
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block">Uptime</span>
          <span className="font-mono text-sm text-primary">{status.uptime.toFixed(1)}%</span>
        </div>
      </div>
    </div>
  )
}
