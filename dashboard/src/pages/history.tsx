import { useState, useMemo } from "react";
import { motion } from "framer-motion";
import { AppShell } from "@/components/layout/AppShell";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend
} from "recharts";
import { Filter, ChevronLeft, ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

function generateHistory(days = 30) {
  const data = [];
  for (let i = days - 1; i >= 0; i--) {
    const d = new Date();
    d.setDate(d.getDate() - i);
    const normal = 20 + Math.floor(Math.random() * 60);
    const leak = Math.floor(Math.random() * 8);
    const blockage = Math.floor(Math.random() * 5);
    data.push({
      date: d.toLocaleDateString("en-GB", { month: "short", day: "numeric" }),
      fullDate: d.toISOString().split("T")[0],
      normal, leak, blockage,
      latency: 1.5 + Math.random() * 2.5,
      pressureA: 101325 + (Math.random() - 0.5) * 3000,
      pressureB: 98500 + (Math.random() - 0.5) * 3000,
      pressureC: 95800 + (Math.random() - 0.5) * 3000,
    });
  }
  return data;
}

function generateAlerts(count = 50) {
  const faults = ["NORMAL_BASELINE", "LEAK_INCIPIENT", "LEAK_MODERATE", "LEAK_CRITICAL", "BLOCKAGE_25", "BLOCKAGE_50", "BLOCKAGE_75"];
  const severities = ["LOW", "MEDIUM", "HIGH", "CRITICAL"] as const;
  return Array.from({ length: count }, (_, i) => ({
    id: String(i + 1).padStart(4, "0"),
    faultClass: faults[Math.floor(Math.random() * faults.length)],
    severity: severities[Math.floor(Math.random() * severities.length)],
    confidence: 0.65 + Math.random() * 0.34,
    timestamp: new Date(Date.now() - Math.random() * 7 * 86400000).toISOString(),
  }));
}

const HISTORY = generateHistory();
const ALERTS = generateAlerts();

const SEVERITY_COLORS = {
  LOW: "text-emerald-400 bg-emerald-400/10",
  MEDIUM: "text-amber-400 bg-amber-400/10",
  HIGH: "text-orange-400 bg-orange-400/10",
  CRITICAL: "text-red-400 bg-red-400/10",
};

const PAGE_SIZE = 8;

export default function HistoryPage() {
  const [from, setFrom] = useState(() => {
    const d = new Date(); d.setDate(d.getDate() - 14);
    return d.toISOString().split("T")[0];
  });
  const [to, setTo] = useState(() => new Date().toISOString().split("T")[0]);
  const [filtered, setFiltered] = useState(HISTORY);
  const [page, setPage] = useState(0);

  const handleGo = () => {
    setFiltered(HISTORY.filter(d => d.fullDate >= from && d.fullDate <= to));
    setPage(0);
  };

  const kpi = useMemo(() => ({
    normal: filtered.reduce((s, d) => s + d.normal, 0),
    leak: filtered.reduce((s, d) => s + d.leak, 0),
    blockage: filtered.reduce((s, d) => s + d.blockage, 0),
    avgLatency: (filtered.reduce((s, d) => s + d.latency, 0) / (filtered.length || 1)).toFixed(2),
  }), [filtered]);

  const pieData = [
    { name: "Normal", value: kpi.normal, color: "#34d399" },
    { name: "Leak", value: kpi.leak, color: "#f87171" },
    { name: "Blockage", value: kpi.blockage, color: "#fbbf24" },
  ];

  const pagedAlerts = ALERTS.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);
  const totalPages = Math.ceil(ALERTS.length / PAGE_SIZE);

  return (
    <AppShell>
      <div className="p-4 md:p-6 space-y-5 max-w-screen-2xl mx-auto">
        <div>
          <h1 className="text-xl font-bold">History & Analytics</h1>
          <p className="text-sm text-muted-foreground font-mono mt-0.5">Historical pipeline data analysis</p>
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
          <motion.button whileTap={{ scale: 0.95 }} onClick={handleGo} className="px-5 py-2 bg-primary text-primary-foreground rounded-xl font-semibold text-sm flex items-center gap-2 hover:brightness-110 transition-all">
            <Filter className="w-4 h-4" />APPLY
          </motion.button>
          <div className="text-xs text-muted-foreground font-mono ml-auto">Showing {filtered.length} days of data</div>
        </div>

        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          {[
            { label: "Normal Events", value: kpi.normal.toLocaleString(), color: "text-emerald-400", bg: "bg-emerald-400/10" },
            { label: "Leak Events", value: kpi.leak.toLocaleString(), color: "text-red-400", bg: "bg-red-400/10" },
            { label: "Blockage Events", value: kpi.blockage.toLocaleString(), color: "text-amber-400", bg: "bg-amber-400/10" },
            { label: "Avg Latency", value: `${kpi.avgLatency}s`, color: "text-primary", bg: "bg-primary/10" },
          ].map((k) => (
            <motion.div key={k.label} initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} className={cn("rounded-2xl p-5 border border-card-border", k.bg)}>
              <div className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-2">{k.label}</div>
              <div className={cn("text-3xl font-mono font-bold", k.color)}>{k.value}</div>
            </motion.div>
          ))}
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-card border border-card-border rounded-2xl p-5 shadow-sm">
            <h3 className="font-semibold mb-4">Fault Distribution</h3>
            <div className="h-52">
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie data={pieData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={3} dataKey="value">
                    {pieData.map((entry, i) => <Cell key={i} fill={entry.color} />)}
                  </Pie>
                  <Legend iconType="circle" wrapperStyle={{ fontSize: "12px", fontFamily: "Space Mono" }} />
                  <Tooltip contentStyle={{ background: "hsl(var(--card))", border: "1px solid hsl(var(--border))", borderRadius: "12px", fontFamily: "Space Mono", fontSize: "11px" }} />
                </PieChart>
              </ResponsiveContainer>
            </div>
          </div>

          <div className="md:col-span-2 bg-card border border-card-border rounded-2xl p-5 shadow-sm">
            <h3 className="font-semibold mb-4">Pressure History</h3>
            <div className="h-52">
              <ResponsiveContainer width="100%" height="100%">
                <LineChart data={filtered.slice(-14)} margin={{ top: 5, right: 5, bottom: 5, left: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" stroke="hsl(var(--border))" opacity={0.4} />
                  <XAxis dataKey="date" tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }} tickLine={false} axisLine={false} />
                  <YAxis tick={{ fontSize: 10, fill: "hsl(var(--muted-foreground))", fontFamily: "Space Mono" }} tickLine={false} axisLine={false} tickFormatter={(v) => (v / 1000).toFixed(0) + "k"} />
                  <Tooltip contentStyle={{ background: "hsl(var(--card))", border: "1px solid hsl(var(--border))", borderRadius: "12px", fontFamily: "Space Mono", fontSize: "11px" }} formatter={(v: number) => [`${Math.round(v).toLocaleString()} Pa`, ""]} />
                  <Legend wrapperStyle={{ fontSize: "11px", fontFamily: "Space Mono" }} />
                  <Line type="monotone" dataKey="pressureA" stroke="#60a5fa" strokeWidth={2} dot={false} name="Node A" />
                  <Line type="monotone" dataKey="pressureB" stroke="#a78bfa" strokeWidth={2} dot={false} name="Node B" />
                  <Line type="monotone" dataKey="pressureC" stroke="#22d3ee" strokeWidth={2} dot={false} name="Node C" />
                </LineChart>
              </ResponsiveContainer>
            </div>
          </div>
        </div>

        <div className="bg-card border border-card-border rounded-2xl shadow-sm overflow-hidden">
          <div className="px-5 py-4 border-b border-border flex items-center justify-between">
            <h3 className="font-semibold">Alert Records</h3>
            <span className="text-xs text-muted-foreground font-mono">{ALERTS.length} total entries</span>
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
                {pagedAlerts.map((alert, i) => (
                  <motion.tr key={alert.id} initial={{ opacity: 0 }} animate={{ opacity: 1 }} transition={{ delay: i * 0.03 }} className="border-b border-border/50 hover:bg-muted/20 transition-colors">
                    <td className="px-5 py-3 font-mono text-xs text-muted-foreground">#{alert.id}</td>
                    <td className="px-5 py-3 font-mono text-xs">{alert.faultClass}</td>
                    <td className="px-5 py-3">
                      <span className={cn("text-xs font-mono font-semibold px-2 py-0.5 rounded-full", SEVERITY_COLORS[alert.severity])}>{alert.severity}</span>
                    </td>
                    <td className="px-5 py-3 font-mono text-xs">{(alert.confidence * 100).toFixed(1)}%</td>
                    <td className="px-5 py-3 font-mono text-xs text-muted-foreground">{new Date(alert.timestamp).toLocaleString()}</td>
                  </motion.tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="px-5 py-3 flex items-center justify-between border-t border-border">
            <span className="text-xs text-muted-foreground font-mono">Page {page + 1} of {totalPages}</span>
            <div className="flex gap-2">
              <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className="p-1.5 rounded-lg hover:bg-muted disabled:opacity-40 transition-colors"><ChevronLeft className="w-4 h-4" /></button>
              <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1} className="p-1.5 rounded-lg hover:bg-muted disabled:opacity-40 transition-colors"><ChevronRight className="w-4 h-4" /></button>
            </div>
          </div>
        </div>
      </div>
    </AppShell>
  );
}
