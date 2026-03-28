"use client"

import { useState } from "react"
import { useAuth } from "@/lib/auth-context"
import { Shield, Lock, Loader2, Terminal } from "lucide-react"

export function LoginForm() {
  const { login } = useAuth()
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState("")
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    setError("")
    if (!username || !password) {
      setError("ERROR: Both operator ID and access key required")
      return
    }
    setLoading(true)
    const success = await login(username, password)
    if (!success) {
      setError("ERROR: Invalid credentials - access denied")
    }
    setLoading(false)
  }

  return (
    <div className="min-h-screen flex flex-col bg-background relative overflow-hidden">
      {/* Topographic grid background */}
      <div className="absolute inset-0 opacity-[0.03]" style={{
        backgroundImage: `
          radial-gradient(circle at 25% 25%, rgba(0, 255, 100, 0.3) 0%, transparent 50%),
          radial-gradient(circle at 75% 75%, rgba(0, 255, 100, 0.15) 0%, transparent 50%),
          linear-gradient(rgba(255,255,255,0.03) 1px, transparent 1px),
          linear-gradient(90deg, rgba(255,255,255,0.03) 1px, transparent 1px)
        `,
        backgroundSize: "100% 100%, 100% 100%, 40px 40px, 40px 40px",
      }} />

      {/* Top system bar */}
      <header className="relative z-10 border-b border-border bg-card/60 backdrop-blur-sm">
        <div className="max-w-[1400px] mx-auto px-4 lg:px-6 flex items-center h-12">
          <div className="flex items-center gap-3">
            <div className="h-7 w-7 rounded-md border border-primary/30 bg-primary/10 flex items-center justify-center">
              <Shield className="h-3.5 w-3.5 text-primary" />
            </div>
            <span className="font-mono text-sm tracking-wider">
              <span className="text-primary font-semibold">MINING_OPS_SYS</span>
              <span className="text-muted-foreground"> // NODE_CONTROL</span>
            </span>
          </div>
        </div>
      </header>

      {/* Login card */}
      <div className="relative z-10 flex-1 flex items-center justify-center px-4">
        <div className="w-full max-w-md">
          {/* Card header bar */}
          <div className="rounded-t-lg border border-b-0 border-border bg-card/80 backdrop-blur-sm px-5 py-3 flex items-center gap-2">
            <Terminal className="h-3.5 w-3.5 text-primary" />
            <span className="font-mono text-xs tracking-widest text-muted-foreground">
              AUTH_GATEWAY // SECURE_LOGIN
            </span>
          </div>

          <div className="rounded-b-lg border border-border bg-card/60 backdrop-blur-sm px-8 py-8">
            <div className="flex flex-col items-center mb-8">
              <div className="h-14 w-14 rounded-xl border border-primary/30 bg-primary/10 flex items-center justify-center mb-5">
                <Shield className="h-7 w-7 text-primary" />
              </div>
              <h1 className="text-xl font-semibold text-foreground text-balance text-center">
                Operator Login
              </h1>
              <p className="text-sm text-muted-foreground mt-1.5">
                Restricted mining operations access
              </p>
            </div>

            <form onSubmit={handleSubmit} className="flex flex-col gap-5">
              <div>
                <label htmlFor="username" className="block font-mono text-[11px] font-medium text-muted-foreground mb-2 tracking-widest uppercase">
                  Operator ID
                </label>
                <input
                  id="username"
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full h-11 px-4 rounded-md border border-border bg-secondary/80 text-foreground font-mono text-sm placeholder:text-muted-foreground/50 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary/50 transition-colors"
                  placeholder="operator"
                  autoComplete="username"
                />
              </div>
              <div>
                <label htmlFor="password" className="block font-mono text-[11px] font-medium text-muted-foreground mb-2 tracking-widest uppercase">
                  Access Key
                </label>
                <div className="relative">
                  <Lock className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-muted-foreground/50" />
                  <input
                    id="password"
                    type="password"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="w-full h-11 pl-10 pr-4 rounded-md border border-border bg-secondary/80 text-foreground font-mono text-sm placeholder:text-muted-foreground/50 focus:outline-none focus:ring-2 focus:ring-primary/50 focus:border-primary/50 transition-colors"
                    placeholder="Enter access key"
                    autoComplete="current-password"
                  />
                </div>
              </div>

              {error && (
                <div className="font-mono text-xs text-destructive bg-destructive/10 border border-destructive/20 rounded-md px-3 py-2">
                  {error}
                </div>
              )}

              <button
                type="submit"
                disabled={loading}
                className="w-full h-11 rounded-md bg-primary text-primary-foreground font-mono font-semibold text-sm tracking-widest uppercase hover:opacity-90 transition-opacity disabled:opacity-50 flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 className="h-4 w-4 animate-spin" />
                    AUTHENTICATING...
                  </>
                ) : (
                  "INITIALIZE SESSION"
                )}
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  )
}
