import { useState, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { useLocation } from "wouter";
import { useAuthStore } from "@/store/authStore";
import { Eye, EyeOff, ChevronRight, ShieldCheck, UserCheck } from "lucide-react";

type Tab = "login" | "register";

const SEED_OPERATOR = {
  username: "admin",
  password: "admin2026",
  firstName: "System",
  lastName: "Administrator",
  role: "OPERATOR" as const,
};

function getRegistry(): Record<string, { password: string; firstName: string; lastName: string; role: "OPERATOR" | "VIEWER" }> {
  try {
    return JSON.parse(localStorage.getItem("pipeline-user-registry") ?? "{}");
  } catch {
    return {};
  }
}

function saveRegistry(r: ReturnType<typeof getRegistry>) {
  localStorage.setItem("pipeline-user-registry", JSON.stringify(r));
}

function seedAdmin() {
  const registry = getRegistry();
  if (!registry[SEED_OPERATOR.username]) {
    registry[SEED_OPERATOR.username] = {
      password: SEED_OPERATOR.password,
      firstName: SEED_OPERATOR.firstName,
      lastName: SEED_OPERATOR.lastName,
      role: SEED_OPERATOR.role,
    };
    saveRegistry(registry);
  }
}

export default function LoginPage() {
  const [tab, setTab] = useState<Tab>("login");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [firstName, setFirstName] = useState("");
  const [lastName, setLastName] = useState("");
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [, setLocation] = useLocation();
  const { login } = useAuthStore();

  useEffect(() => {
    seedAdmin();
  }, []);

  const reset = () => {
    setUsername("");
    setPassword("");
    setFirstName("");
    setLastName("");
    setError("");
  };

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    await new Promise((r) => setTimeout(r, 600));

    if (!username || !password) {
      setError("Please enter your username and password.");
      setLoading(false);
      return;
    }

    const registry = getRegistry();
    const found = registry[username];

    if (!found) {
      setError("Username not found. Please register first.");
      setLoading(false);
      return;
    }
    if (found.password !== password) {
      setError("Incorrect password. Please try again.");
      setLoading(false);
      return;
    }

    login("demo-token-" + Date.now(), {
      username,
      firstName: found.firstName,
      lastName: found.lastName,
      role: found.role,
    });
    setLocation("/dashboard");
    setLoading(false);
  };

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setLoading(true);
    await new Promise((r) => setTimeout(r, 600));

    if (!username || !password || !firstName) {
      setError("First name, username and password are required.");
      setLoading(false);
      return;
    }
    if (password.length < 4) {
      setError("Password must be at least 4 characters.");
      setLoading(false);
      return;
    }

    const registry = getRegistry();
    if (registry[username]) {
      setError("Username already taken. Please choose another.");
      setLoading(false);
      return;
    }

    registry[username] = {
      password,
      firstName,
      lastName,
      role: "VIEWER",
    };
    saveRegistry(registry);
    login("demo-token-" + Date.now(), {
      username,
      firstName,
      lastName,
      role: "VIEWER",
    });
    setLocation("/dashboard");
    setLoading(false);
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center relative overflow-hidden bg-background">
      <div className="absolute inset-0 grid-bg opacity-50" />
      <div className="absolute inset-0 bg-gradient-to-br from-primary/5 via-transparent to-purple-500/5" />
      <div className="absolute top-1/4 left-1/4 w-64 h-64 bg-primary/10 rounded-full blur-3xl animate-pulse" />
      <div className="absolute bottom-1/4 right-1/4 w-48 h-48 bg-purple-500/10 rounded-full blur-3xl animate-pulse" style={{ animationDelay: "1s" }} />

      <motion.div
        initial={{ opacity: 0, y: 30 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6, ease: "easeOut" }}
        className="relative z-10 w-full max-w-md mx-4"
      >
        <div className="bg-card/80 backdrop-blur-2xl border border-card-border rounded-2xl shadow-2xl overflow-hidden">
          <div className="px-8 pt-8 pb-6 text-center border-b border-border">
            <motion.div
              initial={{ scale: 0.8, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              transition={{ delay: 0.2 }}
              className="w-14 h-14 bg-primary rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg glow-cyan"
            >
              <svg viewBox="0 0 24 24" fill="none" className="w-8 h-8 text-white" stroke="currentColor" strokeWidth={1.5}>
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5M2 12l10 5 10-5" />
              </svg>
            </motion.div>
            <h1 className="text-2xl font-bold tracking-tight">Pipeline Monitor</h1>
            <p className="text-muted-foreground text-sm mt-1 font-mono">AI Leak Detection · JKUAT 2026</p>
          </div>

          <div className="flex border-b border-border">
            {(["login", "register"] as Tab[]).map((t) => (
              <button
                key={t}
                onClick={() => { setTab(t); reset(); }}
                className={`flex-1 py-3 text-sm font-mono font-semibold tracking-wider uppercase transition-colors ${
                  tab === t
                    ? "text-primary border-b-2 border-primary"
                    : "text-muted-foreground hover:text-foreground"
                }`}
              >
                {t}
              </button>
            ))}
          </div>

          <div className="px-8 py-6">
            <AnimatePresence mode="wait">
              <motion.form
                key={tab}
                initial={{ opacity: 0, x: tab === "login" ? -20 : 20 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: tab === "login" ? 20 : -20 }}
                transition={{ duration: 0.2 }}
                onSubmit={tab === "login" ? handleLogin : handleRegister}
                className="space-y-4"
              >
                {tab === "register" && (
                  <>
                    <div className="grid grid-cols-2 gap-3">
                      <div>
                        <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">First Name *</label>
                        <input
                          value={firstName}
                          onChange={(e) => setFirstName(e.target.value)}
                          placeholder="John"
                          className="w-full px-3 py-2.5 bg-background border border-input rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
                        />
                      </div>
                      <div>
                        <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">Last Name</label>
                        <input
                          value={lastName}
                          onChange={(e) => setLastName(e.target.value)}
                          placeholder="Doe"
                          className="w-full px-3 py-2.5 bg-background border border-input rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
                        />
                      </div>
                    </div>

                    <div className="flex items-start gap-3 bg-primary/8 border border-primary/20 rounded-xl px-4 py-3">
                      <UserCheck className="w-4 h-4 text-primary mt-0.5 shrink-0" />
                      <div>
                        <div className="text-xs font-mono font-semibold text-primary">VIEWER ACCESS ONLY</div>
                        <div className="text-xs text-muted-foreground mt-0.5">
                          All new accounts are created as <span className="font-semibold">VIEWER</span>. Contact your system administrator to request OPERATOR privileges.
                        </div>
                      </div>
                    </div>
                  </>
                )}

                <div>
                  <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">Username</label>
                  <input
                    value={username}
                    onChange={(e) => setUsername(e.target.value)}
                    placeholder={tab === "login" ? "admin" : "your.username"}
                    autoComplete="username"
                    className="w-full px-3 py-2.5 bg-background border border-input rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
                  />
                </div>

                <div>
                  <label className="text-xs font-mono text-muted-foreground uppercase tracking-wider mb-1.5 block">Password</label>
                  <div className="relative">
                    <input
                      type={showPass ? "text" : "password"}
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      placeholder="••••••••"
                      autoComplete={tab === "login" ? "current-password" : "new-password"}
                      className="w-full px-3 py-2.5 pr-10 bg-background border border-input rounded-xl text-sm focus:outline-none focus:ring-2 focus:ring-primary/50 transition-all"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPass(!showPass)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                    >
                      {showPass ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                    </button>
                  </div>
                </div>

                {tab === "login" && (
                  <div className="flex items-center gap-2 px-3 py-2 bg-muted/50 rounded-xl border border-border">
                    <ShieldCheck className="w-3.5 h-3.5 text-primary shrink-0" />
                    <span className="text-xs font-mono text-muted-foreground">
                      Default OPERATOR: <span className="text-foreground">admin</span> / <span className="text-foreground">admin2026</span>
                    </span>
                  </div>
                )}

                {error && (
                  <motion.p
                    initial={{ opacity: 0, y: -5 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="text-sm text-destructive bg-destructive/10 px-3 py-2 rounded-lg"
                  >
                    {error}
                  </motion.p>
                )}

                <motion.button
                  type="submit"
                  disabled={loading}
                  whileHover={{ scale: 1.02 }}
                  whileTap={{ scale: 0.98 }}
                  className="w-full py-3 bg-primary text-primary-foreground rounded-xl font-semibold flex items-center justify-center gap-2 shadow-lg hover:brightness-110 transition-all disabled:opacity-70"
                >
                  {loading ? (
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                  ) : (
                    <>
                      {tab === "login" ? "ACCESS SYSTEM" : "CREATE ACCOUNT"}
                      <ChevronRight className="w-4 h-4" />
                    </>
                  )}
                </motion.button>
              </motion.form>
            </AnimatePresence>
          </div>

          <div className="px-8 pb-6 text-center">
            <p className="text-xs text-muted-foreground font-mono">
              Copper Tailings Pipeline · ENM 2026
            </p>
          </div>
        </div>
      </motion.div>
    </div>
  );
}
