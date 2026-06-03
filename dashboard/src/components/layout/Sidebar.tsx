import { Link, useLocation } from "wouter";
import { motion, AnimatePresence } from "framer-motion";
import {
  LayoutDashboard,
  History,
  FlaskConical,
  Settings,
  LogOut,
  X,
  Activity,
  Lock,
} from "lucide-react";
import { useAuthStore } from "@/store/authStore";
import { cn } from "@/lib/utils";

const NAV_ITEMS = [
  { href: "/dashboard", icon: LayoutDashboard, label: "Dashboard", operatorOnly: false },
  { href: "/history", icon: History, label: "History", operatorOnly: false },
  { href: "/simulation", icon: FlaskConical, label: "Simulation", operatorOnly: true },
  { href: "/recommendations", icon: Activity, label: "AI Recommendations", operatorOnly: false },
  { href: "/settings", icon: Settings, label: "Settings", operatorOnly: false },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export function Sidebar({ open, onClose }: SidebarProps) {
  const [location] = useLocation();
  const { logout, user } = useAuthStore();

  const isOperator = user?.role === "OPERATOR";

  const SidebarContent = () => (
    <div className="flex flex-col h-full">
      <div className="p-5 border-b border-sidebar-border">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 rounded-xl bg-primary flex items-center justify-center shadow-lg">
            <svg viewBox="0 0 24 24" fill="none" className="w-5 h-5 text-white" stroke="currentColor" strokeWidth={2}>
              <path d="M12 2L2 7l10 5 10-5-10-5z" />
              <path d="M2 17l10 5 10-5M2 12l10 5 10-5" />
            </svg>
          </div>
          <div>
            <div className="font-mono font-bold text-sm tracking-wider">PIPELINE<span className="text-primary">·AI</span></div>
            <div className="text-xs text-muted-foreground font-mono">ENM 2026 · JKUAT</div>
          </div>
        </div>
        <div className={cn(
          "mt-3 text-xs font-mono font-semibold px-2 py-1 rounded-lg inline-flex items-center gap-1.5",
          isOperator ? "bg-primary/15 text-primary" : "bg-muted text-muted-foreground"
        )}>
          {isOperator ? "● OPERATOR" : "● VIEWER"}
        </div>
      </div>

      <nav className="flex-1 p-4 space-y-1 overflow-y-auto">
        <div className="text-xs font-mono font-semibold text-muted-foreground uppercase tracking-widest px-2 mb-3">
          Navigation
        </div>
        {NAV_ITEMS.map((item) => {
          const isActive = location === item.href || (item.href !== "/" && location.startsWith(item.href));
          const isLocked = item.operatorOnly && !isOperator;
          return (
            <Link key={item.href} href={item.href}>
              <motion.div
                whileHover={{ x: isLocked ? 0 : 3 }}
                whileTap={{ scale: 0.97 }}
                onClick={onClose}
                className={cn(
                  "flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-all cursor-pointer",
                  isActive
                    ? "bg-primary text-primary-foreground shadow-sm"
                    : isLocked
                    ? "text-muted-foreground/50 hover:bg-sidebar-accent/50"
                    : "text-sidebar-foreground hover:bg-sidebar-accent"
                )}
              >
                <item.icon className="w-4 h-4 shrink-0" />
                <span>{item.label}</span>
                {isActive && !isLocked && (
                  <motion.div
                    layoutId="active-indicator"
                    className="ml-auto w-1.5 h-1.5 rounded-full bg-primary-foreground/70"
                  />
                )}
                {isLocked && (
                  <Lock className="ml-auto w-3.5 h-3.5 opacity-50" />
                )}
              </motion.div>
            </Link>
          );
        })}
      </nav>

      <div className="p-4 border-t border-sidebar-border">
        <div className="text-xs text-muted-foreground font-mono px-2 mb-3">
          Copper Tailings Pipeline
        </div>
        <motion.button
          whileHover={{ x: 3 }}
          whileTap={{ scale: 0.97 }}
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium text-muted-foreground hover:text-destructive hover:bg-destructive/10 transition-all w-full"
        >
          <LogOut className="w-4 h-4 shrink-0" />
          <span>Logout</span>
        </motion.button>
      </div>
    </div>
  );

  return (
    <>
      <aside className="hidden lg:flex flex-col w-60 border-r border-sidebar-border bg-sidebar h-screen sticky top-0 shrink-0">
        <SidebarContent />
      </aside>

      <AnimatePresence>
        {open && (
          <>
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              onClick={onClose}
              className="fixed inset-0 z-40 bg-black/60 lg:hidden"
            />
            <motion.aside
              initial={{ x: -280 }}
              animate={{ x: 0 }}
              exit={{ x: -280 }}
              transition={{ type: "spring", damping: 25, stiffness: 200 }}
              className="fixed left-0 top-0 z-50 h-full w-64 bg-sidebar border-r border-sidebar-border lg:hidden"
            >
              <div className="absolute top-3 right-3">
                <button onClick={onClose} className="p-2 rounded-lg hover:bg-muted">
                  <X className="w-4 h-4" />
                </button>
              </div>
              <SidebarContent />
            </motion.aside>
          </>
        )}
      </AnimatePresence>
    </>
  );
}
