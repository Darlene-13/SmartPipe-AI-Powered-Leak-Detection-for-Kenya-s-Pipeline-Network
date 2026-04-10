import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { AppShell } from "@/components/layout/AppShell";
import { useLiveData } from "@/hooks/useLiveData";
import { useSystemStore } from "@/store/systemStore";
import { Activity, Brain, AlertTriangle, CheckCircle, Clock, Cpu, RefreshCw, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

interface RecommendationEntry {
  id: string;
  timestamp: string;
  status: string;
  recommendation: string;
  confidence: number;
  model: string;
  latency: number;
  category: "normal" | "warning" | "critical";
}

function generateRecommendations(): RecommendationEntry[] {
  const recs = [
    { status: "NORMAL_OPERATION", recommendation: "Pipeline operating normally. All pressure readings within acceptable range. Continue routine monitoring schedule. No maintenance action required at this time.", confidence: 0.97, category: "normal" as const },
    { status: "NORMAL_OPERATION", recommendation: "Steady-state conditions maintained across all three nodes. Node A upstream pressure nominal at ~101 kPa. No anomalies detected in the last 24 hours.", confidence: 0.95, category: "normal" as const },
    { status: "LEAK_INCIPIENT", recommendation: "Incipient leak signature detected at midstream junction. Pressure differential of 3.2 kPa between Node A and Node B exceeds threshold. Recommend visual inspection of section AB within 4 hours.", confidence: 0.83, category: "warning" as const },
    { status: "BLOCKAGE_25", recommendation: "Minor blockage detected at downstream segment. 25% flow restriction indicated. Monitor closely for 2 hours. If condition persists, dispatch maintenance team to inspect valve V-07.", confidence: 0.88, category: "warning" as const },
    { status: "LEAK_CRITICAL", recommendation: "CRITICAL: Major pressure loss detected. Estimated 15 L/min leak rate at section BC. Immediate action required: (1) Isolate section BC, (2) Reduce inlet pressure to 80 kPa, (3) Notify operations supervisor.", confidence: 0.96, category: "critical" as const },
    { status: "NORMAL_OPERATION", recommendation: "Post-maintenance verification complete. Pressure readings stabilized across all nodes. Pipeline integrity confirmed. Resume normal operations.", confidence: 0.99, category: "normal" as const },
    { status: "BLOCKAGE_75", recommendation: "CRITICAL BLOCKAGE: 75% flow restriction detected. Upstream pressure dangerously elevated at 106 kPa. Emergency shutdown of section recommended. Contact field team immediately.", confidence: 0.94, category: "critical" as const },
  ];
  return recs.map((r, i) => ({
    id: String(i + 1),
    timestamp: new Date(Date.now() - (recs.length - 1 - i) * 3600000).toISOString(),
    ...r,
    model: "OLLAMA · LLAMA3",
    latency: 0.5 + Math.random() * 1.5,
  }));
}

const CATEGORY_STYLES = {
  normal: { bg: "bg-emerald-500/10 border-emerald-500/30", text: "text-emerald-400", icon: CheckCircle, badge: "bg-emerald-400/10 text-emerald-400" },
  warning: { bg: "bg-amber-500/10 border-amber-500/30", text: "text-amber-400", icon: AlertTriangle, badge: "bg-amber-400/10 text-amber-400" },
  critical: { bg: "bg-red-500/10 border-red-500/30", text: "text-red-400", icon: AlertTriangle, badge: "bg-red-400/10 text-red-400" },
};

export default function RecommendationsPage() {
  useLiveData();
  const { recommendation, status, latency } = useSystemStore();
  const [history] = useState<RecommendationEntry[]>(generateRecommendations);
  const [refreshing, setRefreshing] = useState(false);
  const [currentRec, setCurrentRec] = useState(recommendation);
  const [selected, setSelected] = useState<RecommendationEntry | null>(null);

  useEffect(() => { setCurrentRec(recommendation); }, [recommendation]);

  const handleRefresh = async () => {
    setRefreshing(true);
    await new Promise(r => setTimeout(r, 1200));
    setRefreshing(false);
  };

  const currentCategory = status === "NORMAL_OPERATION" ? "normal" : status === "OFFLINE" ? "normal" : status === "LEAK_DETECTED" ? "critical" : "warning";
  const cfg = CATEGORY_STYLES[currentCategory];
  const Icon = cfg.icon;

  return (
    <AppShell>
      <div className="p-4 md:p-6 space-y-5 max-w-screen-2xl mx-auto">
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-xl font-bold flex items-center gap-2">
              <Brain className="w-5 h-5 text-primary" />
              AI Recommendations
            </h1>
            <p className="text-sm text-muted-foreground font-mono mt-0.5">LLM-powered pipeline intelligence · OLLAMA · LLAMA3</p>
          </div>
          <motion.button whileTap={{ scale: 0.95 }} onClick={handleRefresh} className="flex items-center gap-2 px-4 py-2 bg-muted rounded-xl text-sm font-medium hover:bg-muted/80 transition-colors">
            <RefreshCw className={cn("w-4 h-4", refreshing && "animate-spin")} />
            Refresh
          </motion.button>
        </div>

        <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }} className={cn("rounded-2xl border p-6 shadow-lg", cfg.bg)}>
          <div className="flex items-start gap-4">
            <div className={cn("w-12 h-12 rounded-2xl flex items-center justify-center shrink-0", cfg.badge)}>
              <Icon className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <div className="flex items-center gap-3 mb-2 flex-wrap">
                <span className={cn("text-xs font-mono font-bold tracking-wider uppercase px-2 py-0.5 rounded-full", cfg.badge)}>
                  LIVE · {status.replace(/_/g, " ")}
                </span>
                <span className="text-xs font-mono text-muted-foreground flex items-center gap-1">
                  <Clock className="w-3 h-3" />{new Date().toLocaleTimeString()}
                </span>
                <span className="text-xs font-mono text-muted-foreground flex items-center gap-1">
                  <Cpu className="w-3 h-3" />{latency.llm.toFixed(2)}s inference
                </span>
              </div>
              <AnimatePresence mode="wait">
                <motion.p key={currentRec.slice(0, 30)} initial={{ opacity: 0, y: 5 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }} className="text-base leading-relaxed">
                  {currentRec}
                </motion.p>
              </AnimatePresence>
              <div className="mt-3 flex items-center gap-4">
                <span className="text-xs font-mono bg-card/50 border border-border px-2 py-1 rounded-lg">OLLAMA · LLAMA3 · llama3.2:3b</span>
                <span className="text-xs text-muted-foreground font-mono">Confidence: {status === "NORMAL_OPERATION" ? "97" : "89"}%</span>
              </div>
            </div>
          </div>
        </motion.div>

        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          {[
            { label: "Model", value: "LLaMA 3.2 3B", sub: "OLLAMA local inference", icon: Brain, color: "text-purple-400" },
            { label: "Avg Inference Time", value: `${latency.llm.toFixed(2)}s`, sub: "Local GPU accelerated", icon: Cpu, color: "text-cyan-400" },
            { label: "Total Pipeline Latency", value: `${latency.total.toFixed(2)}s`, sub: "End-to-end < 5s target", icon: Activity, color: "text-emerald-400" },
          ].map((card) => (
            <motion.div key={card.label} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="bg-card border border-card-border rounded-2xl p-4 shadow-sm">
              <div className="flex items-center gap-2 mb-2">
                <card.icon className={cn("w-4 h-4", card.color)} />
                <span className="text-xs font-mono text-muted-foreground uppercase tracking-wider">{card.label}</span>
              </div>
              <div className={cn("text-2xl font-mono font-bold", card.color)}>{card.value}</div>
              <div className="text-xs text-muted-foreground mt-1">{card.sub}</div>
            </motion.div>
          ))}
        </div>

        <div className="bg-card border border-card-border rounded-2xl shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-border flex items-center justify-between">
            <h3 className="font-semibold">Recommendation History</h3>
            <span className="text-xs text-muted-foreground font-mono">{history.length} entries</span>
          </div>
          <div className="divide-y divide-border/50">
            {history.map((entry, i) => {
              const entryCfg = CATEGORY_STYLES[entry.category];
              const EntryIcon = entryCfg.icon;
              return (
                <motion.div
                  key={entry.id}
                  initial={{ opacity: 0 }}
                  animate={{ opacity: 1 }}
                  transition={{ delay: i * 0.05 }}
                  onClick={() => setSelected(selected?.id === entry.id ? null : entry)}
                  className="px-5 py-4 hover:bg-muted/20 cursor-pointer transition-colors"
                >
                  <div className="flex items-start gap-4">
                    <div className={cn("w-8 h-8 rounded-xl flex items-center justify-center shrink-0 mt-0.5", entryCfg.badge)}>
                      <EntryIcon className="w-4 h-4" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1 flex-wrap">
                        <span className={cn("text-xs font-mono font-bold uppercase px-1.5 py-0.5 rounded", entryCfg.badge)}>{entry.category}</span>
                        <span className="text-xs text-muted-foreground font-mono">{entry.status.replace(/_/g, " ")}</span>
                        <span className="text-xs text-muted-foreground font-mono ml-auto">{new Date(entry.timestamp).toLocaleTimeString()}</span>
                      </div>
                      <p className={cn("text-sm transition-all", selected?.id === entry.id ? "" : "line-clamp-1 text-muted-foreground")}>
                        {entry.recommendation}
                      </p>
                      {selected?.id === entry.id && (
                        <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: "auto" }} className="mt-2 flex items-center gap-3 text-xs font-mono text-muted-foreground">
                          <span>Confidence: {(entry.confidence * 100).toFixed(0)}%</span>
                          <span>·</span>
                          <span>{entry.model}</span>
                          <span>·</span>
                          <span>{entry.latency.toFixed(2)}s inference</span>
                        </motion.div>
                      )}
                    </div>
                    <ChevronRight className={cn("w-4 h-4 text-muted-foreground shrink-0 transition-transform", selected?.id === entry.id && "rotate-90")} />
                  </div>
                </motion.div>
              );
            })}
          </div>
        </div>
      </div>
    </AppShell>
  );
}
