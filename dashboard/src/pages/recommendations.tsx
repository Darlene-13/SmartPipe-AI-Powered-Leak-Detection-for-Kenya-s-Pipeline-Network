import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { AppShell } from "@/components/layout/AppShell";
import { useLiveData } from "@/hooks/useLiveData";
import { useSystemStore } from "@/store/systemStore";
import { api } from "@/lib/api";
import { Activity, Brain, AlertTriangle, CheckCircle, Clock, Cpu, RefreshCw, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

const CATEGORY_STYLES = {
  normal:   { bg: "bg-emerald-500/10 border-emerald-500/30", text: "text-emerald-400", icon: CheckCircle,   badge: "bg-emerald-400/10 text-emerald-400" },
  warning:  { bg: "bg-amber-500/10  border-amber-500/30",   text: "text-amber-400",   icon: AlertTriangle, badge: "bg-amber-400/10  text-amber-400"   },
  critical: { bg: "bg-red-500/10    border-red-500/30",     text: "text-red-400",     icon: AlertTriangle, badge: "bg-red-400/10    text-red-400"     },
};

function getCategory(status: string): "normal" | "warning" | "critical" {
  const s = status.toUpperCase();
  if (s.includes("LEAK"))  return "critical";
  if (s.includes("BLOCK")) return "warning";
  return "normal";
}

const MODEL_LABEL = "GROQ · llama-3.3-70b-versatile";

// Convert ms to seconds string, e.g. 2300 -> "2.30s"
function msToSec(val: any, fallback: number): string {
  const n = parseFloat(String(val ?? fallback));
  if (isNaN(n)) return `${fallback.toFixed(2)}s`;
  // if value looks like it's already in seconds (< 100), use as-is
  const secs = n > 100 ? n / 1000 : n;
  return `${secs.toFixed(2)}s`;
}

export default function RecommendationsPage() {
  useLiveData();
  const { recommendation, status, latency } = useSystemStore();
  const [history,      setHistory]      = useState<any[]>([]);
  const [latencyStats, setLatencyStats] = useState<any>(null);
  const [refreshing,   setRefreshing]   = useState(false);
  const [loading,      setLoading]      = useState(true);
  const [selected,     setSelected]     = useState<string | null>(null);

  async function fetchData() {
    setRefreshing(true);
    const [alertsRes, latencyRes] = await Promise.allSettled([
      api.get("/api/alerts/recent"),
      api.get("/api/analytics/latency/stats"),
    ]);

    if (alertsRes.status === "fulfilled") {
      const d = alertsRes.value.data;
      setHistory(Array.isArray(d) ? d : d?.content ?? d?.alerts ?? d?.data ?? []);
    }
    if (latencyRes.status === "fulfilled") setLatencyStats(latencyRes.value.data);

    setLoading(false);
    setRefreshing(false);
  }

  useEffect(() => { fetchData(); }, []);

  const currentCategory = getCategory(status);
  const cfg  = CATEGORY_STYLES[currentCategory];
  const Icon = cfg.icon;

  // averageLatency from backend is in milliseconds — convert to seconds for display
  const avgInference = msToSec(latencyStats?.averageLatency, latency.llm);
  const avgTotal     = msToSec(latencyStats?.averageLatency, latency.total);

  return (
      <AppShell>
        <div className="p-4 md:p-6 space-y-5 max-w-screen-2xl mx-auto">

          {/* Header */}
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold flex items-center gap-2">
                <Brain className="w-5 h-5 text-primary" /> AI Recommendations
              </h1>
              <p className="text-sm text-muted-foreground font-mono mt-0.5">
                LLM-powered pipeline intelligence · GROQ · llama-3.3-70b-versatile
              </p>
            </div>
            <motion.button whileTap={{ scale: 0.95 }} onClick={fetchData}
                           className="flex items-center gap-2 px-4 py-2 bg-muted rounded-xl text-sm font-medium hover:bg-muted/80 transition-colors">
              <RefreshCw className={cn("w-4 h-4", refreshing && "animate-spin")} /> Refresh
            </motion.button>
          </div>

          {/* Live status card */}
          <motion.div initial={{ opacity: 0, y: -10 }} animate={{ opacity: 1, y: 0 }}
                      className={cn("rounded-2xl border p-6 shadow-lg", cfg.bg)}>
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
                    <Clock className="w-3 h-3" /> {new Date().toLocaleTimeString()}
                  </span>
                  <span className="text-xs font-mono text-muted-foreground flex items-center gap-1">
                    <Cpu className="w-3 h-3" /> {latency.llm.toFixed(2)}s inference
                  </span>
                </div>
                <AnimatePresence mode="wait">
                  <motion.p key={recommendation.slice(0, 30)}
                            initial={{ opacity: 0, y: 5 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0 }}
                            className="text-base leading-relaxed">
                    {recommendation || "Awaiting backend recommendation..."}
                  </motion.p>
                </AnimatePresence>
                <div className="mt-3 flex items-center gap-4">
                  <span className="text-xs font-mono bg-card/50 border border-border px-2 py-1 rounded-lg">
                    {MODEL_LABEL}
                  </span>
                </div>
              </div>
            </div>
          </motion.div>

          {/* Stats cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {[
              { label: "Model",                  value: "LLaMA 3.3 70B",  sub: "Groq cloud inference",     icon: Brain,    color: "text-purple-400"  },
              { label: "Avg Inference Time",     value: avgInference,     sub: "LLM latency from backend", icon: Cpu,      color: "text-cyan-400"    },
              { label: "Total Pipeline Latency", value: avgTotal,         sub: "End-to-end < 5s target",   icon: Activity, color: "text-emerald-400" },
            ].map((card) => (
                <motion.div key={card.label} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }}
                            className="bg-card border border-card-border rounded-2xl p-4 shadow-sm">
                  <div className="flex items-center gap-2 mb-2">
                    <card.icon className={cn("w-4 h-4", card.color)} />
                    <span className="text-xs font-mono text-muted-foreground uppercase tracking-wider">{card.label}</span>
                  </div>
                  <div className={cn("text-2xl font-mono font-bold", card.color)}>{card.value}</div>
                  <div className="text-xs text-muted-foreground mt-1">{card.sub}</div>
                </motion.div>
            ))}
          </div>

          {/* Recent alert recommendations */}
          <div className="bg-card border border-card-border rounded-2xl shadow-sm overflow-hidden">
            <div className="px-5 py-4 border-b border-border flex items-center justify-between">
              <h3 className="font-semibold">Recent Alert Recommendations</h3>
              <span className="text-xs text-muted-foreground font-mono">{history.length} entries</span>
            </div>
            <div className="divide-y divide-border/50">
              {loading ? (
                  <div className="text-center text-muted-foreground text-sm py-10 font-mono">Loading from backend...</div>
              ) : history.length === 0 ? (
                  <div className="text-center text-muted-foreground text-sm py-10 font-mono">No recent alerts from backend</div>
              ) : history.map((entry, i) => {
                const faultClass = entry.faultClass ?? entry.fault_class ?? "NORMAL";
                const cat        = getCategory(faultClass);
                const entryCfg   = CATEGORY_STYLES[cat];
                const EntryIcon  = entryCfg.icon;
                const id         = String(entry.id ?? i);
                const ts         = entry.createdAt ?? entry.timestamp;
                const recText    = entry.recommendation ?? entry.description ?? entry.message ?? "No recommendation available.";
                const sev        = entry.severityLevel ?? entry.severity ?? "MODERATE";
                const conf       = parseFloat(entry.confidence ?? entry.score ?? 0.85);

                return (
                    <motion.div key={id} initial={{ opacity: 0 }} animate={{ opacity: 1 }}
                                transition={{ delay: i * 0.04 }}
                                onClick={() => setSelected(selected === id ? null : id)}
                                className="px-5 py-4 hover:bg-muted/20 cursor-pointer transition-colors">
                      <div className="flex items-start gap-4">
                        <div className={cn("w-8 h-8 rounded-xl flex items-center justify-center shrink-0 mt-0.5", entryCfg.badge)}>
                          <EntryIcon className="w-4 h-4" />
                        </div>
                        <div className="flex-1 min-w-0">
                          <div className="flex items-center gap-2 mb-1 flex-wrap">
                            <span className={cn("text-xs font-mono font-bold uppercase px-1.5 py-0.5 rounded", entryCfg.badge)}>
                              {cat}
                            </span>
                            <span className="text-xs text-muted-foreground font-mono">
                              {faultClass.replace(/_/g, " ")}
                            </span>
                            <span className="text-xs text-muted-foreground font-mono ml-auto">
                              {ts ? new Date(ts).toLocaleTimeString() : "?"}
                            </span>
                          </div>
                          <p className={cn("text-sm transition-all", selected === id ? "" : "line-clamp-2 text-muted-foreground")}>
                            {recText}
                          </p>
                          {selected === id && (
                              <motion.div initial={{ opacity: 0, height: 0 }} animate={{ opacity: 1, height: "auto" }}
                                          className="mt-2 flex items-center gap-3 text-xs font-mono text-muted-foreground">
                                <span>Severity: {sev}</span>
                                <span>·</span>
                                <span>Confidence: {(conf * 100).toFixed(0)}%</span>
                                <span>·</span>
                                <span>{MODEL_LABEL}</span>
                              </motion.div>
                          )}
                        </div>
                        <ChevronRight className={cn("w-4 h-4 text-muted-foreground shrink-0 transition-transform", selected === id && "rotate-90")} />
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