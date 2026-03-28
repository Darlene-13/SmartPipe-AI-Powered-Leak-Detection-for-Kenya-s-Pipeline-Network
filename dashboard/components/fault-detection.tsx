"use client"

import { cn } from "@/lib/utils"
import type { FaultDetection as FaultDetectionType } from "@/lib/simulator"

interface FaultDetectionProps {
  fault: FaultDetectionType
}

export function FaultDetectionCard({ fault }: FaultDetectionProps) {
  const isNormal = fault.classification === "NORMAL"
  const isLeak = fault.classification === "LEAK"

  const borderColor = !isNormal
    ? isLeak ? "border-destructive/40" : "border-warning/30"
    : "border-border"

  return (
    <div className={cn(
      "rounded-lg border p-4",
      borderColor,
      !isNormal ? (isLeak ? "bg-destructive/5" : "bg-warning/5") : "bg-card"
    )}>
      <div className="flex items-center justify-between mb-4">
        <span className="font-mono text-xs tracking-widest text-muted-foreground">ML_CLASSIFIER</span>
        <span className={cn(
          "font-mono text-[10px] font-bold tracking-widest",
          isNormal ? "text-primary" : "text-destructive"
        )}>
          [{isNormal ? "CLEAR" : "DETECTED"}]
        </span>
      </div>

      <div className="flex flex-col gap-3">
        <div className="flex justify-between items-baseline">
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider">Fault Class</span>
          <span className={cn(
            "font-mono text-sm font-bold tracking-wider",
            isNormal ? "text-primary" : isLeak ? "text-destructive" : "text-warning"
          )}>
            {fault.classification}
          </span>
        </div>

        <div className="flex justify-between items-baseline">
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider">Confidence</span>
          <span className="font-mono text-sm text-foreground">{fault.confidence.toFixed(1)}%</span>
        </div>

        <div className="flex justify-between items-baseline">
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider">dP/dt</span>
          <span className={cn("font-mono text-sm", fault.dpdt < -100 ? "text-destructive" : "text-foreground")}>
            {fault.dpdt > 0 ? "+" : ""}{fault.dpdt.toFixed(0)} Pa/s
          </span>
        </div>

        {fault.affectedNode && (
          <div className="flex justify-between items-baseline">
            <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider">Location</span>
            <span className="font-mono text-sm text-destructive font-bold">NODE_{fault.affectedNode}</span>
          </div>
        )}

        {fault.severity !== "none" && (
          <div className="flex justify-between items-baseline">
            <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider">Severity</span>
            <span className={cn(
              "font-mono text-sm font-bold uppercase",
              fault.severity === "critical" ? "text-destructive" : fault.severity === "moderate" ? "text-warning" : "text-muted-foreground"
            )}>
              {fault.severity}
            </span>
          </div>
        )}
      </div>

      {!isNormal && (
        <div className="mt-4 pt-3 border-t border-border/50">
          <p className="font-mono text-[10px] text-muted-foreground leading-relaxed">
            {fault.description}
          </p>
        </div>
      )}
    </div>
  )
}
