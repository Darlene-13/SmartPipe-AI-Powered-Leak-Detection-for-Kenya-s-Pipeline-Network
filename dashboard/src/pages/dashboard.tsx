import { useEffect, useRef, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { AppShell } from "@/components/layout/AppShell";
import { useLiveData } from "@/hooks/useLiveData";
import { usePressureHistory } from "@/hooks/usePressureHistory";
import { useSystemStore } from "@/store/systemStore";
import { cn } from "@/lib/utils";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip,
  ResponsiveContainer, Legend
} from "recharts";
import {
  AlertTriangle, TrendingUp, TrendingDown, Minus,
  Cpu, Clock, Activity, Zap, Waves
} from "lucide-react";

// ─── Status configuration ────────────────────────────────────────────────────

const STATUS_CONFIG = {
  NORMAL_OPERATION: {
    label: "NORMAL OPERATION",
    bg:   "bg-emerald-500/10 border-emerald-500/30",
    text: "text-emerald-400",
    glow: "shadow-emerald-500/20",
    bar:  "from-emerald-500 to-cyan-400",
    dot:  "bg-emerald-400",
  },
  LEAK_DETECTED: {
    label: "LEAK DETECTED",
    bg:   "bg-red-500/10 border-red-500/30",
    text: "text-red-400",
    glow: "shadow-red-500/20",
    bar:  "from-red-500 to-orange-400",
    dot:  "bg-red-400",
  },
  BLOCKAGE_DETECTED: {
    label: "BLOCKAGE DETECTED",
    bg:   "bg-amber-500/10 border-amber-500/30",
    text: "text-amber-400",
    glow: "shadow-amber-500/20",
    bar:  "from-amber-500 to-yellow-400",
    dot:  "bg-amber-400",
  },
  OFFLINE: {
    label: "OFFLINE",
    bg:   "bg-gray-500/10 border-gray-500/30",
    text: "text-gray-400",
    glow: "shadow-gray-500/20",
    bar:  "from-gray-500 to-gray-400",
    dot:  "bg-gray-400",
  },
};

const TREND_ICONS = {
  rising:  TrendingUp,
  falling: TrendingDown,
  stable:  Minus,
};

// ─── Node Card ───────────────────────────────────────────────────────────────

function NodeCard({ reading }: {
  reading: {
    nodeId: string;
    nodeName: string;
    pressure: number;
    velocity?: number;
    trend: string;
  };
}) {
  const TrendIcon  = TREND_ICONS[reading.trend as keyof typeof TREND_ICONS] ?? Minus;
  const trendColor =
      reading.trend === "rising"  ? "text-amber-400" :
          reading.trend === "falling" ? "text-blue-400"  : "text-emerald-400";

  const barColor =
      reading.nodeId === "A" ? "bg-blue-400" :
          reading.nodeId === "B" ? "bg-purple-400" : "bg-cyan-400";

  // Clamp bar width: pressure range 0–120k Pa
  const barPct = Math.min(Math.max((reading.pressure / 120000) * 100, 0), 100);

  return (
      <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          whileHover={{ y: -2 }}
          className="bg-card border border-card-border rounded-2xl p-5 shadow-sm"
      >
        <div className="flex items-start justify-between mb-4">
          <div>
            <div className="text-xs font-mono text-muted-foreground uppercase tracking-widest mb-1">
              Node {reading.nodeId}
            </div>
            <div className="text-sm font-semibold">{reading.nodeName}</div>
          </div>
          <div className={cn("flex items-center gap-1.5 text-xs font-mono", trendColor)}>
            <TrendIcon className="w-3.5 h-3.5" />
            {reading.trend}
          </div>
        </div>

        {/* Pressure */}
        <div className="text-3xl font-mono font-bold tracking-tight">
          {Math.round(reading.pressure).toLocaleString()}
          <span className="text-sm text-muted-foreground ml-1 font-normal">Pa</span>
        </div>
        <div className="mt-2 h-1.5 bg-muted rounded-full overflow-hidden">
          <motion.div
              className={cn("h-full rounded-full", barColor)}
              animate={{ width: `${barPct}%` }}
              transition={{ duration: 0.3 }}
          />
        </div>

        {/* Velocity — shown if available */}
        {reading.velocity !== undefined && reading.velocity > 0 && (
            <div className="mt-3 flex items-center gap-2">
              <Waves className="w-3 h-3 text-muted-foreground shrink-0" />
              <span className="text-xs font-mono text-muted-foreground">
            {reading.velocity.toFixed(3)} m/s
          </span>
            </div>
        )}
      </motion.div>
  );
}

// ─── Live Pressure Chart ─────────────────────────────────────────────────────

function LivePressureChart() {
  const history      = usePressureHistory();
  const [, setTick]  = useState(0);

  useEffect(() => {
    const id = setInterval(() => setTick(t => t + 1), 500);
    return () => clearInterval(id);
  }, []);

  const data = history.slice(-30);

  return (
      <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="font-semibold">Live Pressure Monitor</h3>
            <p className="text-xs text-muted-foreground font-mono mt-0.5">
              Real-time readings · 500ms interval
            </p>
          </div>
          <div className="flex items-center gap-1.5 text-xs text-emerald-400 font-mono">
            <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full status-pulse" />
            LIVE
          </div>
        </div>
        <div className="h-52">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 5, right: 5, bottom: 5, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" opacity={0.5} />
              <XAxis
                  dataKey="time"
                  tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }}
                  tickLine={false} axisLine={false} interval={9}
              />
              <YAxis
                  tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }}
                  tickLine={false} axisLine={false}
                  tickFormatter={(v) => (v / 1000).toFixed(0) + "k"}
              />
              <Tooltip
                  contentStyle={{
                    background: "hsl(var(--card))", border: "1px solid hsl(var(--card-border))",
                    borderRadius: "12px", fontSize: "11px", fontFamily: "Space Mono",
                  }}
                  formatter={(val: number) => [`${Math.round(val).toLocaleString()} Pa`, ""]}
              />
              <Legend wrapperStyle={{ fontSize: "11px", fontFamily: "Space Mono" }} />
              <Line type="monotone" dataKey="nodeA" stroke="#60a5fa" strokeWidth={2} dot={false} name="Node A" />
              <Line type="monotone" dataKey="nodeB" stroke="#a78bfa" strokeWidth={2} dot={false} name="Node B" />
              <Line type="monotone" dataKey="nodeC" stroke="#22d3ee" strokeWidth={2} dot={false} name="Node C" />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
  );
}

// ─── Live Velocity Chart ─────────────────────────────────────────────────────
// Reads velocity fields from the same pressure-history store.
// usePressureHistory already stores nodeA/B/C — we add velA/B/C here.
// If your store doesn't yet carry velocity, this chart gracefully shows 0.

function LiveVelocityChart() {
  const history      = usePressureHistory();
  const [, setTick]  = useState(0);

  useEffect(() => {
    const id = setInterval(() => setTick(t => t + 1), 500);
    return () => clearInterval(id);
  }, []);

  // Map history to velocity fields — fall back to 0 if not yet stored
  const data = history.slice(-30).map((pt: any) => ({
    time: pt.time,
    velA: pt.velA ?? pt.velocityA ?? 0,
    velB: pt.velB ?? pt.velocityB ?? 0,
    velC: pt.velC ?? pt.velocityC ?? 0,
  }));

  return (
      <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="font-semibold">Live Velocity Monitor</h3>
            <p className="text-xs text-muted-foreground font-mono mt-0.5">
              Flow velocity m/s · 500ms interval
            </p>
          </div>
          <div className="flex items-center gap-1.5 text-xs text-cyan-400 font-mono">
            <span className="w-1.5 h-1.5 bg-cyan-400 rounded-full status-pulse" />
            LIVE
          </div>
        </div>
        <div className="h-52">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 5, right: 5, bottom: 5, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" opacity={0.5} />
              <XAxis
                  dataKey="time"
                  tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }}
                  tickLine={false} axisLine={false} interval={9}
              />
              <YAxis
                  tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }}
                  tickLine={false} axisLine={false}
                  tickFormatter={(v) => v.toFixed(1)}
                  domain={[0, "auto"]}
              />
              <Tooltip
                  contentStyle={{
                    background: "hsl(var(--card))", border: "1px solid hsl(var(--card-border))",
                    borderRadius: "12px", fontSize: "11px", fontFamily: "Space Mono",
                  }}
                  formatter={(val: number) => [`${val.toFixed(3)} m/s`, ""]}
              />
              <Legend wrapperStyle={{ fontSize: "11px", fontFamily: "Space Mono" }} />
              <Line type="monotone" dataKey="velA" stroke="#f59e0b" strokeWidth={2} dot={false} name="Node A" />
              <Line type="monotone" dataKey="velB" stroke="#10b981" strokeWidth={2} dot={false} name="Node B" />
              <Line type="monotone" dataKey="velC" stroke="#f43f5e" strokeWidth={2} dot={false} name="Node C" />
            </LineChart>
          </ResponsiveContainer>
        </div>
      </div>
  );
}

// ─── Alert Feed ──────────────────────────────────────────────────────────────

function AlertsFeed() {
  const { alerts } = useSystemStore();

  const SEVERITY_COLORS: Record<string, string> = {
    NONE:     "text-emerald-400 bg-emerald-400/10 border-emerald-400/30",
    LOW:      "text-emerald-400 bg-emerald-400/10 border-emerald-400/30",
    MEDIUM:   "text-amber-400  bg-amber-400/10  border-amber-400/30",
    MODERATE: "text-amber-400  bg-amber-400/10  border-amber-400/30",
    HIGH:     "text-orange-400 bg-orange-400/10 border-orange-400/30",
    CRITICAL: "text-red-400    bg-red-400/10    border-red-400/30",
  };

  return (
      <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm flex flex-col">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h3 className="font-semibold">Alert Feed</h3>
            <p className="text-xs text-muted-foreground font-mono mt-0.5">
              {alerts.filter(a => a.severity === "HIGH" || a.severity === "CRITICAL").length} active alerts
            </p>
          </div>
          <AlertTriangle className="w-4 h-4 text-muted-foreground" />
        </div>
        <div className="flex-1 space-y-2 overflow-y-auto max-h-52">
          <AnimatePresence>
            {alerts.length === 0 ? (
                <div className="text-center text-muted-foreground text-sm py-8 font-mono">
                  No alerts recorded
                </div>
            ) : (
                alerts.map((alert) => (
                    <motion.div
                        key={alert.id}
                        initial={{ opacity: 0, x: -10 }}
                        animate={{ opacity: 1, x: 0 }}
                        className={cn(
                            "flex items-center gap-3 px-3 py-2 rounded-xl border text-xs",
                            SEVERITY_COLORS[alert.severity] ?? SEVERITY_COLORS["LOW"]
                        )}
                    >
                      <span className="font-mono font-bold shrink-0">{alert.severity}</span>
                      <span className="flex-1 truncate">{alert.description}</span>
                      <span className="font-mono shrink-0 opacity-70">
                  {new Date(alert.timestamp).toLocaleTimeString()}
                </span>
                    </motion.div>
                ))
            )}
          </AnimatePresence>
        </div>
      </div>
  );
}

// ─── Latency Meter ───────────────────────────────────────────────────────────

function LatencyMeter() {
  const { latency } = useSystemStore();
  const pct = Math.min((latency.total / 5) * 100, 100);

  return (
      <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-3">
          <div>
            <h3 className="font-semibold">End-to-End Latency</h3>
            <p className="text-xs text-muted-foreground font-mono">Target: &lt;5s</p>
          </div>
          <Clock className="w-4 h-4 text-muted-foreground" />
        </div>
        <div className="text-4xl font-mono font-bold text-primary mb-3">
          {latency.total.toFixed(2)}
          <span className="text-base text-muted-foreground ml-1">s</span>
        </div>
        <div className="h-2 bg-muted rounded-full overflow-hidden mb-4">
          <motion.div
              className={cn(
                  "h-full rounded-full",
                  pct < 80 ? "bg-emerald-400" : pct < 100 ? "bg-amber-400" : "bg-red-400"
              )}
              animate={{ width: `${pct}%` }}
              transition={{ duration: 0.3 }}
          />
        </div>
        <div className="space-y-2">
          {[
            { label: "ESP32 Publish", value: latency.esp32, icon: Zap },
            { label: "ML Predict",    value: latency.ml,    icon: Cpu },
            { label: "LLM Reason",    value: latency.llm,   icon: Activity },
          ].map((s) => (
              <div key={s.label} className="flex items-center gap-2 text-xs">
                <s.icon className="w-3 h-3 text-muted-foreground shrink-0" />
                <span className="text-muted-foreground flex-1 font-mono">{s.label}</span>
                <span className="font-mono font-semibold">{s.value.toFixed(2)}s</span>
              </div>
          ))}
        </div>
      </div>
  );
}

// ─── AI Recommendation ───────────────────────────────────────────────────────

function AIRecommendation() {
  const { recommendation } = useSystemStore();

  return (
      <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-3">
          <h3 className="font-semibold">AI Recommendation</h3>
          <span className="text-xs font-mono bg-primary/10 text-primary border border-primary/20 px-2 py-0.5 rounded-full">
          OLLAMA · LLAMA3
        </span>
        </div>
        <AnimatePresence mode="wait">
          <motion.p
              key={recommendation.slice(0, 30)}
              initial={{ opacity: 0, y: 5 }}
              animate={{ opacity: 1, y: 0 }}
              className="text-sm text-muted-foreground leading-relaxed"
          >
            {recommendation}
          </motion.p>
        </AnimatePresence>
      </div>
  );
}

// ─── Dashboard Page ──────────────────────────────────────────────────────────

export default function DashboardPage() {
  useLiveData();
  const { status, nodeReadings } = useSystemStore();
  const cfg = STATUS_CONFIG[status] ?? STATUS_CONFIG.OFFLINE;

  return (
      <AppShell>
        <div className="p-4 md:p-6 space-y-5 max-w-screen-2xl mx-auto">

          {/* ── Status Banner ── */}
          <AnimatePresence mode="wait">
            <motion.div
                key={status}
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                className={cn(
                    "flex items-center gap-4 px-6 py-4 rounded-2xl border shadow-lg",
                    cfg.bg, cfg.glow
                )}
            >
              <div className={cn("w-3 h-3 rounded-full status-pulse shrink-0", cfg.dot)} />
              <div className="flex-1">
                <div className={cn("font-mono font-bold text-lg tracking-wider", cfg.text)}>
                  {cfg.label}
                </div>
                <div className="text-xs text-muted-foreground font-mono mt-0.5">
                  Last updated: {new Date().toLocaleTimeString()} · Copper Tailings Pipeline System
                </div>
              </div>
              <div className={cn("h-8 w-24 rounded-full bg-gradient-to-r opacity-30", cfg.bar)} />
            </motion.div>
          </AnimatePresence>

          {/* ── Node Cards ── */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            {nodeReadings.map((reading) => (
                <NodeCard key={reading.nodeId} reading={reading} />
            ))}
          </div>

          {/* ── Charts side by side ── */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <LivePressureChart />
            <LiveVelocityChart />
          </div>

          {/* ── Bottom row ── */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <LatencyMeter />
            <AIRecommendation />
            <AlertsFeed />
          </div>

        </div>
      </AppShell>
  );
}