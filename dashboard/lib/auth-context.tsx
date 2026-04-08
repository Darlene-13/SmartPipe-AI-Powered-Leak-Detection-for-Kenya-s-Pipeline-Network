"use client"

import { createContext, useContext, useState, useCallback, type ReactNode } from "react"

interface AuthUser {
  username: string
  token: string
  expiresAt: string
}

interface AuthContextType {
  user: AuthUser | null
  login: (username: string, password: string) => Promise<boolean>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextType | null>(null)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<AuthUser | null>(() => {
    if (typeof window === "undefined") return null
    const stored = sessionStorage.getItem("pipeline_auth")
    if (stored) {
      try {
        const parsed = JSON.parse(stored)
        if (new Date(parsed.expiresAt) > new Date()) return parsed
        sessionStorage.removeItem("pipeline_auth")
      } catch {
        return null
      }
    }
    return null
  })

  const login = useCallback(async (username: string, password: string) => {
    // Simulated auth - in production, POST /api/auth/login
    await new Promise((resolve) => setTimeout(resolve, 800))
    if (username && password) {
      const authUser: AuthUser = {
        username,
        token: `sentinel_${Date.now()}_${Math.random().toString(36).slice(2)}`,
        expiresAt: new Date(Date.now() + 8 * 60 * 60 * 1000).toISOString(),
      }
      setUser(authUser)
      sessionStorage.setItem("pipeline_auth", JSON.stringify(authUser))
      return true
    }
    return false
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    sessionStorage.removeItem("pipeline_auth")
  }, [])

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated: !!user }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) throw new Error("useAuth must be used within AuthProvider")
  return context
}
