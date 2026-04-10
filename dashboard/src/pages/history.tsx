import { useState, useEffect } from "react";
import { motion } from "framer-motion";
import { AppShell } from "@/components/layout/AppShell";
import { api } from "@/lib/api";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from "recharts";
import { Filter, ChevronLeft, ChevronRight, RefreshCw } from "lucide-react";
import { cn } from "@/lib/utils";

const PAGE_SIZE = 8;

const SEVERITY_COLORS = {
  LOW: "text-emerald-400 bg-emerald-400/10",
  MEDIUM: "text-amber-400 bg-amber-400/10",
  HIGH: "text-orange-400 bg-orange-400/10",
  CRITICAL: "text-red-400 bg-red-400/10",
};

export default function HistoryPage() {
  const [alerts, setAlerts] = useState<any[]>([]);
  const [pressureHistory, setPressureHistory] = useState<any[]>([]);
  const [summary, setSummary] = useState<any>(null);
  const [faultDist, setFaultDist] = useState<any[]>([]);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [from, setFrom] = useState(() => { const d = new Date(); d.setDate(d.getDate() - 14); return d.toISOString().split("T")[0]; });
  const [to, setTo] = useState(() => new Date().toISOString().split("T")[0]);

  async function fetchAll() {
    setLoading(true);
    const [alertsRes, historyRes, summaryRes, distRes] = await Promise.allSettled([
      api.get("/api/alerts/history"),
      api.get("/api/sensors/readings/history"),
      api.get("/api/analytics/summary"),
      api.get("/api/analytics/fault-distribution"),
    ]);

    if (alertsRes.status === "fulfilled") {
      const d = alertsRes.value.data;
      setAlerts(Array.isArray(d) ? d : d?.alerts ?? d?.data ?? []);
    }
    if (historyRes.status === "fulfilled") {
      const d = historyRes.value.data;
      const raw = Array.isArray(d) ? d : d?.history ?? d?.readings ?? d?.data ?? [];
      const grouped: Record<string, { A: number; B: number; C: number }> = {};
      raw.forEach((r: any) => {
        const ts = r.timestamp ?? r.createdAt ?? "";
        const time = ts ? new Date(ts).toLocaleTimeString() : "—";
        const id = (r.nodeId ?? r.node_id ?? r.id ?? "A").toString().toUpperCase();
        const pressure = parseFloat(r.pressure ?? r.value ?? 0);
        if (!grouped[time]) grouped[time] = { A: 0, B: 0, C: 0 };
        if (id.includes("A")) grouped[time].A = pressure;
        else if (id.includes("B")) grouped[time].B = pressure;
        else if (id.includes("C")) grouped[time].C = pressure;
      });
      setPressureHistory(Object.entries(grouped).slice(-14).map(([time, v]) => ({ time, nodeA: v.A, nodeB: v.B, nodeC: v.C })));
    }
    if (summaryRes.status === "fulfilled") setSummary(summaryRes.value.data);
    if (distRes.status === "fulfilled") {
      const d = distRes.value.data;
      const list = Array.isArray(d) ? d : d?.distribution ?? d?.data ?? [];
      setFaultDist(list.map((item: any) => ({
        name: item.faultClass ?? item.fault_class ?? item.name ?? item.label ?? "Unknown",
        value: item.count ?? item.value ?? item.total ?? 0,
        color: item.faultClass?.toLowerCase().includes("leak") ? "#f87171"
            : item.faultClass?.toLowerCase().includes("block") ? "#fbbf24"
                : "#34d399",
      })));
    }
    setLoading(false);
  }

  useEffect(() => { fetchAll(); }, []);

  const pagedAlerts = alerts.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);
  const totalPages = Math.ceil(alerts.length / PAGE_SIZE);

  const kpi = {
    total: summary?.totalReadings ?? summary?.total ?? alerts.length,
    leaks: summary?.leakCount ?? summary?.leaks ?? faultDist.find(d => d.name.toLowerCase().includes("leak"))?.value ?? 0,
    blockages: summary?.blockageCount ?? summary?.blockages ?? faultDist.find(d => d.name.toLowerCase().includes("block"))?.value ?? 0,
    avgLatency: (summary?.avgLatency ?? summary?.averageLatency ?? 0).toFixed(2),
  };

  return (
      <AppShell>
        <div className="p-4 md:p-6 space-y-5 max-w-screen-2xl mx-auto">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-xl font-bold">History & Analytics</h1>
              <p className="text-sm text-muted-foreground font-mono mt-0.5">Live data from backend</p>
            </div>
            <motion.button whileTap={{ scale: 0.95 }} onClick={fetchAll} className="flex items-center gap-2 px-4 py-2 bg-muted rounded-xl text-sm font-medium hover:bg-muted/80 transition-colors">
              <RefreshCw className={cn("w-4 h-4", loading && "animate-spin")} />Refresh
            </motion.button>
          </div>

          <div className="bg-card border border-card-border rounded-2xl p-4 flex flex-wrap items-end gap-4">
            <div>
              <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">From</label>
              <input type="date" value={from} onChange={(e) => setFrom(e.target.value)} className="px-3 py-2 bg-background border border-input rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
            </div>
            <div>
              <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">To</label>
              <input type="date" value={to} onChange={(e) => setTo(e.target.value)} className="px-3 py-2 bg-background border border-input rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/50" />
            </div>
            <motion.button whileTap={{ scale: 0.95 }} onClick={fetchAll} className="px-5 py-2 bg-primary text-primary-foreground rounded-xl font-semibold text-sm flex items-center gap-2 hover:brightness-110 transition-all">
              <Filter className="w-4 h-4" />APPLY
            </motion.button>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            {[
              { label: "Total Events", value: String(kpi.total), color: "text-primary", bg: "bg-primary/10" },
              { label: "Leak Events", value: String(kpi.leaks), color: "text-red-400", bg: "bg-red-400/10" },
              { label: "Blockage Events", value: String(kpi.blockages), color: "text-amber-400", bg: "bg-amber-400/10" },
              { label: "Avg Latency", value: `${kpi.avgLatency}s`, color: "text-emerald-400", bg: "bg-emerald-400/10" },
            ].map((k) => (
                <motion.div key={k.label} initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} className={cn("rounded-2xl p-5 border border-card-border", k.bg)}>
                  <div className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-2">{k.label}</div>
                  <div className={cn("text-3xl font-mono font-bold", k.color)}>{loading ? "—" : k.value}</div>
                </motion.div>
            ))}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
              <h3 className="font-semibold mb-4">Fault Distribution</h3>
              <div className="h-52">
                {faultDist.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <PieChart>
                        <Pie data={faultDist} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={3} dataKey="value">
                          {faultDist.map((entry, i) => <Cell key={i} fill={entry.color} />)}
                        </Pie>
                        <Legend iconType="circle" wrapperStyle={{ fontSize: "12px", fontFamily: "Space Mono" }} />
                        <Tooltip contentStyle={{ background: "hsl(var(--card))", border: "1px solid hsl(var(--border))", borderRadius: "12px", fontFamily: "Space Mono", fontSize: "11px" }} />
                      </PieChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="h-full flex items-center justify-center text-sm text-muted-foreground font-mono">{loading ? "Loading..." : "No data available"}</div>
                )}
              </div>
            </div>

            <div className="md:col-span-2 bg-card border border-card-border rounded-2xl p-5 shadow-sm">
              <h3 className="font-semibold mb-4">Pressure History</h3>
              <div className="h-52">
                {pressureHistory.length > 0 ? (
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={pressureHistory} margin={{ top: 5, right: 5, bottom: 5, left: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" opacity={0.4} />
                        <XAxis dataKey="time" tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }} tickLine={false} axisLine={false} />
                        <YAxis tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }} tickLine={false} axisLine={false} tickFormatter={(v) => (v / 1000).toFixed(0) + "k"} />
                        <Tooltip contentStyle={{ background: "hsl(var(--card))", border: "1px solid hsl(var(--border))", borderRadius: "12px", fontFamily: "Space Mono", fontSize: "11px" }} formatter={(v: number) => [`${Math.round(v).toLocaleString()} Pa`, ""]} />
                        <Legend wrapperStyle={{ fontSize: "11px", fontFamily: "Space Mono" }} />
                        <Line type="monotone" dataKey="nodeA" stroke="#60a5fa" strokeWidth={2} dot={false} name="Node A" />
                        <Line type="monotone" dataKey="nodeB" stroke="#a78bfa" strokeWidth={2} dot={false} name="Node B" />
                        <Line type="monotone" dataKey="nodeC" stroke="#22d3ee" strokeWidth={2} dot={false} name="Node C" />
                      </LineChart>
                    </ResponsiveContainer>
                ) : (
                    <div className="h-full flex items-center justify-center text-sm text-muted-foreground font-mono">{loading ? "Loading..." : "No history data"}</div>
                )}
              </div>
            </div>
          </div>

          <div className="bg-card border border-card-border rounded-2xl shadow-sm overflow-hidden">
            <div className="px-5 py-4 border-b border-border flex items-center justify-between">
              <h3 className="font-semibold">Alert Records</h3>
              <span className="text-xs text-muted-foreground font-mono">{alerts.length} total entries</span>
            </div>
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                <tr className="border-b border-border bg-muted/30">
                  {["ID", "Fault Class", "Severity", "Confidence", "Timestamp"].map((h) => (
                      <th key={h} className="text-left px-5 py-3 text-xs font-mono text-muted-foreground uppercase tracking-wider">{h}</th>
                  ))}
                </tr>
                </thead>
                <tbody>
                {loading ? (
                    <tr><td colSpan={5} className="px-5 py-8 text-center text-sm text-muted-foreground font-mono">Loading from backend...</td></tr>
                ) : pagedAlerts.length === 0 ? (
                    <tr><td colSpan={5} className="px-5 py-8 text-center text-sm text-muted-foreground font-mono">No alert records found</td></tr>
                ) : (
                    pagedAlerts.map((alert, i) => {
                      const sev = (alert.severity ?? "MEDIUM").toUpperCase() as keyof typeof SEVERITY_COLORS;
                      return (
                          <motion.tr key={alert.id ?? alert._id ?? i} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: i * 0.03 }} className="border-b border-border/50 hover:bg-muted/20 transition-colors">
                            <td className="px-5 py-3 font-mono text-xs text-muted-foreground">#{(alert.id ?? alert._id ?? String(i + 1)).toString().padStart(4, "0").slice(-4)}</td>
                            <td className="px-5 py-3 font-mono text-xs">{alert.faultClass ?? alert.fault_class ?? alert.type ?? "UNKNOWN"}</td>
                            <td className="px-5 py-3">
                              <span className={cn("text-xs font-mono font-semibold px-2 py-0.5 rounded-full", SEVERITY_COLORS[sev] ?? "text-muted-foreground bg-muted")}>{sev}</span>
                            </td>
                            <td className="px-5 py-3 font-mono text-xs">{((parseFloat(alert.confidence ?? alert.score ?? 0.85)) * 100).toFixed(1)}%</td>
                            <td className="px-5 py-3 font-mono text-xs text-muted-foreground">{alert.timestamp ? new Date(alert.timestamp).toLocaleString() : "—"}</td>
                          </motion.tr>
                      );
                    })
                )}
                </tbody>
              </table>
            </div>
            {!loading && totalPages > 1 && (
                <div className="px-5 py-3 flex items-center justify-between border-t border-border">
                  <span className="text-xs text-muted-foreground font-mono">Page {page + 1} of {totalPages}</span>
                  <div className="flex gap-2">
                    <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="p-1.5 rounded-lg hover:bg-muted disabled:opacity-40"><ChevronLeft className="w-4 h-4" /></button>
                    <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1} className="p-1.5 rounded-lg hover:bg-muted disabled:opacity-40"><ChevronRight className="w-4 h-4" /></button>
                  </div>
                </div>
            )}
          </div>
        </div>
      </AppShell>
  );
}
