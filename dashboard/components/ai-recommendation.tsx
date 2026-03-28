"use client"

import { cn } from "@/lib/utils"
import { Bot } from "lucide-react"
import type { AIRecommendation as AIRecommendationType } from "@/lib/simulator"

interface AIRecommendationProps {
  recommendation: AIRecommendationType
}

export function AIRecommendationCard({ recommendation }: AIRecommendationProps) {
  return (
    <div className={cn(
      "rounded-lg border p-4",
      recommendation.urgent ? "border-destructive/40 bg-destructive/5" : "border-border bg-card"
    )}>
      <div className="flex items-center justify-between mb-4">
        <div className="flex items-center gap-2">
          <Bot className="h-3.5 w-3.5 text-primary" />
          <span className="font-mono text-xs tracking-widest text-muted-foreground">AI_ADVISORY</span>
        </div>
        {recommendation.urgent && (
          <span className="font-mono text-[10px] font-bold tracking-widest text-destructive bg-destructive/10 border border-destructive/20 px-2 py-0.5 rounded">
            URGENT
          </span>
        )}
      </div>
      <p className="text-sm text-muted-foreground leading-relaxed mb-4 font-mono">{recommendation.message}</p>
      {recommendation.actions.length > 0 && (
        <div className="border-t border-border/50 pt-3">
          <span className="font-mono text-[10px] text-muted-foreground/60 uppercase tracking-wider block mb-2">
            Recommended Actions
          </span>
          <ol className="flex flex-col gap-1.5">
            {recommendation.actions.map((action, i) => (
              <li key={i} className="flex items-start gap-2.5 font-mono text-xs">
                <span className={cn(
                  "font-bold mt-px shrink-0",
                  recommendation.urgent ? "text-destructive" : "text-primary"
                )}>
                  {String(i + 1).padStart(2, "0")}.
                </span>
                <span className="text-foreground">{action}</span>
              </li>
            ))}
          </ol>
        </div>
      )}
    </div>
  )
}
