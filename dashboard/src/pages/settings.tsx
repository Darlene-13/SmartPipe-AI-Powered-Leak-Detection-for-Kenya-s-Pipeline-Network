import { motion } from "framer-motion";
import { AppShell } from "@/components/layout/AppShell";
import { useThemeStore } from "@/store/themeStore";
import { useSettingsStore } from "@/store/settingsStore";
import { useAuthStore } from "@/store/authStore";
import { Switch } from "@/components/ui/switch";
import { cn } from "@/lib/utils";
import { User, Sun, Moon, Server, Users } from "lucide-react";

const MOCK_USERS = [
  { id: "1", username: "admin", firstName: "System", lastName: "Administrator", role: "OPERATOR" },
  { id: "2", username: "operator01", firstName: "John", lastName: "Kamau", role: "OPERATOR" },
  { id: "3", username: "viewer01", firstName: "Mary", lastName: "Ochieng", role: "VIEWER" },
  { id: "4", username: "viewer02", firstName: "Peter", lastName: "Njoroge", role: "VIEWER" },
];

function SettingRow({ label, description, children }: { label: string; description?: string; children: React.ReactNode }) {
  return (
    <div className="flex items-center justify-between py-4 border-b border-border/60 last:border-0">
      <div>
        <div className="text-sm font-medium">{label}</div>
        {description && <div className="text-xs text-muted-foreground mt-0.5">{description}</div>}
      </div>
      {children}
    </div>
  );
}

export default function SettingsPage() {
  const { theme, toggleTheme } = useThemeStore();
  const { liveChartUpdates, soundAlerts, wsEnabled, backendUrl, mlServiceUrl, mqttUrl, redisUrl, toggleLiveChartUpdates, toggleSoundAlerts, toggleWs } = useSettingsStore();
  const { user } = useAuthStore();

  const initials = user
    ? `${(user.firstName?.[0] ?? user.username[0])}${user.lastName?.[0] ?? ""}`.toUpperCase()
    : "OP";

  return (
    <AppShell>
      <div className="p-4 md:p-6 space-y-5 max-w-3xl mx-auto">
        <div>
          <h1 className="text-xl font-bold">Settings</h1>
          <p className="text-sm text-muted-foreground font-mono mt-0.5">System configuration and preferences</p>
        </div>

        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="bg-card border border-card-border rounded-2xl p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-4">
            <User className="w-4 h-4 text-muted-foreground" />
            <h2 className="font-semibold">User Profile</h2>
          </div>
          <div className="flex items-center gap-4">
            <div className="w-16 h-16 rounded-2xl bg-primary/20 border-2 border-primary/40 flex items-center justify-center">
              <span className="text-xl font-mono font-bold text-primary">{initials}</span>
            </div>
            <div>
              <div className="font-semibold text-lg">{user?.firstName ?? ""} {user?.lastName ?? ""}</div>
              <div className="text-sm text-muted-foreground font-mono">@{user?.username ?? "—"}</div>
              <div className="mt-1.5">
                <span className={cn(
                  "text-xs font-mono font-semibold px-2 py-0.5 rounded-full",
                  user?.role === "OPERATOR"
                    ? "bg-primary/10 text-primary border border-primary/30"
                    : "bg-muted text-muted-foreground border border-border"
                )}>
                  {user?.role ?? "VIEWER"}
                </span>
              </div>
            </div>
          </div>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.1 }} className="bg-card border border-card-border rounded-2xl p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-2">
            {theme === "dark" ? <Moon className="w-4 h-4 text-muted-foreground" /> : <Sun className="w-4 h-4 text-muted-foreground" />}
            <h2 className="font-semibold">Display Settings</h2>
          </div>
          <div>
            <SettingRow label="Dark Mode" description="Switch between dark and light interface theme">
              <div className="flex items-center gap-2">
                <Sun className="w-3.5 h-3.5 text-muted-foreground" />
                <Switch checked={theme === "dark"} onCheckedChange={toggleTheme} />
                <Moon className="w-3.5 h-3.5 text-muted-foreground" />
              </div>
            </SettingRow>
            <SettingRow label="Live Chart Updates" description="Real-time pressure chart data refresh every 500ms">
              <Switch checked={liveChartUpdates} onCheckedChange={toggleLiveChartUpdates} />
            </SettingRow>
            <SettingRow label="Sound Alerts" description="Play audio notification when fault is detected">
              <Switch checked={soundAlerts} onCheckedChange={toggleSoundAlerts} />
            </SettingRow>
            <SettingRow label="WebSocket Feed" description="Real-time alerts via WebSocket connection">
              <Switch checked={wsEnabled} onCheckedChange={toggleWs} />
            </SettingRow>
          </div>
        </motion.div>

        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.15 }} className="bg-card border border-card-border rounded-2xl p-6 shadow-sm">
          <div className="flex items-center gap-3 mb-4">
            <Server className="w-4 h-4 text-muted-foreground" />
            <h2 className="font-semibold">System Information</h2>
          </div>
          <div className="space-y-3">
            {[
              { label: "Backend URL", value: backendUrl },
              { label: "ML Service URL", value: mlServiceUrl },
              { label: "MQTT Broker URL", value: mqttUrl },
              { label: "Redis URL", value: redisUrl },
              { label: "Frontend Version", value: "v1.0.0-ENM2026" },
              { label: "Model", value: "OLLAMA · LLaMA3.2:3b" },
            ].map((item) => (
              <div key={item.label} className="flex items-center justify-between py-2 border-b border-border/40 last:border-0">
                <span className="text-sm text-muted-foreground">{item.label}</span>
                <span className="text-sm font-mono text-right max-w-xs truncate">{item.value}</span>
              </div>
            ))}
          </div>
        </motion.div>

        {user?.role === "OPERATOR" && (
          <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: 0.2 }} className="bg-card border border-card-border rounded-2xl p-6 shadow-sm">
            <div className="flex items-center gap-3 mb-4">
              <Users className="w-4 h-4 text-muted-foreground" />
              <h2 className="font-semibold">Registered Users</h2>
              <span className="ml-auto text-xs font-mono bg-primary/10 text-primary px-2 py-0.5 rounded-full">OPERATOR VIEW</span>
            </div>
            <div className="space-y-2">
              {MOCK_USERS.map((u) => (
                <div key={u.id} className="flex items-center gap-3 p-3 rounded-xl hover:bg-muted/30 transition-colors">
                  <div className="w-8 h-8 rounded-full bg-primary/15 border border-primary/30 flex items-center justify-center">
                    <span className="text-xs font-mono font-bold text-primary">{u.firstName[0]}{u.lastName[0]}</span>
                  </div>
                  <div className="flex-1">
                    <div className="text-sm font-medium">{u.firstName} {u.lastName}</div>
                    <div className="text-xs text-muted-foreground font-mono">@{u.username}</div>
                  </div>
                  <span className={cn("text-xs font-mono px-2 py-0.5 rounded-full", u.role === "OPERATOR" ? "bg-primary/10 text-primary" : "bg-muted text-muted-foreground")}>
                    {u.role}
                  </span>
                </div>
              ))}
            </div>
          </motion.div>
        )}
      </div>
    </AppShell>
  );
}
