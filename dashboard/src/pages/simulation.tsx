import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { AppShell } from "@/components/layout/AppShell";
import { useLiveData } from "@/hooks/useLiveData";
import { usePressureHistory } from "@/hooks/usePressureHistory";
import { useSystemStore } from "@/store/systemStore";
import { useAuthStore } from "@/store/authStore";
import { api } from "@/lib/api";
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from "recharts";
import { Zap, Cpu, Activity, ArrowRight, ChevronDown, Lock, AlertCircle } from "lucide-react";
import { cn } from "@/lib/utils";

const COLOR_CLASSES: Record<string, string> = {
  normal: "border-emerald-500/40 bg-emerald-500/10 hover:bg-emerald-500/20 text-emerald-400",
  leak: "border-red-500/40 bg-red-500/10 hover:bg-red-500/20 text-red-400",
  blockage: "border-amber-500/40 bg-amber-500/10 hover:bg-amber-500/20 text-amber-400",
  default: "border-blue-500/40 bg-blue-500/10 hover:bg-blue-500/20 text-blue-400",
};

const ACTIVE_CLASSES: Record<string, string> = {
  normal: "border-emerald-500 bg-emerald-500/25 text-emerald-300",
  leak: "border-red-500 bg-red-500/25 text-red-300",
  blockage: "border-amber-500 bg-amber-500/25 text-amber-300",
  default: "border-blue-500 bg-blue-500/25 text-blue-300",
};

function getColor(name: string) {
  const n = name.toLowerCase();
  if (n.includes("normal") || n.includes("base")) return "normal";
  if (n.includes("leak")) return "leak";
  if (n.includes("block")) return "blockage";
  return "default";
}

function SimulationChart() {
  const history = usePressureHistory();
  const data = history.slice(-30);
  return (
      <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold">Live Simulation Chart</h3>
          <div className="text-xs font-mono text-emerald-400 flex items-center gap-1.5">
            <span className="w-1.5 h-1.5 bg-emerald-400 rounded-full status-pulse" />LIVE
          </div>
        </div>
        <div className="h-52">
          <ResponsiveContainer width="100%" height="100%">
            <LineChart data={data} margin={{ top: 5, right: 5, bottom: 5, left: 0 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" opacity={0.5} />
              <XAxis dataKey="time" tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }} tickLine={false} axisLine={false} interval={9} />
              <YAxis tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }} tickLine={false} axisLine={false} tickFormatter={(v) => (v / 1000).toFixed(0) + "k"} />
              <Tooltip contentStyle={{ background: "hsl(var(--card))", border: "1px solid hsl(var(--border))", borderRadius: "12px", fontFamily: "Space Mono", fontSize: "11px" }} formatter={(v: number) => [`${Math.round(v).toLocaleString()} Pa`, ""]} />
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

function AccessDenied() {
  return (
      <AppShell>
        <div className="flex items-center justify-center min-h-[70vh] px-6">
          <motion.div initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} className="text-center max-w-sm">
            <div className="w-20 h-20 rounded-2xl bg-destructive/10 border border-destructive/30 flex items-center justify-center mx-auto mb-6">
              <Lock className="w-10 h-10 text-destructive" />
            </div>
            <h2 className="text-xl font-bold mb-2">Access Restricted</h2>
            <p className="text-muted-foreground text-sm mb-4 leading-relaxed">
              The Simulation module is only available to <span className="text-primary font-semibold font-mono">OPERATOR</span> accounts.
            </p>
            <div className="bg-card border border-card-border rounded-xl p-4 text-left space-y-2">
              <div className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-2">Your permissions</div>
              {[
                { label: "Dashboard monitoring", allowed: true },
                { label: "Historical analytics", allowed: true },
                { label: "AI recommendations", allowed: true },
                { label: "Simulation & fault injection", allowed: false },
                { label: "User management", allowed: false },
              ].map((p) => (
                  <div key={p.label} className="flex items-center gap-2 text-xs">
                <span className={cn("w-4 h-4 rounded-full flex items-center justify-center shrink-0", p.allowed ? "bg-emerald-400/20 text-emerald-400" : "bg-destructive/20 text-destructive")}>
                  {p.allowed ? "✓" : "✕"}
                </span>
                    <span className={p.allowed ? "text-foreground" : "text-muted-foreground line-through"}>{p.label}</span>
                  </div>
              ))}
            </div>
          </motion.div>
        </div>
      </AppShell>
  );
}

export default function SimulationPage() {
  const { user } = useAuthStore();
  useLiveData();
  const { latency } = useSystemStore();

  const [scenarios, setScenarios] = useState<string[]>([]);
  const [activeScenario, setActiveScenario] = useState<string>("");
  const [faultClass, setFaultClass] = useState("LEAK_MODERATE");
  const [severity, setSeverity] = useState("HIGH");
  const [duration, setDuration] = useState("30");
  const [injecting, setInjecting] = useState(false);
  const [runningScenario, setRunningScenario] = useState(false);
  const [feedback, setFeedback] = useState<{ type: "success" | "error"; msg: string } | null>(null);

  useEffect(() => {
    api.get("/api/simulate/scenarios")
        .then(({ data }) => {
          const list: string[] = Array.isArray(data)
              ? data.map((s: any) => s.name ?? s.id ?? s)
              : data?.scenarios?.map((s: any) => s.name ?? s.id ?? s) ?? [];
          setScenarios(list);
          if (list.length > 0) setFaultClass(list[0]);
        })
        .catch(() => {
          setScenarios(["NORMAL_BASELINE", "LEAK_INCIPIENT", "LEAK_MODERATE", "LEAK_CRITICAL", "BLOCKAGE_25", "BLOCKAGE_50", "BLOCKAGE_75"]);
        });
  }, []);

  if (user?.role !== "OPERATOR") return <AccessDenied />;

  const handleScenario = async (name: string) => {
    setActiveScenario(name);
    setRunningScenario(true);
    setFeedback(null);
    try {
      await api.post(`/api/simulate/scenario/${name}`);
      setFeedback({ type: "success", msg: `Scenario "${name}" activated successfully.` });
    } catch (err: any) {
      setFeedback({ type: "error", msg: err.response?.data?.message ?? err.response?.data?.detail ?? "Failed to activate scenario." });
    } finally {
      setRunningScenario(false);
    }
  };

  const handleInject = async () => {
    setInjecting(true);
    setFeedback(null);
    try {
      await api.post("/api/simulate/inject-fault", {
        faultClass,
        severity,
        duration: parseInt(duration),
      });
      setFeedback({ type: "success", msg: `Fault injected: ${faultClass} · ${severity} for ${duration}s` });
    } catch (err: any) {
      setFeedback({ type: "error", msg: err.response?.data?.message ?? err.response?.data?.detail ?? "Fault injection failed." });
    } finally {
      setInjecting(false);
    }
  };

  const STEPS = [
    { label: "ESP32 Publish", value: latency.esp32, icon: Zap, color: "text-cyan-400" },
    { label: "ML Predict", value: latency.ml, icon: Cpu, color: "text-purple-400" },
    { label: "LLM Reason", value: latency.llm, icon: Activity, color: "text-blue-400" },
  ];

  const displayScenarios = scenarios.length > 0 ? scenarios : ["NORMAL_BASELINE", "LEAK_INCIPIENT", "LEAK_MODERATE", "LEAK_CRITICAL", "BLOCKAGE_25", "BLOCKAGE_50", "BLOCKAGE_75"];

  return (
      <AppShell>
        <div className="p-4 md:p-6 space-y-5 max-w-screen-2xl mx-auto">
          <div>
            <h1 className="text-xl font-bold">Simulation Control</h1>
            <p className="text-sm text-muted-foreground font-mono mt-0.5">Inject scenarios and test system response via backend API</p>
          </div>

          {feedback && (
              <motion.div initial={{ opacity: 0, y: -8 }} animate={{ opacity: 1, y: 0 }} className={cn("flex items-center gap-3 px-4 py-3 rounded-xl border text-sm", feedback.type === "success" ? "bg-emerald-500/10 border-emerald-500/30 text-emerald-400" : "bg-destructive/10 border-destructive/30 text-destructive")}>
                <AlertCircle className="w-4 h-4 shrink-0" />
                {feedback.msg}
              </motion.div>
          )}

          <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold">Scenario Grid</h3>
              {runningScenario && <div className="text-xs font-mono text-primary flex items-center gap-1.5"><div className="w-3 h-3 border border-primary border-t-transparent rounded-full animate-spin" />Activating...</div>}
            </div>
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-3">
              {displayScenarios.map((name) => {
                const colorKey = getColor(name);
                const isActive = activeScenario === name;
                return (
                    <motion.button key={name} whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.96 }} onClick={() => handleScenario(name)} disabled={runningScenario}
                                   className={cn("flex flex-col items-start p-4 rounded-xl border-2 text-left transition-all disabled:opacity-60", isActive ? ACTIVE_CLASSES[colorKey] : COLOR_CLASSES[colorKey])}>
                      <span className="text-xs font-mono font-bold tracking-wider mb-1">{name.replace(/_/g, " ")}</span>
                      {isActive && <span className="mt-2 text-xs font-mono bg-white/10 px-2 py-0.5 rounded-full">ACTIVE</span>}
                    </motion.button>
                );
              })}
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
              <h3 className="font-semibold mb-4">Manual Fault Injection</h3>
              <div className="space-y-4">
                <div>
                  <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">Fault Class</label>
                  <div className="relative">
                    <select value={faultClass} onChange={(e) => setFaultClass(e.target.value)} className="w-full px-3 py-2.5 pr-8 bg-background border border-input rounded-xl text-sm appearance-none focus:outline-none focus:ring-2 focus:ring-primary/50">
                      {displayScenarios.map(s => <option key={s} value={s}>{s.replace(/_/g, " ")}</option>)}
                    </select>
                    <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 pointer-events-none text-muted-foreground" />
                  </div>
                </div>
                <div>
                  <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">Severity</label>
                  <div className="relative">
                    <select value={severity} onChange={(e) => setSeverity(e.target.value)} className="w-full px-3 py-2.5 pr-8 bg-background border border-input rounded-xl text-sm appearance-none focus:outline-none focus:ring-2 focus:ring-primary/50">
                      {["LOW", "MEDIUM", "HIGH", "CRITICAL"].map(s => <option key={s}>{s}</option>)}
                    </select>
                    <ChevronDown className="absolute right-3 top-1/2 -translate-y-1/2 w-4 h-4 pointer-events-none text-muted-foreground" />
                  </div>
                </div>
                <div>
                  <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">Duration (seconds)</label>
                  <input type="number" value={duration} onChange={(e) => setDuration(e.target.value)} min="5" max="300" className="w-full px-3 py-2.5 bg-background border border-input rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
                </div>
                <motion.button whileHover={{ scale: 1.02 }} whileTap={{ scale: 0.97 }} onClick={handleInject} disabled={injecting}
                               className="w-full py-3 bg-destructive text-destructive-foreground rounded-xl font-semibold flex items-center justify-center gap-2 hover:brightness-110 transition-all disabled:opacity-60">
                  {injecting ? <><div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />INJECTING...</> : <>INJECT FAULT<ArrowRight className="w-4 h-4" /></>}
                </motion.button>
              </div>
            </div>

            <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
              <div className="flex items-center justify-between mb-4">
                <h3 className="font-semibold">End-to-End Latency Proof</h3>
                <span className={cn("text-xs font-mono px-2 py-0.5 rounded-full font-semibold", latency.total < 5 ? "bg-emerald-400/10 text-emerald-400" : "bg-red-400/10 text-red-400")}>
                {latency.total < 5 ? "UNDER 5s" : "OVER 5s"}
              </span>
              </div>
              <div className="space-y-3 mb-5">
                {STEPS.map((step, i) => (
                    <div key={step.label}>
                      <div className="flex items-center gap-3 mb-1.5">
                        <step.icon className={cn("w-3.5 h-3.5 shrink-0", step.color)} />
                        <span className="text-xs font-mono text-muted-foreground flex-1">{step.label}</span>
                        <span className="text-xs font-mono font-semibold">{step.value.toFixed(2)}s</span>
                      </div>
                      <div className="h-1.5 bg-muted rounded-full overflow-hidden">
                        <motion.div className={cn("h-full rounded-full", step.color.replace("text-", "bg-"))} animate={{ width: `${(step.value / 5) * 100}%` }} transition={{ duration: 0.3 }} />
                      </div>
                      {i < STEPS.length - 1 && <div className="flex justify-center my-1"><ArrowRight className="w-3 h-3 text-muted-foreground" /></div>}
                    </div>
                ))}
              </div>
              <div className="border-t border-border pt-4">
                <div className="flex items-center justify-between">
                  <span className="text-sm font-mono font-bold">TOTAL</span>
                  <motion.span key={latency.total.toFixed(1)} initial={{ opacity: 0, scale: 0.9 }} animate={{ opacity: 1, scale: 1 }} className="text-2xl font-mono font-bold text-primary">
                    {latency.total.toFixed(2)}s
                  </motion.span>
                </div>
                <div className="h-2 bg-muted rounded-full overflow-hidden mt-2">
                  <motion.div className={cn("h-full rounded-full", latency.total < 5 ? "bg-emerald-400" : "bg-red-400")} animate={{ width: `${Math.min((latency.total / 5) * 100, 100)}%` }} transition={{ duration: 0.3 }} />
                </div>
                <div className="flex justify-between text-xs font-mono text-muted-foreground mt-1"><span>0s</span><span>5s (target)</span></div>
              </div>
            </div>
          </div>

          <SimulationChart />
        </div>
      </AppShell>
  );
}
