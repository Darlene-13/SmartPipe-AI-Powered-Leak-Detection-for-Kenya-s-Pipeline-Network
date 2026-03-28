"use client"

import Link from "next/link"
import { usePathname } from "next/navigation"
import { cn } from "@/lib/utils"
import { useAuth } from "@/lib/auth-context"
import { Shield, LogOut, BarChart3, Play, LayoutDashboard } from "lucide-react"
import { useState, useEffect } from "react"

const NAV_ITEMS = [
  { href: "/dashboard", label: "LIVE_MONITOR", icon: LayoutDashboard },
  { href: "/history", label: "HISTORY_LOG", icon: BarChart3 },
  { href: "/simulation", label: "HIL_SIM", icon: Play },
]

export function DashboardHeader() {
  const { user, logout } = useAuth()
  const pathname = usePathname()
  const [clock, setClock] = useState("")

  useEffect(() => {
    function update() {
      const now = new Date()
      setClock(now.toLocaleTimeString("en-GB", { hour12: false }))
    }
    update()
    const id = setInterval(update, 1000)
    return () => clearInterval(id)
  }, [])

  return (
    <header className="border-b border-border bg-card/80 backdrop-blur-sm sticky top-0 z-50">
      <div className="max-w-[1600px] mx-auto px-4 lg:px-6">
        <div className="flex items-center justify-between h-12">
          <div className="flex items-center gap-6">
            <Link href="/dashboard" className="flex items-center gap-2.5">
              <div className="h-7 w-7 rounded-md border border-primary/30 bg-primary/10 flex items-center justify-center">
                <Shield className="h-3.5 w-3.5 text-primary" />
              </div>
              <span className="font-mono text-xs tracking-wider hidden sm:inline">
                <span className="text-primary font-semibold">PIPELINE_SENTINEL</span>
                <span className="text-muted-foreground"> // AI</span>
              </span>
            </Link>

            <div className="h-5 w-px bg-border hidden md:block" />

            <nav className="hidden md:flex items-center gap-1">
              {NAV_ITEMS.map(({ href, label, icon: Icon }) => (
                <Link
                  key={href}
                  href={href}
                  className={cn(
                    "flex items-center gap-1.5 px-3 py-1.5 rounded-md font-mono text-xs tracking-wider transition-colors",
                    pathname === href
                      ? "bg-primary/10 text-primary border border-primary/20"
                      : "text-muted-foreground hover:text-foreground hover:bg-accent/50"
                  )}
                >
                  <Icon className="h-3 w-3" />
                  {label}
                </Link>
              ))}
            </nav>
          </div>

          <div className="flex items-center gap-4">
            <div className="hidden sm:flex items-center gap-3 font-mono text-xs">
              <span className="flex items-center gap-1.5">
                <span className="h-1.5 w-1.5 rounded-full bg-primary animate-pulse" />
                <span className="text-primary font-medium tracking-wider">LIVE</span>
              </span>
              <span className="text-muted-foreground tabular-nums">{clock}</span>
            </div>

            <div className="h-5 w-px bg-border hidden sm:block" />

            {user && (
              <span className="font-mono text-xs text-muted-foreground hidden sm:block">
                OPR:<span className="text-foreground ml-1">{user.username.toUpperCase()}</span>
              </span>
            )}

            <button
              onClick={logout}
              className="flex items-center gap-1.5 font-mono text-xs text-muted-foreground hover:text-destructive transition-colors px-2 py-1 rounded-md hover:bg-destructive/10"
              aria-label="Log out of system"
            >
              <LogOut className="h-3 w-3" />
              <span className="hidden sm:inline tracking-wider">EXIT</span>
            </button>
          </div>
        </div>
      </div>
    </header>
  )
}
