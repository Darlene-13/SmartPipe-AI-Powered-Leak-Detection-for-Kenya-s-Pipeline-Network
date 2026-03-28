"use client"

import { cn } from "@/lib/utils"
import type { LatencyStats } from "@/lib/simulator"

interface LatencyDisplayProps {
  stats: LatencyStats
  inline?: boolean
}

export function LatencyDisplay({ stats, inline = false }: LatencyDisplayProps) {
  if (inline) {
    return (
      <div className="flex items-center gap-4 font-mono text-xs">
        <span className="text-muted-foreground/60 tracking-widest">LATENCY:</span>
        <span className="text-foreground tabular-nums">
          {stats.endToEndMs ? `${stats.endToEndMs.toLocaleString()}ms` : "--"}
        </span>
        <span className="text-muted-foreground/40">|</span>
        <span className="text-muted-foreground/60">AVG:</span>
        <span className="text-foreground tabular-nums">{stats.averageMs ? `${stats.averageMs}ms` : "--"}</span>
        <span className="text-muted-foreground/40">|</span>
        <span className="text-muted-foreground/60">MAX:</span>
        <span className="text-foreground tabular-nums">{stats.maxMs ? `${stats.maxMs}ms` : "--"}</span>
        {stats.allWithinTarget && stats.endToEndMs && (
          <>
            <span className="text-muted-foreground/40">|</span>
            <span className="text-primary font-bold tracking-wider">{'< 5s TARGET MET'}</span>
          </>
        )}
      </div>
    )
  }

  return (
    <div className="rounded-lg border border-border bg-card p-4">
      <span className="font-mono text-xs tracking-widest text-muted-foreground block mb-4">LATENCY_METRICS</span>
      <div className="grid grid-cols-2 sm:grid-cols-3 gap-4 font-mono text-xs">
        <div>
          <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Fault Injected</span>
          <span className="text-foreground tabular-nums">
            {stats.lastInjected ? new Date(stats.lastInjected).toLocaleTimeString("en-GB", { hour12: false }) : "--"}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Alert Received</span>
          <span className="text-foreground tabular-nums">
            {stats.alertReceived ? new Date(stats.alertReceived).toLocaleTimeString("en-GB", { hour12: false }) : "--"}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">End-to-End</span>
          <span className={cn(
            "font-bold",
            stats.endToEndMs && stats.endToEndMs < 5000 ? "text-primary" : "text-foreground"
          )}>
            {stats.endToEndMs ? `${stats.endToEndMs.toLocaleString()}ms` : "--"}
          </span>
        </div>
        <div>
          <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Average</span>
          <span className="text-foreground tabular-nums">{stats.averageMs ? `${stats.averageMs}ms` : "--"}</span>
        </div>
        <div>
          <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">Max Recorded</span>
          <span className="text-foreground tabular-nums">{stats.maxMs ? `${stats.maxMs}ms` : "--"}</span>
        </div>
        <div>
          <span className="text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-1">{'< 5s Target'}</span>
          <span className={cn("font-bold", stats.allWithinTarget ? "text-primary" : "text-destructive")}>
            {stats.maxMs > 0 ? (stats.allWithinTarget ? "PASS" : "FAIL") : "--"}
          </span>
        </div>
      </div>
    </div>
  )
}
