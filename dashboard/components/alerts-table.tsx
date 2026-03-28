"use client"

import { cn } from "@/lib/utils"
import type { Alert } from "@/lib/simulator"

interface AlertsTableProps {
  alerts: Alert[]
  title?: string
  maxRows?: number
}

export function AlertsTable({ alerts, title = "ALERT_LOG", maxRows }: AlertsTableProps) {
  const displayed = maxRows ? alerts.slice(0, maxRows) : alerts

  return (
    <div className="rounded-lg border border-border bg-card p-4">
      <div className="flex items-center justify-between mb-3">
        <span className="font-mono text-xs tracking-widest text-muted-foreground">{title}</span>
        <span className="font-mono text-[10px] text-muted-foreground/50">{alerts.length} records</span>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full font-mono text-xs">
          <thead>
            <tr className="border-b border-border">
              <th className="text-left py-2 px-2 text-[10px] font-medium text-muted-foreground/60 tracking-widest uppercase">ID</th>
              <th className="text-left py-2 px-2 text-[10px] font-medium text-muted-foreground/60 tracking-widest uppercase">Time</th>
              <th className="text-left py-2 px-2 text-[10px] font-medium text-muted-foreground/60 tracking-widest uppercase">Class</th>
              <th className="text-left py-2 px-2 text-[10px] font-medium text-muted-foreground/60 tracking-widest uppercase">Severity</th>
              <th className="text-left py-2 px-2 text-[10px] font-medium text-muted-foreground/60 tracking-widest uppercase">Node</th>
              <th className="text-right py-2 px-2 text-[10px] font-medium text-muted-foreground/60 tracking-widest uppercase">Conf</th>
            </tr>
          </thead>
          <tbody>
            {displayed.length === 0 && (
              <tr>
                <td colSpan={6} className="text-center py-8 text-muted-foreground/50 text-xs tracking-wider">
                  NO ALERTS RECORDED
                </td>
              </tr>
            )}
            {displayed.map((alert) => (
              <tr key={alert.id} className="border-b border-border/30 last:border-0 hover:bg-accent/30 transition-colors">
                <td className="py-2 px-2 text-muted-foreground">{alert.id}</td>
                <td className="py-2 px-2 text-muted-foreground tabular-nums">
                  {new Date(alert.timestamp).toLocaleTimeString("en-GB", { hour12: false })}
                </td>
                <td className="py-2 px-2">
                  <span className={cn(
                    "font-bold",
                    alert.type === "NORMAL"
                      ? "text-primary"
                      : alert.type === "LEAK"
                        ? "text-destructive"
                        : "text-warning"
                  )}>
                    {alert.type}
                  </span>
                </td>
                <td className="py-2 px-2">
                  <span className={cn(
                    alert.severity === "critical"
                      ? "text-destructive font-bold"
                      : alert.severity === "moderate"
                        ? "text-warning"
                        : "text-muted-foreground"
                  )}>
                    {alert.severity === "none" ? "--" : alert.severity.toUpperCase()}
                  </span>
                </td>
                <td className="py-2 px-2 text-foreground">
                  {alert.nodeId ? `NODE_${alert.nodeId}` : "--"}
                </td>
                <td className="py-2 px-2 text-right text-foreground tabular-nums">
                  {alert.confidence}%
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
